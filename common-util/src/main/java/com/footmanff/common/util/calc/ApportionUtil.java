package com.footmanff.common.util.calc;

import com.footmanff.common.util.BigDecimalUtil;
import com.footmanff.common.util.collection.CollectionUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author footmanff on 2021/5/23.
 */
public class ApportionUtil {

    /**
     * 将input按照factorMap的value的值进行分摊
     * <p>
     * 当输入数包含的成分合计是0时，其他分摊因子直接取最大值返回
     * 比如：
     * 输入数 3，仅包含现金、积分
     * 分摊因子包含，现金-0、积分-0、余额-4
     * 此时因为现金、积分都为0，无法计算比例，会直接取余额为4返回
     *
     * @param input            待分摊数
     * @param inputComposition 输入数包含的成分
     * @param factorMap        分摊因子
     * @param <K>              键类型
     * @param comparator       分摊因子key的排序规则
     * @return 分摊以后的结果
     */
    public static <K> Map<K, BigDecimal> apportionByRatio(BigDecimal input, Set<K> inputComposition, Map<K, BigDecimal> factorMap,
                                                          Comparator<K> comparator) {
        if (isInputContainAllFactor(inputComposition, factorMap)) {
            return apportionByRatio(input, factorMap, comparator);
        } else {
            // 输入数成分【包含】的所有分摊因子
            Map<K, BigDecimal> containedFactorMap = CollectionUtil.collect(factorMap, inputComposition::contains);

            // 根据输入数的成分，分摊出结果
            Map<K, BigDecimal> result = apportionByRatio(input, containedFactorMap, comparator);

            // 输入数成分【包含】的分摊因子之和
            BigDecimal containedFactorValueSum = CollectionUtil.sum(factorMap, inputComposition::contains);

            if (containedFactorValueSum.compareTo(BigDecimal.ZERO) > 0 && input.compareTo(containedFactorValueSum) > 0) {
                throw new IllegalArgumentException("带成分时，input不得大于input所有成分之和");
            }

            // 输入数成分【不包含】的所有分摊因子
            Map<K, BigDecimal> unContainedFactorMap = CollectionUtil.collect(factorMap, key -> !inputComposition.contains(key));

            for (Map.Entry<K, BigDecimal> entry : unContainedFactorMap.entrySet()) {
                K key = entry.getKey();
                BigDecimal value = entry.getValue();

                // 为0的情况，见方法注释的例子
                if (containedFactorValueSum.compareTo(BigDecimal.ZERO) == 0) {
                    result.put(key, value);
                }
                // 不为0的情况，不考虑负数情况，外部校验控制了不会有负数
                else {
                    /*
                     * 分摊结果 = 输入数 * 分摊因子 / 输入数成分分摊因子之和
                     * 比如：
                     *   输入数 3，仅包含现金、积分
                     *   分摊因子包含，现金-1、积分-2、余额-4
                     *   余额分摊结果 = 3 * 4 / (1 + 2) = 4，应为输入的是全额，刚好得出 4
                     */
                    BigDecimal apportion = input.multiply(value).divide(containedFactorValueSum, 2, RoundingMode.DOWN);
                    result.put(key, apportion);
                }
            }
            return result;
        }
    }


    /**
     * 将input按照factorMap的value的值进行分摊
     *
     * @param input      待分摊数
     * @param factorMap  分摊因子
     * @param <K>        键类型
     * @param comparator 分摊因子key的排序规则
     * @return 分摊以后的结果
     */
    public static <K> Map<K, BigDecimal> apportionByRatio(BigDecimal input, Map<K, BigDecimal> factorMap,
                                                          Comparator<K> comparator) {
        if (input == null || factorMap == null) {
            throw new IllegalArgumentException("total、factorMap不得为Null");
        }
        if (input.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("total不得小于0");
        }
        if (factorMap.isEmpty()) {
            return new HashMap<>();
        }
        if (factorMap.size() == 1) {
            Map<K, BigDecimal> result = new HashMap<>();
            result.put(factorMap.keySet().iterator().next(), input);
            return result;
        }
        List<K> sortFactorList = new ArrayList<>(factorMap.keySet());
        if (comparator != null) {
            sortFactorList.sort(comparator);
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (K k : sortFactorList) {
            BigDecimal factor = factorMap.get(k);
            if (factor == null) {
                throw new IllegalArgumentException("factorMap的value不得为null key: " + k);
            }
            if (factor.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("factorMap的value不得为负数 key: " + k);
            }
            sum = sum.add(factor);
        }
        if (sum.compareTo(BigDecimal.ZERO) <= 0) {
            return new HashMap<>();
        }
        if (input.compareTo(sum) > 0) {
            throw new IllegalArgumentException("input不得大于factorMap内value的和");
        }
        if (input.equals(BigDecimal.ZERO)) {
            Map<K, BigDecimal> result = new HashMap<>();
            for (Map.Entry<K, BigDecimal> entry : factorMap.entrySet()) {
                result.put(entry.getKey(), BigDecimal.ZERO);
            }
            return result;
        }
        if (input.equals(sum)) {
            Map<K, BigDecimal> result = new HashMap<>();
            for (Map.Entry<K, BigDecimal> entry : factorMap.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }
        Map<K, BigDecimal> result = new HashMap<>();

        BigDecimal restInput = input;
        int count = 0;
        for (K key : sortFactorList) {
            count++;
            if (count == sortFactorList.size()) {
                break;
            }
            BigDecimal factor = factorMap.get(key);
            BigDecimal apportionValue = factor.multiply(input).divide(sum, 2, RoundingMode.DOWN);

            apportionValue = BigDecimalUtil.min(apportionValue, restInput);

            result.put(key, apportionValue);

            restInput = restInput.subtract(apportionValue);

            if (restInput.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }

        K lastKey = sortFactorList.get(sortFactorList.size() - 1);
        BigDecimal lastFactor = factorMap.get(lastKey);

        if (restInput.compareTo(BigDecimal.ZERO) > 0) {
            if (restInput.compareTo(lastFactor) >= 0) {
                result.put(lastKey, lastFactor);
                restInput = restInput.subtract(lastFactor);
            } else {
                result.put(lastKey, restInput);
                restInput = restInput.subtract(restInput);
            }
        }
        if (restInput.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("算法错误，剩余申请金额小于0 input: " + input + " factorMap: " + factorMap);
        }
        if (restInput.compareTo(BigDecimal.ZERO) > 0) {
            for (K key : sortFactorList) {
                BigDecimal apportionValue = result.get(key);
                BigDecimal factor = factorMap.get(key);
                BigDecimal rest = factor.subtract(apportionValue);
                if (rest.compareTo(restInput) >= 0) {
                    result.put(key, apportionValue.add(restInput));
                    break;
                } else {
                    result.put(key, apportionValue.add(rest));
                    restInput = restInput.subtract(rest);
                }
            }
        }
        return result;
    }

    /**
     * 输入数的成分包含所有的分摊因子
     *
     * @param inputComposition 输入数包含的成分
     * @param factorMap        分摊因子
     * @param <K>              键类型
     * @return true: 输入数的成分包含所有的分摊因子
     */
    private static <K> boolean isInputContainAllFactor(Set<K> inputComposition, Map<K, BigDecimal> factorMap) {
        if (CollectionUtil.isEmpty(inputComposition)) {
            throw new IllegalArgumentException("输入数包含的成分不得为空");
        }
        Set<K> factorKeySet = factorMap.keySet();
        boolean containsAll = inputComposition.containsAll(factorKeySet);
        if (containsAll) {
            return true;
        } else {
            return false;
        }
    }
}