package com.footmanff.common.util.biz;

import com.footmanff.common.util.biz.batch.BatchExecutor3;
import com.footmanff.common.util.biz.batch2.BatchExecHandler;
import com.footmanff.common.util.biz.batch2.BatchExecParam;
import com.footmanff.common.util.biz.batch2.BatchExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BatchExecutorTest3 {

    public static void main(String[] args) throws Exception {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(400, 400, 120L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

        AtomicInteger totalTask = new AtomicInteger();
        AtomicInteger totalCall = new AtomicInteger();
        AtomicLong totalCost = new AtomicLong();
        AtomicLong normalCost = new AtomicLong();

        int batchLimit = 10;
        long maxTime = 25L;
        int bufferCount = 200;
        int bufferSize = 100;
        int thread = 20;
        BatchExecHandler<Integer, String> handler = new BatchExecHandler<Integer, String>() {
            @Override
            public String process(BatchExecParam<Integer> param) {
                totalTask.addAndGet(param.getTaskList().size());
                long fs = System.nanoTime();
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                normalCost.addAndGet(System.nanoTime() - fs);
                return "success";
            }
        };

        int c = 100;
        BatchExecutor<Integer, String> batchExecutor3 = new BatchExecutor<>(batchLimit, maxTime, bufferCount, bufferSize, thread, handler);
        CountDownLatch countDownLatch = new CountDownLatch(c);

        for (int i = 0; i < c; i++) {
            pool.submit(() -> {
                try {
                    long s = System.nanoTime();
                    batchExecutor3.execute("someKey", 1);
                    totalCost.addAndGet(System.nanoTime() - s);
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    totalCall.incrementAndGet();
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        System.out.println("正常结束");

        System.out.println("最终 totalTask: " + totalTask.get() + " totalCall: " + totalCall.get() + " totalCost: " + totalCost.get() / 1000000L + "/" + normalCost.get() / 1000000L);
    }

}
