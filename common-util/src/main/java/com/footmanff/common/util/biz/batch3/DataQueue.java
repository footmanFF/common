package com.footmanff.common.util.biz.batch3;

import com.footmanff.common.util.biz.AtomicRangeInteger;

import java.util.List;

public class DataQueue<T> {

    private final Object[] queue;

    private final AtomicRangeInteger index;

    public DataQueue(int size) {
        queue = new Object[size];
        index = new AtomicRangeInteger(0, size);
    }

    /**
     * 保存数据到缓存，缓存满则放弃写入
     */
    public boolean save(T data) {
        int i = index.getAndIncrement();
        if (queue[i] != null) {
            return false;
        }
        queue[i] = data;
        return true;
    }

    public void obtainData(List<T> dataList, int limit) {
        int c = 0;
        for (int i = 0; i < queue.length; i++) {
            if (queue[i] != null) {
                dataList.add((T) queue[i]);
                queue[i] = null;
                c ++;
                if (c >= limit) {
                    return;
                }
            }
        }
    }

    public void obtainData(List<T> dataList) {
        for (int i = 0; i < queue.length; i++) {
            if (queue[i] != null) {
                dataList.add((T) queue[i]);
                queue[i] = null;
            }
        }
    }

}
