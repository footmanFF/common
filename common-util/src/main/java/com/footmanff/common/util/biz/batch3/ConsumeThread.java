package com.footmanff.common.util.biz.batch3;

import com.footmanff.common.util.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

public class ConsumeThread<T> extends Thread {

    private final DataConsumer<T> dataConsumer;

    private final List<DataQueue<T>> dataBuffers;

    private volatile boolean running;

    private final long consumeCycle;

    private final Integer consumeBathLimit;

    public ConsumeThread(String threadName, DataConsumer<T> dataConsumer, long consumeCycle) {
        this(threadName, dataConsumer, consumeCycle, null);
    }

    public ConsumeThread(String threadName, DataConsumer<T> dataConsumer, long consumeCycle, Integer consumeBathLimit) {
        super(threadName);
        this.dataConsumer = dataConsumer;
        this.consumeCycle = consumeCycle;
        this.dataBuffers = new ArrayList<>();
        this.consumeBathLimit = consumeBathLimit;
    }

    public void addDataBuffer(DataQueue<T> dataBuffer) {
        this.dataBuffers.add(dataBuffer);
    }

    @Override
    public void run() {
        running = true;

        final List<T> consumeList = new ArrayList<T>(1500);

        while (running) {
            if (!consume(consumeList)) {
                try {
                    Thread.sleep(consumeCycle);
                } catch (InterruptedException e) {
                }
            }
        }
        consume(consumeList);
    }

    private boolean consume(List<T> dataList) {
        boolean result = false;
        for (DataQueue<T> dataBuffer : dataBuffers) {
            if (consumeBathLimit != null) {
                dataBuffer.obtainData(dataList, consumeBathLimit);
            } else {
                dataBuffer.obtainData(dataList);
            }
            if (CollectionUtil.isNotEmpty(dataList)) {
                try {
                    dataConsumer.consume(dataList);
                } catch (Throwable e) {
                    dataConsumer.onError(dataList, e);
                } finally {
                    dataList.clear();
                }
                result = true;
            }
        }
        return result;
    }

}
