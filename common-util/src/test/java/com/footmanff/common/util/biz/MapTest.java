package com.footmanff.common.util.biz;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class MapTest {

    public static void main(String[] args) throws Exception {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(400, 400, 120L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

        ConcurrentHashMap<String, String> lockMap = new ConcurrentHashMap<>();

        CountDownLatch countDownLatch = new CountDownLatch(2);
        
        for (int i = 0; i < 222; i++) {
            pool.submit(() -> {
                while (true) {
                    String pre = lockMap.putIfAbsent("123", "value");
                    if (pre == null) {
                        System.out.println("null");
                    }
                }
            });
        }
        countDownLatch.await();

    }

}
