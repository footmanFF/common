package com.footmanff.common.util.current.future;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.footmanff.common.util.current.queue.ResizableCapacityLinkedBlockIngQueue;
import com.footmanff.common.util.current.pool.TraceThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class CompletableFutureUtil {

    private final static int DEFAULT_CORE_POOL_SIZE = 400;

    private final static int DEFAULT_MAX_POOL_SIZE = 1024;

    private final static long DEFAULT_KEEP_ALIVE_TIME = 5L;

    private final static int DEFAULT_QUEUE_CAPACITY = 200;

    private final ExecutorService executorServiceTtlWrapper;

    private final ThreadPoolExecutor executor;

    public CompletableFutureUtil() {
        this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_POOL_SIZE, DEFAULT_QUEUE_CAPACITY);
    }

    public CompletableFutureUtil(int core, int max, int queueCapacity) {
        executor = new TraceThreadPoolExecutor(
                core,
                max,
                DEFAULT_KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ResizableCapacityLinkedBlockIngQueue<>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executorServiceTtlWrapper = TtlExecutors.getTtlExecutorService(executor);
    }

    public ThreadPoolExecutor getExecutor() {
        return this.executor;
    }

    public ExecutorService getExecutorServiceTtlWrapper() {
        return this.executorServiceTtlWrapper;
    }

    public void setPoolSize(int corePoolSize, int maxPoolSize, int queueCapacity) {
        if (corePoolSize <= 0) {
            throw new IllegalArgumentException("核心线程数需大于0");
        }
        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("最大线程数需大于0");
        }
        if (queueCapacity >= 0) {
            throw new IllegalArgumentException("阻塞队列大小需大于0");
        }
        if (corePoolSize < maxPoolSize) {
            maxPoolSize = corePoolSize;
        }
        executor.setCorePoolSize(corePoolSize);
        executor.setMaximumPoolSize(maxPoolSize);

        BlockingQueue<Runnable> blockingQueue = executor.getQueue();
        if (blockingQueue.size() != queueCapacity) {
            ((ResizableCapacityLinkedBlockIngQueue<Runnable>) blockingQueue).setCapacity(queueCapacity);
        }
    }

    public CompletableFuture<?> composed(Runnable... runnableList) {
        CompletableFuture<?>[] completableFutures = new CompletableFuture[runnableList.length];
        for (int i = 0; i < runnableList.length; i++) {
            completableFutures[i] = CompletableFuture.runAsync(runnableList[i], executorServiceTtlWrapper);
        }
        return composed(completableFutures);
    }

    public CompletableFuture<?> composed(ExecutorService executorService, Runnable... runnableList) {
        CompletableFuture<?>[] completableFutures = new CompletableFuture[runnableList.length];
        for (int i = 0; i < runnableList.length; i++) {
            completableFutures[i] = CompletableFuture.runAsync(runnableList[i], executorService);
        }
        return composed(completableFutures);
    }

    public CompletableFuture<?> composed(List<Runnable> runnableList) {
        CompletableFuture<?>[] completableFutures = new CompletableFuture[runnableList.size()];
        for (int i = 0; i < runnableList.size(); i++) {
            completableFutures[i] = CompletableFuture.runAsync(runnableList.get(i), executorServiceTtlWrapper);
        }
        return composed(completableFutures);
    }

    public <T> CompletableFuture<?> composed(List<Supplier<T>> suppliers, Consumer<T> resultConsumer) {
        CompletableFuture<T>[] completableFutures = new CompletableFuture[suppliers.size()];
        for (int i = 0; i < suppliers.size(); i++) {
            completableFutures[i] = CompletableFuture.supplyAsync(suppliers.get(i), executorServiceTtlWrapper);
        }
        return composed(resultConsumer, completableFutures);
    }

    public CompletableFuture<?> composed(List<Runnable> runnableList, Executor executor) {
        CompletableFuture<?>[] completableFutures = new CompletableFuture[runnableList.size()];
        for (int i = 0; i < runnableList.size(); i++) {
            completableFutures[i] = CompletableFuture.runAsync(runnableList.get(i), executor);
        }
        return composed(completableFutures);
    }

    public CompletableFuture<?> composedAllCompleted(Runnable... runnableList) {
        CompletableFuture<?>[] completableFutures = new CompletableFuture[runnableList.length];
        for (int i = 0; i < runnableList.length; i++) {
            completableFutures[i] = CompletableFuture.runAsync(runnableList[i], executorServiceTtlWrapper);
        }
        return composedAllCompleted(completableFutures);
    }

    public CompletableFuture<?> composedAllCompleted(List<Runnable> runnableList) {
        CompletableFuture<?>[] completableFutures = new CompletableFuture[runnableList.size()];
        for (int i = 0; i < runnableList.size(); i++) {
            completableFutures[i] = CompletableFuture.runAsync(runnableList.get(i), executorServiceTtlWrapper);
        }
        return composedAllCompleted(completableFutures);
    }

    /**
     * 并行执行方法,全部完成才算完成
     */
    public CompletableFuture<?> composedAllCompleted(CompletableFuture<?>... completableFutures) {
        // Complete when ALL the underlying futures are completed
        return CompletableFuture.allOf(completableFutures).whenComplete((v, t) -> {
            if (t == null) {
                return;
            }
            if (t instanceof CompletionException) {
                throw (CompletionException) t;
            } else {
                throw new CompletionException(t);
            }
        });
    }

    /**
     * 并行执行方法,保证快速失败,全部完成才算完成
     */
    public CompletableFuture<?> composed(CompletableFuture<?>... completableFutures) {
        // Complete when ALL the underlying futures are completed
        CompletableFuture<?> allComplete = CompletableFuture.allOf(completableFutures);

        // Complete when ANY of the underlying futures are exceptional
        CompletableFuture<?> anyException = new CompletableFuture<>();
        for (CompletableFuture<?> completableFuture : completableFutures) {
            completableFuture.exceptionally((t) -> {
                anyException.completeExceptionally(t);
                return null;
            });
        }

        // Complete when either of the above are satisfied
        return CompletableFuture.anyOf(allComplete, anyException).whenComplete((v, t) -> {
            if (t == null) {
                return;
            }
            if (t instanceof CompletionException) {
                throw (CompletionException) t;
            } else {
                throw new CompletionException(t);
            }
        });
    }

    /**
     * 并行执行方法,完成后消费执行结果,保证快速失败,全部完成才算完成
     */
    public <T> CompletableFuture<?> composed(Consumer<T> resultConsumer, CompletableFuture<T>... completableFutures) {
        // Complete when ALL the underlying futures are completed
        CompletableFuture<?> allComplete = CompletableFuture.allOf(completableFutures);

        // Complete when ANY of the underlying futures are exceptional
        CompletableFuture<?> anyException = new CompletableFuture<>();
        for (CompletableFuture<?> completableFuture : completableFutures) {
            completableFuture.exceptionally((t) -> {
                anyException.completeExceptionally(t);
                return null;
            });
        }

        // Complete when either of the above are satisfied
        return CompletableFuture.anyOf(allComplete, anyException).whenComplete((v, t) -> {
            if (t == null) {
                for (CompletableFuture<T> completableFuture : completableFutures) {
                    resultConsumer.accept(completableFuture.getNow(null));
                }

                return;
            }
            if (t instanceof CompletionException) {
                throw (CompletionException) t;
            } else {
                throw new CompletionException(t);
            }
        });
    }
    
}
