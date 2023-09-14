package com.footmanff.common.util.biz.batch;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Bucket<T, R> {

    private String id = UUID.randomUUID().toString();

    private List<T> taskList = new ArrayList<>();

    private String key;

    private String cacheKey;

    private ReentrantLock lock;

    private Condition mainThreadCondition;

    private volatile boolean mainThreadSignal;

    private List<Pair<ReentrantLock, Condition>> subThreadConditionList = new ArrayList<>();

    private R result;

    private Throwable exp;

    private boolean invalidated;

    public String getId() {
        return id;
    }

    public boolean isInvalidated() {
        return invalidated;
    }

    public void setInvalidated(boolean invalidated) {
        this.invalidated = invalidated;
    }

    public synchronized void addTask(T task) {
        taskList.add(task);
    }

    public synchronized void addSubThreadCondition(Pair<ReentrantLock, Condition> c) {
        subThreadConditionList.add(c);
    }

    public synchronized List<Pair<ReentrantLock, Condition>> getSubThreadConditionList() {
        return subThreadConditionList;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Throwable getExp() {
        return exp;
    }

    public void setExp(Throwable exp) {
        this.exp = exp;
    }

    public synchronized List<T> getTaskList() {
        return taskList;
    }

    public R getResult() {
        return result;
    }

    public void setResult(R result) {
        this.result = result;
    }

    public Condition getMainThreadCondition() {
        return mainThreadCondition;
    }

    public void setMainThreadCondition(Condition mainThreadCondition) {
        this.mainThreadCondition = mainThreadCondition;
    }

    public void setMainThreadSignal(boolean mainThreadSignal) {
        this.mainThreadSignal = mainThreadSignal;
    }

    public boolean isMainThreadSignal() {
        return mainThreadSignal;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public Bucket<T, R> setLock(ReentrantLock lock) {
        this.lock = lock;
        return this;
    }

    public boolean isFinished() {
        return result != null || exp != null;
    }

}
