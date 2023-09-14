package com.footmanff.common.util.biz.batch;

import lombok.Data;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class SubBucket {

    private ReentrantLock lock;
    private Condition condition;

}
