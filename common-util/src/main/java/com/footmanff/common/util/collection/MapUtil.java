package com.footmanff.common.util.collection;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {

    public static <K, V> Map<K, V> newHashMap(Object... args) {
        Map<K, V> result = new HashMap<>();
        if (args == null || args.length == 0) {
            return result;
        }
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("数量必须是偶数个");
        }
        for (int i = 0; i < args.length; i = i + 2) {
            K k = (K) args[i];
            V v = (V) args[i + 1];
            result.put(k, v);
        }
        return result;
    }

}
