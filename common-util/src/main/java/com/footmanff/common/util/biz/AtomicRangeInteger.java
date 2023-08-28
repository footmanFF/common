package com.footmanff.common.util.biz;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicRangeInteger extends Number implements Serializable {

    private static final long serialVersionUID = -4099792402691141643L;

    /**
     * 不用AtomicInteger是为了优化伪共享问题
     * <a href="https://github.com/apache/skywalking/pull/2930" />
     */
    private final AtomicIntegerArray values;

    // 为了解决伪共享，让前后都预留一些字节，占满一个缓存行
    private static final int VALUE_OFFSET = 15;

    private final int startValue;
    private final int endValue;

    public AtomicRangeInteger(int startValue, int maxValue) {
        this.values = new AtomicIntegerArray(31);
        this.values.set(VALUE_OFFSET, startValue);
        this.startValue = startValue;
        this.endValue = maxValue - 1;
    }

    public final int getAndIncrement() {
        int next;
        do {
            next = this.values.incrementAndGet(VALUE_OFFSET);
            if (next > endValue && this.values.compareAndSet(VALUE_OFFSET, next, startValue)) {
                return endValue;
            }
        } while (next > endValue);

        return next - 1;
    }

    public static void main(String[] args) {
        AtomicRangeInteger atomicRangeInteger = new AtomicRangeInteger(0, 3);

        for (int i = 0; i < 200; i++) {
            System.out.println(atomicRangeInteger.getAndIncrement());
        }
    }

    public final int get() {
        return this.values.get(VALUE_OFFSET);
    }

    @Override
    public int intValue() {
        return this.values.get(VALUE_OFFSET);
    }

    @Override
    public long longValue() {
        return this.values.get(VALUE_OFFSET);
    }

    @Override
    public float floatValue() {
        return this.values.get(VALUE_OFFSET);
    }

    @Override
    public double doubleValue() {
        return this.values.get(VALUE_OFFSET);
    }
}
