package com.footmanff.common.util.collection;

import org.apache.commons.lang3.StringUtils;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author footmanff on 2020/9/22.
 */
public final class CollectionUtil {

    public static <T> List<T> list(T... element) {
        List<T> list = new ArrayList<>();
        if (element == null) {
            return list;
        }
        for (T t : element) {
            if (t == null) {
                continue;
            }
            list.add(t);
        }
        return list;
    }

    /**
     * 过滤list中的null
     */
    public static <T> List<T> filterNull(List<T> list) {
        if (list == null) {
            return list;
        }
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 过滤出满足predicate条件的结果，并收集到list
     */
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        if (list == null) {
            return list;
        }
        return list.stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * 过滤出满足predicate条件的结果，并收集到list
     */
    public static <T> T filterFirst(List<T> list, Predicate<T> predicate) {
        if (list == null) {
            return null;
        }
        for (T t : list) {
            if (t == null) {
                continue;
            }
            if (predicate.test(t)) {
                return t;
            }
        }
        return null;
    }

    /**
     * map 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * map 是否为非空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    /**
     * collection 是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * collection 是否为空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * 收集列表元素的某个方法的执行结果，并组成一个新列表返回
     *
     * @param list     原始列表
     * @param function 原始列表元素的方法
     * @param <T>      原始列表元素
     * @param <R>      原始列表元素方法返回结果
     * @return 新列表
     */
    public static <T, R> List<R> collect(Collection<T> list, Function<T, R> function) {
        if (function == null) {
            throw new IllegalArgumentException("function不得为null");
        }
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        List<R> result = new ArrayList<>();
        for (T t : list) {
            R r = function.apply(t);
            if (r == null) {
                continue;
            }
            result.add(r);
        }
        return result;
    }

    /**
     * 根据条件收集收集新的map
     *
     * @param map       map
     * @param condition 条件
     * @param <K>       键类型
     * @param <V>       值类型
     * @return 收集结果
     */
    public static <K, V> Map<K, V> collect(Map<K, V> map, Predicate<K> condition) {
        if (isEmpty(map)) {
            return new HashMap<>();
        }
        if (condition == null) {
            throw new IllegalArgumentException("condition不得为null");
        }
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();

            if (condition.test(key)) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 收集列表元素的某个方法的执行结果，并组成一个新set返回
     *
     * @param list     原始列表
     * @param function 原始列表元素的方法
     * @param <T>      原始列表元素
     * @param <R>      原始列表元素方法返回结果
     * @return set 结果
     */
    public static <T, R> Set<R> collectToSet(Collection<T> list, Function<T, R> function) {
        if (function == null) {
            throw new IllegalArgumentException("function不得为null");
        }
        if (isEmpty(list)) {
            return new HashSet<>();
        }
        Set<R> result = new HashSet<>();
        for (T t : list) {
            R r = function.apply(t);
            if (r == null) {
                continue;
            }
            result.add(r);
        }
        return result;
    }

    /**
     * 打平收集
     */
    public static <T> List<T> flatCollect(Collection<Collection<T>> col) {
        List<T> result = new ArrayList<>();
        if (col == null) {
            return result;
        }
        for (Collection<T> c : col) {
            if (c == null) {
                continue;
            }
            result.addAll(c);
        }
        return result;
    }

    /**
     * 打平收集
     */
    public static <K, T extends Collection<K>> List<K> flatCollect(Map<?, T> col) {
        List<K> result = new ArrayList<>();
        if (col == null) {
            return result;
        }
        for (Map.Entry<?, T> entry : col.entrySet()) {
            T c = entry.getValue();
            if (c == null) {
                continue;
            }
            result.addAll(c);
        }
        return result;
    }

    /**
     * 收集列表元素的某个方法的执行结果，并组成一个新列表返回
     *
     * @param list     原始列表
     * @param function 原始列表元素的方法
     * @param <T>      原始列表元素
     * @param <R>      原始列表元素方法返回结果
     * @return 新列表
     */
    public static <T, R> List<R> flatCollect(Collection<T> list, Function<T, Collection<R>> function) {
        if (function == null) {
            throw new IllegalArgumentException("function不得为null");
        }
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        List<R> result = new ArrayList<>();
        for (T t : list) {
            Collection<R> r = function.apply(t);
            if (r == null) {
                continue;
            }
            result.addAll(r);
        }
        return result;
    }

    /**
     * 根据列表元素的某个方法的方法执行group操作
     *
     * @param list     原始列表
     * @param function 原始列表元素的方法
     * @param <T>      原始列表元素
     * @param <K>      原始列表元素方法返回结果
     * @return group结果
     */
    public static <T, K> Map<K, List<T>> group(List<T> list, Function<T, K> function) {
        if (function == null) {
            throw new IllegalArgumentException("function不得为null");
        }
        if (isEmpty(list)) {
            return new HashMap<>();
        }
        Map<K, List<T>> result = new HashMap<>();
        for (T t : list) {
            if (t == null) {
                continue;
            }
            K key = function.apply(t);
            if (key == null) {
                continue;
            }
            List<T> groupList = result.computeIfAbsent(key, k -> new ArrayList<>(2));
            groupList.add(t);
        }
        return result;
    }

    /**
     * 根据列表元素的某个方法的方法执行group操作
     *
     * @param list      原始列表
     * @param keyFunc   key转换函数
     * @param valueFunc value转换函数
     * @param <T>       原始列表元素
     * @param <K>       原始列表元素方法返回结果
     * @param <R>       转换类型
     * @return group结果
     */
    public static <T, K, R> Map<K, List<R>> group(List<T> list, Function<T, K> keyFunc, Function<T, R> valueFunc) {
        if (keyFunc == null || valueFunc == null) {
            throw new IllegalArgumentException("function不得为null");
        }
        if (isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.groupingBy(keyFunc, Collectors.mapping(valueFunc, Collectors.toList())));
    }

    /**
     * 根据列表元素的某个方法的方法执行map操作
     *
     * @param list     原始列表
     * @param function 原始列表元素的方法
     * @param <T>      原始列表元素
     * @param <K>      原始列表元素方法返回结果
     * @return map结果
     */
    public static <T, K> Map<K, T> map(Collection<T> list, Function<T, K> function) {
        if (function == null) {
            throw new IllegalArgumentException("function不得为null");
        }
        if (isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.toMap(function, e -> e, (e1, e2) -> e1));
    }

    /**
     * 列表元素的某个func的结果作为key
     * 列表元素的某个func的结果作为value
     *
     * @param list      原始列表
     * @param keyFunc   生成key的func
     * @param valueFunc 生成value的func
     * @param <T>       数组类型
     * @param <K>       map的key类型
     * @param <R>       map的value类型
     * @return map结果
     */
    public static <T, K, R> Map<K, R> map(Collection<T> list, Function<T, K> keyFunc, Function<T, R> valueFunc) {
        if (keyFunc == null) {
            throw new IllegalArgumentException("keyFunc不得为null");
        }
        if (isEmpty(list)) {
            return new HashMap<>();
        }

        HashMap<K, R> krHashMap = new HashMap<>();

        for (T e : list) {
            if (e == null) {
                continue;
            }
            K k = keyFunc.apply(e);
            R v = valueFunc.apply(e);
            if (k != null && v != null) {
                krHashMap.put(k, v);
            }
        }

        return krHashMap;
    }

    /**
     * 列表元素的某个func的结果作为key，对列表去重
     *
     * @param list    原始列表
     * @param keyFunc 生成key的func
     * @param <T>     列表元素类型
     * @return 去重以后的结果
     */
    public static <T> List<T> distinct(List<T> list, Function<T, ?> keyFunc) {
        if (keyFunc == null) {
            throw new IllegalArgumentException("keyFunc不得为null");
        }
        if (isEmpty(list)) {
            return list;
        }
        try {
            return list.stream().filter(distinctByKey(keyFunc)).collect(Collectors.toList());
        } catch (Throwable e) {
            throw new RuntimeException("针对列表元素去重异常", e);
        }
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = new HashSet<>(16);
        return t -> {
            Object obj = keyExtractor.apply(t);
            if (obj == null) {
                throw new IllegalArgumentException("keyFunc返回结果不得为null");
            }
            return seen.add(obj);
        };
    }

    /**
     * 求和针对list的元素做求和
     *
     * @param list 求和源数据
     * @param func 计算求和的参数
     * @return 求和结果
     */
    public static <T> BigDecimal sum(Collection<T> list, Function<T, BigDecimal> func) {
        return sum(list, e -> true, func);
    }

    /**
     * 求和针对list的元素做求和
     *
     * @param list      求和源数据
     * @param condition 是否需要求和的条件
     * @param func      计算求和的参数
     * @return 求和结果
     */
    public static <T> BigDecimal sum(Collection<T> list, Predicate<T> condition, Function<T, BigDecimal> func) {
        if (func == null || condition == null) {
            throw new IllegalArgumentException("condition或func不得为null");
        }
        if (isEmpty(list)) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (T t : list) {
            if (t == null) {
                continue;
            }
            if (!condition.test(t)) {
                continue;
            }
            BigDecimal r = func.apply(t);
            if (r == null) {
                continue;
            }
            sum = sum.add(r);
        }
        return sum;
    }

    /**
     * 求和针对map的value做求和
     *
     * @param map       求和map
     * @param condition 条件
     * @return 求和结果
     */
    public static <K> BigDecimal sum(Map<K, BigDecimal> map, Predicate<K> condition) {
        if (condition == null) {
            throw new IllegalArgumentException("condition不得为null");
        }
        if (isEmpty(map)) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<K, BigDecimal> entry : map.entrySet()) {
            K key = entry.getKey();
            BigDecimal value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (!condition.test(key)) {
                continue;
            }
            sum = sum.add(value);
        }
        return sum;
    }

    /**
     * 是否包含
     */
    public static <T> boolean contain(Collection<T> list, Predicate<T> predicate) {
        if (list == null || predicate == null) {
            return false;
        }
        for (T t : list) {
            if (predicate.test(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 连接map中k-v为一个字符串，k和v之间以s间隔，每组k-v之间以separator间隔
     *
     * @param map       字符串映射
     * @param s         k和v之间的间隔字符
     * @param separator 每组k-v之间的间隔字符
     * @return 连接以后的字符串
     */
    public static String join(Map<String, String> map, String s, String separator) {
        if (isEmpty(map)) {
            return "";
        }
        StringBuilder info = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
                continue;
            }
            if (info.length() != 0) {
                info.append(separator);
            }
            info.append(key);
            info.append(s);
            info.append(value);
        }
        return info.toString();
    }

    /**
     * 是否集合的所有元素都符合predicate
     *
     * @param collection 集合
     * @param predicate  条件
     * @return true: 集合为空，或所有集合元素符合predicate
     */
    public static <T> boolean allMatch(Collection<T> collection, Predicate<T> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("predicate不得为null");
        }
        if (isEmpty(collection)) {
            return true;
        }
        return collection.stream().allMatch(predicate);
    }

    /**
     * 合并多个集合
     *
     * @param lists 待合并的集合数组
     * @param <T>   数组元素类型
     * @return 合并后的集合
     */
    @SafeVarargs
    public static <T> List<T> merge(List<T>... lists) {
        List<T> list = new ArrayList<>();
        for (List<T> ts : lists) {
            if (isEmpty(ts)) {
                continue;
            }
            list.addAll(ts);
        }
        return list;
    }

    /**
     * 是否两个集合元素数量一致
     */
    public static boolean isSizeEqual(Collection<?> c1, Collection<?> c2) {
        int sizeC1 = c1 == null ? 0 : c1.size();
        int sizeC2 = c2 == null ? 0 : c2.size();
        return sizeC1 == sizeC2;
    }

}
