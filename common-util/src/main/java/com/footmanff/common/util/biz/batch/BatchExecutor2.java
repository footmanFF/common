package com.footmanff.common.util.biz.batch;

import com.footmanff.common.util.base.Holder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class BatchExecutor2<T, R> {

    private final Cache<String, Bucket2<T, R>> loadingCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(4, TimeUnit.SECONDS)
            .build();

    private final Map<String, AtomicLong> seqMap = new ConcurrentHashMap<>();

    private final int batchLimit;

    private final long maxTime;

    public BatchExecutor2(int batchLimit, long maxTime) {
        this.batchLimit = batchLimit;
        this.maxTime = maxTime;
    }

    public R execute(String key, T task, Function<BatchExecParam<T>, R> bachFunc) {
        AcquireBucketResult<T, R> acquireBucketResult = acquireBucket(key, task);
        Bucket2<T, R> bucket2 = acquireBucketResult.getBucket2();
        boolean isMain = acquireBucketResult.isMain();

        if (isMain) {
            try {
                bucket2.getMainCondition().await(maxTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                bucket2.getMainLock().unlock();
            }

            // 开始批处理
            bucket2.processTask(key, bachFunc);
            return getResult(bucket2);
        } else {
            // 被主线程唤醒，主线程完成了批处理，从bucket中拿结果返回
            return getResult(bucket2);
        }
    }

    private R getResult(Bucket2<T, R> bucket) {
        if (bucket.getResult() != null) {
            return bucket.getResult();
        } else {
            throw new RuntimeException(bucket.getExp());
        }
    }

    private AcquireBucketResult<T, R> acquireBucket(String key, T task) {
        AcquireBucketResult<T, R> acquireBucketResult = acquireBucketInner(key, task);

        while (!acquireBucketResult.isBucketInitSuccess()) {
            acquireBucketResult = acquireBucketInner(key, task);
        }

        return acquireBucketResult;
    }

    /**
     * 根据时间窗口、批次获取一个处理bucket。获取以后会尝试初始化bucket，因为并发原因可能会初始化失败。
     * <p/>
     * bucket初始化会读取bucket本身是否已经执行过批处理，如果是，则此bucket已经不可用，需要取新的bucket
     * <p/>
     * bucket初始化、bucket的批处理，可能会并发执行，因此需要对这两个操作加锁控制
     *
     * @param key  任务key
     * @param task 任务
     * @return bucket获取结果
     */
    private AcquireBucketResult<T, R> acquireBucketInner(String key, T task) {
        long time = System.currentTimeMillis();
        long timeWin = time / maxTime;
        String seqKey = key + "_" + timeWin;

        AtomicLong seq = seqMap.computeIfAbsent(seqKey, k -> new AtomicLong());
        long num = seq.incrementAndGet();
        long batchNum = num / batchLimit;

        // 是否跨越了一个批次
        boolean isLastNum = num % batchLimit == batchLimit - 1;

        String cacheKey = seqKey + "_" + batchNum;

        Holder<Boolean> isMainHolder = Holder.of(false);
        Bucket2<T, R> bucket2;
        try {
            bucket2 = loadingCache.get(cacheKey, () -> {
                Bucket2<T, R> b = new Bucket2<>();
                isMainHolder.set(Boolean.TRUE);
                b.getMainLock().lock();
                return b;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        boolean isMain = isMainHolder.get();
        boolean initSuccess = bucket2.init(task, isLastNum, isMain);

        // 当前使用的锁，isMain代表首次进入的请求，非首次进入的请求都作为子锁
        ReentrantLock currentLock = isMainHolder.get() ? bucket2.getMainLock() : bucket2.getSubLock();
        Condition currentCondition = isMainHolder.get() ? bucket2.getMainCondition() : bucket2.getSubCondition();

        AcquireBucketResult<T, R> result = new AcquireBucketResult<>();
        result.setBucket2(bucket2);
        result.setLastNum(isLastNum);
        result.setMain(isMain);
        result.setCurrentLock(currentLock);
        result.setCurrentCondition(currentCondition);
        result.setBucketInitSuccess(initSuccess);
        return result;
    }

    @Data
    private static class AcquireBucketResult<T, R> {
        private Bucket2<T, R> bucket2;
        private boolean bucketInitSuccess;
        private boolean lastNum;
        private boolean main;
        private ReentrantLock currentLock;
        private Condition currentCondition;
    }

}
