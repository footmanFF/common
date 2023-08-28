package com.footmanff.common.util.biz;

import com.footmanff.common.util.biz.batch.Bucket;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.*;

public class CacheTest {

    public static void main(String[] args) throws Exception {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(400, 400, 120L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

        Cache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.SECONDS)
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(2);

        for (int i = 0; i < 1120; i++) {
            pool.submit(() -> {
                while (true) {
                    try {
                        loadingCache.get("1234", () -> {
                            System.out.println("加载");
                            return "aa";
                        });
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        countDownLatch.await();
    }

}
