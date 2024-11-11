package com.footmanff.common.util.calc;

import com.footmanff.common.util.collection.MapUtil;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;

import static org.junit.Assert.*;

public class ApportionUtilTest {

    @Test
    public void apportionByRatio() {
        Map<String, BigDecimal> factorMap = MapUtil.newHashMap("factor1", BigDecimal.valueOf(20.0), "factor2", BigDecimal.valueOf(30.0), "factor3", BigDecimal.valueOf(50.0));
        Map<String, BigDecimal> result = ApportionUtil.apportionByRatio(BigDecimal.valueOf(99), factorMap, Comparator.naturalOrder());
        
        System.out.println(result);
    }

}