package com.footmanff.common.util.biz;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

public class CountableFutureTask<V> extends FutureTask<V> {

    private final long createdTime;

    private final AtomicInteger waitCount;

    public CountableFutureTask(Callable<V> callable) {
        super(callable);
        this.createdTime = System.currentTimeMillis();
        this.waitCount = new AtomicInteger(0);
    }

    @Override
    public void set(V v) {
        super.set(v);
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public int incrWaitCount() {
        return waitCount.incrementAndGet();
    }

}
