package com.footmanff.common.util.biz.batch;

public class BatchExecResult<R> {
    
    private R result;
    
    private Throwable exp;

    public BatchExecResult(R result) {
        this.result = result;
    }

    public BatchExecResult(Throwable t) {
        this.exp = t;
    }

    public R getResult() {
        return result;
    }

    public Throwable getExp() {
        return exp;
    }
}
