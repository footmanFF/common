package com.footmanff.common.util.biz.batch;

import lombok.Data;

import java.util.List;

public class BatchExecParam<T> {

    private final String execKey;
    
    private final List<T> taskList;
    
    private final String cacheKey;
    
    private String bucketId;

    public BatchExecParam(String execKey, List<T> taskList, String cacheKey) {
        this.execKey = execKey;
        this.taskList = taskList;
        this.cacheKey = cacheKey;
    }

    public String getExecKey() {
        return execKey;
    }

    public List<T> getTaskList() {
        return taskList;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }
}
