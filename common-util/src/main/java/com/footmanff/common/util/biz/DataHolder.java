package com.footmanff.common.util.biz;

public class DataHolder<T> {

    private T data;
    private Throwable exception;
    private final boolean loadBySelf;

    public DataHolder(T data, boolean loadBySelf) {
        this.data = data;
        this.loadBySelf = loadBySelf;
    }

    public DataHolder(Throwable exception, boolean loadBySelf) {
        this.exception = exception;
        this.loadBySelf = loadBySelf;
    }

    public T getData() {
        return data;
    }

    public boolean isLoadBySelf() {
        return loadBySelf;
    }

    public Throwable getException() {
        return exception;
    }

    /**
     * true: 正常执行并拿到结果  false: 异常，exception是异常对象
     */
    public boolean isSuccess() {
        return exception == null;
    }
}
