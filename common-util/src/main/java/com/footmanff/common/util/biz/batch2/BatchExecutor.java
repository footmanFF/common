package com.footmanff.common.util.biz.batch2;

import com.footmanff.common.util.biz.CountableFutureTask;
import com.footmanff.common.util.biz.batch3.DataConsumer;
import com.footmanff.common.util.biz.batch3.DataProducer;
import com.footmanff.common.util.collection.CollectionUtil;

import java.util.List;
import java.util.concurrent.*;

public class BatchExecutor<T, R> {

    private final int batchLimit;

    private final long maxTime;

    private final int thread;

    private DataProducer<Task<T, R>> dataProducer;

    public BatchExecutor(int batchLimit, long maxTime, int bufferCount, int bufferSize, int thread, BatchExecHandler<T, R> handler) {
        this.batchLimit = batchLimit;
        this.maxTime = maxTime;
        this.thread = thread;

        dataProducer = new DataProducer<>(bufferCount, bufferSize, "BatchExecutor-", new DataConsumer<Task<T, R>>() {
            @Override
            public void consume(List<Task<T, R>> dataList) {
                Task<T, R> task = dataList.get(0);
                List<T> taskList = CollectionUtil.collect(dataList, e -> e.getTask());
                BatchExecParam<T> param = new BatchExecParam<>(task.getKey(), taskList);
                R result = handler.process(param);
                for (Task<T, R> data : dataList) {
                    data.getCountableFutureTask().set(result);
                }
            }

            @Override
            public void onError(List<Task<T, R>> dataList, Throwable t) {
                for (Task<T, R> data : dataList) {
                    data.getCountableFutureTask().set(t);
                }
            }
        }, maxTime, (key, limit) -> {
            // 根据key做hash，确定一个队列
            int hashCode = key.hashCode();
            hashCode = Math.abs(hashCode);
            return hashCode % limit;
        });
        dataProducer.start(thread, batchLimit);
    }

    public R execute(String key, T task) {
        CountableFutureTask<Object> futureTask = new CountableFutureTask<>(() -> null);

        Task<T, R> innerTask = new Task<>();
        innerTask.setCountableFutureTask(futureTask);
        innerTask.setTask(task);

        boolean add = dataProducer.produce(key, innerTask);
        if (!add) {
            throw new RuntimeException("add fail");
        }
        try {
            Object result = futureTask.get();
            if (result instanceof Throwable) {
                throw new RuntimeException((Throwable) result);
            } else {
                return (R) result;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
