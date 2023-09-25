package com.footmanff.common.util.biz.batch3;

import java.util.List;

public interface DataConsumer<T> {

    void consume(List<T> data);

    void onError(List<T> data, Throwable t);

}
