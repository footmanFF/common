package com.footmanff.common.util.biz.batch;

import com.footmanff.common.util.base.Holder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

public class BatchExecutor3<T, R> {

    private final Map<String, AtomicLong> seqMap = new ConcurrentHashMap<>();

    private final Map<String, Bucket3<T, R>> cache2 = new ConcurrentHashMap<>();

    private final int batchLimit;

    private final long maxTime;

    public BatchExecutor3(int batchLimit, long maxTime) {
        this.batchLimit = batchLimit;
        this.maxTime = maxTime;
    }

    public R execute(String key, T task, Function<BatchExecParam<T>, R> bachFunc) {
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
        Bucket3<T, R> bucket3 = cache2.compute(cacheKey, (k, v) -> {
            if (v == null) {
                Bucket3<T, R> b = new Bucket3<>();
                isMainHolder.set(Boolean.TRUE);
                b.setMainThread(Thread.currentThread());
                b.getTaskList().offer(task);
                return b;
            } else {
                v.getSubThreadList().offer(Thread.currentThread());
                v.getTaskList().offer(task);
                return v;
            }
        });
        boolean isMain = isMainHolder.get();

        if (isMain) {
            LockSupport.parkNanos(maxTime * 1000000L);

            List<T> taskList = new ArrayList<>();
            T t;
            while ((t = bucket3.getTaskList().poll()) != null) {
                taskList.add(t);
            }

            BatchExecParam<T> batchExecParam = new BatchExecParam<>(key, taskList, cacheKey);
            batchExecParam.setBucketId(bucket3.getId());
            BatchExecResult<R> batchExecResult = exec(bachFunc, batchExecParam);
            setBucketResult(bucket3, batchExecResult);

            Thread subThread;
            while ((subThread = bucket3.getSubThreadList().poll()) != null) {
                LockSupport.unpark(subThread);
            }

            return getResult(bucket3);
        } else {
            if (isLastNum) {
                LockSupport.unpark(bucket3.getMainThread());
            }
            LockSupport.park();

            return getResult(bucket3);
        }
    }

    private R getResult(Bucket3<T, R> bucket) {
        if (bucket.getResult() != null) {
            return bucket.getResult();
        } else {
            throw new RuntimeException(bucket.getExp());
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

    private void setBucketResult(Bucket3<T, R> bucket, BatchExecResult<R> result) {
        if (result.getResult() != null) {
            bucket.setResult(result.getResult());
        } else {
            bucket.setExp(result.getExp());
        }
    }

}
