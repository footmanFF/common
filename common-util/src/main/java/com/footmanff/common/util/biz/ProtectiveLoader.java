package com.footmanff.common.util.biz;

import java.util.concurrent.*;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 防击穿的数据加载工具。保证同样的key，同一个jvm只有一个线程去加载数据，其他并发请求都正常返回数据
 */
public class ProtectiveLoader<K, V> {

    private final Logger logger = LoggerFactory.getLogger(ProtectiveLoader.class);

    private final ConcurrentHashMap<K, CountableFutureTask<V>> loadingDataMap = new ConcurrentHashMap<>();

    private static final Long DEFAULT_WAIT_TIMEOUT = 200L;

    private static final int DEFAULT_MAX_WAIT_COUNT = 50;

    /**
     * 并发加载时，阻塞等待获取数据的线程最大数量
     * 注：这个参数主要解决由于执行数据加载任务的线程意外耗时比较久时，其他线程全部被阻塞导致线上层程池满的问题
     */
    private final int maxWaitCount;

    private final Cache<K, V> loadingCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .build();

    public ProtectiveLoader(int maxWaitCount) {
        this.maxWaitCount = maxWaitCount;
    }

    public ProtectiveLoader() {
        this.maxWaitCount = DEFAULT_MAX_WAIT_COUNT;
    }

    /**
     * 加载单个数据 (默认等待其他线程超时时间是200ms，超时之后直接返回null)
     *
     * @param key      数据对应的key
     * @param supplier 数据加载器
     * @return DataHolder 返回结果的封装，加载数据的异常也会透出来，由调用方决定如何处理异常
     */
    public DataHolder<V> get(K key, Supplier<V> supplier) {
        return get(key, supplier, DEFAULT_WAIT_TIMEOUT, false);
    }

    /**
     * 加载单个数据
     *
     * @param key                    数据对应的key
     * @param supplier               数据加载器
     * @param maxWaitTime            等待其他线程获取数据的超时时间
     * @param loadBySelfAfterTimeout 等待其他线程超时之后，当前线程是否自己获取数据
     * @return DataHolder 返回结果的封装，加载数据的异常也会透出来，由调用方决定如何处理异常
     */
    public DataHolder<V> get(K key, Supplier<V> supplier, Long maxWaitTime, boolean loadBySelfAfterTimeout) {
        CountableFutureTask<V> futureTask = loadingDataMap.get(key);

        if (futureTask != null && futureTask.isDone()) {
            // case1:已经查询完毕
            return getFromDoneFuture(futureTask, false);
        } else if (futureTask != null) {
            // case2:其他并发的线程正在查询
            return getFromDoingFuture(futureTask, maxWaitTime, key, supplier, loadBySelfAfterTimeout);
        } else {
            // case3:尝试做第一次获取数据的线程
            futureTask = new CountableFutureTask<>(supplier::get);
            CountableFutureTask<V> preFuture = loadingDataMap.putIfAbsent(key, futureTask);
            if (preFuture == null) {
                futureTask.run();
                try {
                    DataHolder<V> dataHolder = getFromDoneFuture(futureTask, true);

                    // 加入临时的本地缓存，以便出现大量阻塞时，将过载的线程降级为从本地缓存取
                    loadingCache.put(key, dataHolder.getData());

                    return dataHolder;
                } finally {
                    // 放入map的线程要负责清除，避免内存泄露
                    loadingDataMap.remove(key);
                }
            } else {
                // 被别的线程抢先一步
                return getFromDoingFuture(preFuture, maxWaitTime, key, supplier, loadBySelfAfterTimeout);
            }
        }
    }

    private DataHolder<V> getFromDoneFuture(FutureTask<V> futureTask, boolean loadBySelf) {
        try {
            V data = futureTask.get();
            return new DataHolder<>(data, loadBySelf);
        } catch (InterruptedException e) {
            logger.error("error", e);
            Thread.currentThread().interrupt();
            return new DataHolder<>(e, loadBySelf);
        } catch (ExecutionException e) {
            logger.error("error", e);
            return new DataHolder<>(e.getCause(), loadBySelf);
        }
    }

    private DataHolder<V> getFromDoingFuture(CountableFutureTask<V> futureTask, Long timeout, K key, Supplier<V> supplier, boolean loadBySelfAfterTimeout) {
        int waitedCount = futureTask.incrWaitCount();
        if (waitedCount > maxWaitCount) {
            // 当前阻塞的线程已经很多了，不等了，降级为从本地缓存取
            if (logger.isInfoEnabled()) {
                logger.info("当前future已执行{}ms, 阻塞等待的线程数:{}，使用本地缓存", System.currentTimeMillis() - futureTask.getCreatedTime(),
                        waitedCount);
            }
            try {
                // 尝试从本地缓存取，如果本地缓存没有则调supplier计算，
                // 这里相当于击穿了，但是此处的get方法有锁，很多超过maxWaitCount的线程，会在一把锁上等待
                // 缓存有超时时间，当超时的时候需要去主动调一次supplier
                V data = loadingCache.get(key, supplier::get);

                // 如果计算得到了结果，去唤醒被阻塞的其他线程
                if (!futureTask.isDone()) {
                    futureTask.set(data);
                }

                return new DataHolder<>(data, true);
            } catch (ExecutionException e) {
                logger.error("通过本地缓存加载数据失败!", e);
                return new DataHolder<>(e, false);
            }
        } else {
            try {
                V data = futureTask.get(timeout, TimeUnit.MILLISECONDS);
                return new DataHolder<>(data, false);
            } catch (InterruptedException e) {
                logger.error("", e);
                Thread.currentThread().interrupt();
                return new DataHolder<>(e, false);
            } catch (ExecutionException e) {
                logger.error("", e);
                return new DataHolder<>(e.getCause(), false);
            } catch (TimeoutException e) {
                logger.error("等待其他线程加载数据超时", e);
                if (loadBySelfAfterTimeout) {
                    try {
                        V data = supplier.get();
                        return new DataHolder<>(data, true);
                    } catch (Exception ee) {
                        return new DataHolder<>(ee, true);
                    }
                } else {
                    return new DataHolder<>(e, false);
                }
            }
        }
    }
}