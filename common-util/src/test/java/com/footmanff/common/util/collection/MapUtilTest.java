package com.footmanff.common.util.collection;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class MapUtilTest {

    @Test
    public void newHashMap() {
        Map<String, Integer> map = MapUtil.newHashMap("1", 1, "2", 2, "3", 3);
        assertEquals(3, map.size());

        map = MapUtil.newHashMap("1", new Object());
    }
}