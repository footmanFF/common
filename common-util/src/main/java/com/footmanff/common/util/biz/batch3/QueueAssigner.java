package com.footmanff.common.util.biz.batch3;

@FunctionalInterface
public interface QueueAssigner {
    
    int assign(String key, int limit);
    
}
