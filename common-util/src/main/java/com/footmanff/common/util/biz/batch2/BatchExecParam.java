package com.footmanff.common.util.biz.batch2;

import lombok.Data;

import java.util.List;

@Data
public class BatchExecParam<T> {
    
    private final String key;
    
    private final List<T> taskList;
    
}