package com.footmanff.common.util.biz;

import com.footmanff.common.util.biz.batch.BatchExecutor2;
import com.footmanff.common.util.biz.batch.BatchExecutor3;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BatchExecutorTest2 {

    public static void main(String[] args) throws Exception {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(400, 400, 120L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

        int c = 2;
        BatchExecutor3 batchExecutor3 = new BatchExecutor3(10, 500);
        CountDownLatch countDownLatch = new CountDownLatch(c);

        for (int i = 0; i < c; i++) {
            pool.submit(() -> {
                try {
                    batchExecutor3.execute("someKey", 1, param -> {
                        return "success";
                    });
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        System.out.println("正常结束");
    }

}
