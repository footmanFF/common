package com.footmanff.common.util.base;

public class Holder<T> {

    private T data;

    private Holder(T data) {
        this.data = data;
    }

    public static <T> Holder<T> of(T t) {
        return new Holder<>(t);
    }

    public T get() {
        return data;
    }

    public void set(T data) {
        this.data = data;
    }
}
