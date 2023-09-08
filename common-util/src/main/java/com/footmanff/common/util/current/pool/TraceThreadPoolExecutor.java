package com.footmanff.common.util.current.pool;

import com.footmanff.common.util.collection.CollectionUtil;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.*;

public class TraceThreadPoolExecutor extends ThreadPoolExecutor {

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void execute(Runnable command) {
        // 获取当前线程的MDC上下文
        final Map<String, String> mainMDCContext = MDC.getCopyOfContextMap();

        Runnable task = () -> {
            // 获取异步线程的MDC上下文,任务执行完给他恢复回去,后续可以评估是否必要
            Map<String, String> previousMDCContext = MDC.getCopyOfContextMap();
            if (CollectionUtil.isEmpty(mainMDCContext)) {
                MDC.clear();
            } else {
                MDC.setContextMap(mainMDCContext);
            }
            try {
                command.run();
            } finally {
                // 恢复异步线程之前的MDC上下文
                MDC.setContextMap(previousMDCContext);
            }
        };
        super.execute(task);
    }
}
