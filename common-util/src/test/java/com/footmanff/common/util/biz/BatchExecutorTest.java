package com.footmanff.common.util.biz;

import com.footmanff.common.util.biz.batch.BatchExecutor;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BatchExecutorTest {

    public static void main(String[] args) throws Exception {
        System.out.println("开始！！！！！！！！");

        BatchExecutor<Integer, String> batchExecutor = new BatchExecutor<>();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(400, 400, 120L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

        String key = "someKey";

        int c = 80;
        int execTotalLimit = 50000;
        long costPerExec = 10L;
        AtomicInteger execCount = new AtomicInteger();
        AtomicInteger totalTask = new AtomicInteger();
        AtomicInteger totalCall = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(c);

        AtomicInteger batchExecCount = new AtomicInteger();

        AtomicLong totalCost = new AtomicLong();

        final AtomicLong normalCost = new AtomicLong();

        for (int i = 0; i < c; i++) {
            pool.submit(() -> {
                try {
                    while (execCount.incrementAndGet() <= execTotalLimit) {
                        try {
                            long s = System.nanoTime();
                            batchExecutor.execute(key, 1, 10L, 10, param -> {
                                long fs = System.nanoTime();
                                totalTask.addAndGet(param.getTaskList().size());
                                batchExecCount.incrementAndGet();
                                System.out.println("执行批量: " + param.getBucketId() + " size: " + param.getTaskList().size());
                                try {
                                    Thread.sleep(costPerExec);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                normalCost.addAndGet(System.nanoTime() - fs);
                                return "success";
                            });
                            totalCost.addAndGet(System.nanoTime() - s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            totalCall.incrementAndGet();
                        }
                        try {
                            Thread.sleep(10L);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        

        System.out.println("最终 totalTask: " + totalTask.get() + " totalCall: " + totalCall.get() + " totalCost: " + totalCost.get() / 1000000L + "/" + normalCost.get() / 1000000L + " batchExecCount: " + batchExecCount);

        System.out.println("lockCost: " + batchExecutor.getLockCost().get() / 1000000L);

        System.out.println("waitCost: " + batchExecutor.getWaitCost().get() / 1000000L);

        System.out.println("signalCost: " + batchExecutor.getSignalCost().get() / 1000000L);
    }
    
}
