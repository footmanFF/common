package com.footmanff.common.util.biz;


import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ProtectiveLoaderTest {

    @Test
    public void get() throws Exception {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(400, 400, 120L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

        ProtectiveLoader<String, String> protectiveLoader = new ProtectiveLoader<>(10);

        int c = 400;
        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch countDownLatch = new CountDownLatch(c);

        for (int i = 0; i < c; i++) {
            pool.submit(() -> {
                try {
                    long s = System.currentTimeMillis();
                    DataHolder<String> data = protectiveLoader.get("someKey", () -> {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                        }
                        count.incrementAndGet();
                        return "result";
                    }, 500L, false);
                    System.out.println("isSuccess: " + data.isSuccess() + " cost: " + (System.currentTimeMillis() - s));
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        System.out.println("supply 执行次数：" + count);
    }

}