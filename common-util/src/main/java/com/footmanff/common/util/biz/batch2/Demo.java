package com.footmanff.common.util.biz.batch2;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Demo {

    public static void main(String[] args) {
        Task[] array = new Task[100];


        Map<String, BlockingQueue<Task<Integer, String>>> map = new ConcurrentHashMap<>();

        BlockingQueue<Task<Integer, String>> queue = map.computeIfAbsent("", e -> {
            return new ArrayBlockingQueue<>(10000);
        });
        
        queue.offer(new Task<>());
        
        queue.poll();
        
    }

}
