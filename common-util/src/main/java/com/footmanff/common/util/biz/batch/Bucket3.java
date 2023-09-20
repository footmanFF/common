package com.footmanff.common.util.biz.batch;

import lombok.Data;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
public class Bucket3<T, R> {
    
    private String cacheKey;
    
    private String id = UUID.randomUUID().toString();
    
    private volatile Thread mainThread;
    
    private volatile ConcurrentLinkedQueue<Thread> subThreadList;
    
    private volatile ConcurrentLinkedQueue<T> taskList;

    private volatile R result;

    private volatile Throwable exp;

    /**
     * 是否已经批量过了，如果是，则不能有新的任务加进来
     */
    private boolean processed = false;

    public Bucket3() {
        taskList = new ConcurrentLinkedQueue<>();
        subThreadList = new ConcurrentLinkedQueue<>();
    }
    
}