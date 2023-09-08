package com.footmanff.common.util.current.queue;

import cn.hutool.core.util.ReflectUtil;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResizableCapacityLinkedBlockIngQueue<E> extends LinkedBlockingQueue<E> {
    private static final long serialVersionUID = -1698765601525537061L;

    public ResizableCapacityLinkedBlockIngQueue(int capacity) {
        super(capacity);
    }

    public synchronized boolean setCapacity(Integer capacity) {
        boolean successFlag = true;
        try {
            int oldCapacity = (int) ReflectUtil.getFieldValue(this, "capacity");
            AtomicInteger count = (AtomicInteger) ReflectUtil.getFieldValue(this, "count");
            int size = count.get();
            ReflectUtil.setFieldValue(this, "capacity", capacity);
            if (capacity > size && size >= oldCapacity) {
                ReflectUtil.invoke(this, "signalNotFull");
            }
        } catch (Exception ex) {
            log.error("动态变更阻塞队列大小失败", ex);
            successFlag = false;
        }
        return successFlag;
    }

}