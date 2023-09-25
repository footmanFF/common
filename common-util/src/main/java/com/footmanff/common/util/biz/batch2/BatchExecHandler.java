package com.footmanff.common.util.biz.batch2;

public interface BatchExecHandler<T, R> {

    R process(BatchExecParam<T> param);

}
