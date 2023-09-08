package com.footmanff.common.util.current.future;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletableFutureUtilTest {
    
    @Test
    public void t1() {
        CompletableFutureUtil completableFutureUtil = new CompletableFutureUtil();
        AtomicInteger count = new AtomicInteger();

        List<Runnable> runnableList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            runnableList.add(() -> {
                count.incrementAndGet();
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        completableFutureUtil.composed(runnableList).join();
        System.out.println(count.get());
    }
    
}
