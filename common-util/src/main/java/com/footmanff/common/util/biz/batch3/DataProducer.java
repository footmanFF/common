package com.footmanff.common.util.biz.batch3;

import java.util.concurrent.locks.ReentrantLock;

public class DataProducer<T> {

    private long consumeCycle;

    private final String prefix;

    private final DataQueue<T>[] dataBuffers;

    private ConsumeThread<T>[] consumeThreads;

    private final DataConsumer<T> dataConsumer;

    private final ReentrantLock lock;

    private boolean running;

    private QueueAssigner queueAssigner;

    @SuppressWarnings("NonAtomicVolatileUpdate")
    private volatile int i = 0;

    public DataProducer(int bufferCount, int bufferSize, String threadPrefix, DataConsumer<T> dataConsumer) {
        this(bufferCount, bufferSize, threadPrefix, dataConsumer, 50, null);
        this.queueAssigner = new QueueAssigner() {
            @Override
            public int assign(String key, int limit) {
                return Math.abs(i++ % limit);
            }
        };
    }

    public DataProducer(int bufferCount, int bufferSize, String threadPrefix, DataConsumer<T> dataConsumer, long consumeCycle, QueueAssigner queueAssigner) {
        dataBuffers = new DataQueue[bufferCount];
        for (int i = 0; i < bufferCount; i++) {
            dataBuffers[i] = new DataQueue(bufferSize);
        }
        this.dataConsumer = dataConsumer;
        this.prefix = threadPrefix;
        this.lock = new ReentrantLock();
        this.queueAssigner = queueAssigner;
        this.consumeCycle = consumeCycle;
    }

    /**
     * 生产一条数据
     */
    public boolean produce(String key, T data, int retryCountDown) {
        int index = queueAssigner.assign(key, dataBuffers.length);

        for (; retryCountDown > 0; retryCountDown--) {
            if (dataBuffers[index].save(data)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生产一条数据
     */
    public boolean produce(String key, T data) {
        int index = queueAssigner.assign(key, dataBuffers.length);
        return dataBuffers[index].save(data);
    }

    /**
     * 启动生产者/消费者
     */
    public void start(int threadNum, Integer consumeBathLimit) {
        if (running) {
            return;
        }
        lock.lock();
        try {
            consumeThreads = new ConsumeThread[threadNum];
            for (int j = 0; j < threadNum; j++) {
                consumeThreads[j] = new ConsumeThread<T>("DataProducer-Consumer-" + prefix + j, dataConsumer, consumeCycle, consumeBathLimit);
            }
            for (int idx = 0; idx < dataBuffers.length; idx++) {
                int consumerIndex = idx % consumeThreads.length;
                consumeThreads[consumerIndex].addDataBuffer(dataBuffers[idx]);
            }
            for (ConsumeThread<T> consumeThread : consumeThreads) {
                consumeThread.start();
            }
            running = true;
        } finally {
            lock.unlock();
        }
    }

    public boolean isRunning() {
        return running;
    }

}
