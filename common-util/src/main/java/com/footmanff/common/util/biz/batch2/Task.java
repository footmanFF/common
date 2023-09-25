package com.footmanff.common.util.biz.batch2;

import com.footmanff.common.util.biz.CountableFutureTask;
import lombok.Data;

@Data
public class Task<T, R> {

    private CountableFutureTask<Object> countableFutureTask;
    
    private String key;
    
    private T task;

}
