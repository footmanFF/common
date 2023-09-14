package com.footmanff.common.util.biz.batch;

import com.footmanff.common.util.base.Holder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class BatchExecutor<T, R> {

    private final Cache<String, Bucket<T, R>> loadingCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(4, TimeUnit.SECONDS)
            .build();

    private AtomicLong lockCost = new AtomicLong();

    private AtomicLong signalCost = new AtomicLong();

    public AtomicLong getWaitCost() {
        return waitCost;
    }

    public AtomicLong getSignalCost() {
        return signalCost;
    }

    private AtomicLong waitCost = new AtomicLong();

    public AtomicLong getLockCost() {
        return lockCost;
    }

    /**
     * 根据key去做合并执行，按照时间窗口合并，或者达到任务限制数触发合并
     *
     * @param key        执行批次唯一键
     * @param param      当前执行参数
     * @param maxTime    最长不能超过花奴才能得超时时间1秒
     * @param batchLimit 一个批次最大合并任务数
     * @param bachFunc   合并多个任务函数
     * @return 合并以后的结果
     */
    public R execute(String key, T param, long maxTime, int batchLimit, Function<BatchExecParam<T>, R> bachFunc) {
        long timeWin = System.currentTimeMillis() / maxTime;
        String cacheKey = key + "_" + timeWin;

        Result result = acquireAndLockBucket(key, cacheKey);

        ReentrantLock lock = result.getLock();
        Condition condition = result.getCondition();
        Bucket<T, R> bucket = result.getBucket();
        boolean isMain = result.isMain();

        try {
            bucket.addTask(param);
            if (isMain) {
                // 第一个进入的key，仅等待一个时间窗口
                try {
                    // System.out.println("「主线程」开始沉睡");
                    boolean timeout = condition.await(maxTime, TimeUnit.MILLISECONDS);
                    if (timeout) {
                        // System.out.println("超时");
                    } else {
                        // System.out.println("「主线程」被唤醒");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                bucket.setMainThreadSignal(true);
                loadingCache.invalidate(cacheKey);
                bucket.setInvalidated(true);

                // 开始批处理
                BatchExecParam<T> batchExecParam = new BatchExecParam<>(key, bucket.getTaskList(), cacheKey);
                batchExecParam.setBucketId(bucket.getId());
                BatchExecResult<R> batchExecResult = exec(bachFunc, batchExecParam);
                setBucketResult(bucket, batchExecResult);

                // 唤醒后续等待的线程
                for (Pair<ReentrantLock, Condition> pair : bucket.getSubThreadConditionList()) {
                    pair.getLeft().lock();
                    try {
                        pair.getRight().signal();
                    } finally {
                        pair.getLeft().unlock();
                    }
                }
                return getResult(bucket);
            } else {
                // 子线程超过一定数量，唤醒主线程
                if (bucket.getTaskList().size() >= batchLimit && !bucket.isMainThreadSignal()) {
                    // 尝试提前唤醒主线程
                    while (true) {
                        if (bucket.isMainThreadSignal()) {
                            break;
                        }
                        boolean locked = bucket.getLock().tryLock();
                        if (!locked) {
                            continue;
                        }
                        try {
                            bucket.getMainThreadCondition().signal();
                        } catch (IllegalMonitorStateException e) {
                            throw new RuntimeException(e);
                        } finally {
                            bucket.getLock().unlock();
                        }
                        bucket.setMainThreadSignal(true);
                        break;
                    }
                    // System.out.println("唤醒主线程");
                    // System.out.println("超过batchLimit: " + bucket.getId());
                }
                bucket.addSubThreadCondition(Pair.of(lock, condition));
                // 后续进入的key，等待第一个进入的线程唤醒
                try {
                    long s = System.nanoTime();
                    condition.await();
                    waitCost.addAndGet(System.nanoTime() - s);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // 被主线程唤醒，主线程完成了批处理，从bucket中拿结果返回
                return getResult(bucket);
            }
        } finally {
            lock.unlock();
        }
    }

    private Result acquireAndLockBucket(String key, String cacheKey) {
        // 高并发情况下，容易饿死，一直抢不到可用的bucket
        while (true) {
            Holder<Boolean> isMain = Holder.of(Boolean.FALSE);
            Bucket<T, R> bucket;
            try {
                bucket = loadingCache.get(cacheKey, () -> {
                    ReentrantLock lock = new ReentrantLock(false);
                    Bucket<T, R> b = new Bucket<>();
                    b.setKey(key);
                    b.setCacheKey(cacheKey);
                    b.setMainThreadCondition(lock.newCondition());
                    b.setLock(lock);
                    isMain.set(Boolean.TRUE);
                    return b;
                });
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            if (bucket.isInvalidated()) {
                // System.out.println("重新acquire " + bucket.getId() + " " + Thread.currentThread().getName());
                continue;
            }
            ReentrantLock lock;
            Condition condition;
            if (isMain.get()) {
                lock = bucket.getLock();
                condition = bucket.getMainThreadCondition();
            } else {
                lock = new ReentrantLock(false);
                condition = lock.newCondition();
            }
            long s = System.nanoTime();
            lock.lock();
            lockCost.addAndGet(System.nanoTime() - s);
            if (bucket.isInvalidated()) {
                // bucket已经执行结束，代表已经合并过，并且不再接受新的任务，需要重新向缓存拿bucket
                lock.unlock();
                // System.out.println("重新acquireAndLockBucket " + bucket.getId() + " " + Thread.currentThread().getName());
                continue;
            }
            Result result = new Result();
            result.setBucket(bucket);
            result.setMain(isMain.get());
            result.setCondition(condition);
            result.setLock(lock);
            return result;
        }
    }

    private BatchExecResult<R> exec(Function<BatchExecParam<T>, R> bachFunc, BatchExecParam<T> batchExecParam) {
        try {
            R result = bachFunc.apply(batchExecParam);
            return new BatchExecResult<>(result);
        } catch (Throwable e) {
            return new BatchExecResult<>(e);
        }
    }

    private void setBucketResult(Bucket<T, R> bucket, BatchExecResult<R> result) {
        if (result.getResult() != null) {
            bucket.setResult(result.getResult());
        } else {
            bucket.setExp(result.getExp());
        }
    }

    private R getResult(Bucket<T, R> bucket) {
        if (bucket.getResult() != null) {
            return bucket.getResult();
        } else {
            throw new RuntimeException(bucket.getExp());
        }
    }

    private class Result {
        private Bucket<T, R> bucket;
        private boolean main;
        private Condition condition;
        private ReentrantLock lock;

        public Bucket<T, R> getBucket() {
            return bucket;
        }

        public void setBucket(Bucket<T, R> bucket) {
            this.bucket = bucket;
        }

        public boolean isMain() {
            return main;
        }

        public void setMain(boolean main) {
            this.main = main;
        }

        public Condition getCondition() {
            return condition;
        }

        public void setCondition(Condition condition) {
            this.condition = condition;
        }

        public ReentrantLock getLock() {
            return lock;
        }

        public void setLock(ReentrantLock lock) {
            this.lock = lock;
        }
    }

}