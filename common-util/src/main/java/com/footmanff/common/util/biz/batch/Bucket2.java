package com.footmanff.common.util.biz.batch;

import cn.hutool.extra.spring.SpringUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

@Data
public class Bucket2<T, R> {

    private String id = UUID.randomUUID().toString();

    private volatile ReentrantLock mainLock;

    private volatile Condition mainCondition;

    private List<SubBucket> subBucketList;

    private List<T> taskList;

    private R result;

    private Throwable exp;

    /**
     * 是否已经批量过了，如果是，则不能有新的任务加进来
     */
    private boolean processed = false;

    public Bucket2() {
        subBucketList = new ArrayList<>(8);
        mainLock = new ReentrantLock();
        mainCondition = mainLock.newCondition();
        taskList = new ArrayList<>();
    }

    /**
     * 添加任务到taskList，并且添加子锁到subBucketList
     * <p/>
     * 加锁，避免并发时，对List的add操作并发问题
     *
     * @return false: bucket已经执行过批处理，已经不可用
     */
    public synchronized boolean init(T task, ReentrantLock subLock, Condition subCondition) {
        if (processed) {
            return false;
        }
        if (subLock != null) {
            SubBucket subBucket = new SubBucket();
            subBucket.setLock(subLock);
            subBucket.setCondition(subCondition);

            subBucketList.add(subBucket);
        }
        taskList.add(task);

        return true;
    }

    /**
     * 加锁，避免批量处理的同时，还有其他线程在往List中加数据
     */
    public synchronized void processTask(String key, Function<BatchExecParam<T>, R> bachFunc) {
        processed = true;

        BatchExecParam<T> batchExecParam = new BatchExecParam<>(key, taskList, null);
        batchExecParam.setBucketId(id);
        BatchExecResult<R> batchExecResult = exec(bachFunc, batchExecParam);

        if (batchExecResult.getResult() != null) {
            this.result = batchExecResult.getResult();
        } else {
            this.exp = batchExecResult.getExp();
        }
    }

    private BatchExecResult<R> exec(Function<BatchExecParam<T>, R> bachFunc, BatchExecParam<T> batchExecParam) {
        try {
            R result = bachFunc.apply(batchExecParam);
            return new BatchExecResult<>(result);
        } catch (Throwable e) {
            return new BatchExecResult<>(e);
        }
    }

}