package com.footmanff.common.util;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author footmanff on 2021/4/13.
 */
public class BigDecimalUtil {
    
    public static BigDecimal nullToZero(BigDecimal bd) {
        if (bd == null) {
            return BigDecimal.ZERO;
        }
        return bd;
    }

    /**
     * 对多个BigDecimal求和
     */
    public static BigDecimal sum(BigDecimal... bigDecimals) {
        if (bigDecimals == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal bigDecimal : bigDecimals) {
            if (bigDecimal == null) {
                continue;
            }
            result = result.add(bigDecimal);
        }
        return result;
    }

    /**
     * 是否小于0，或等于0
     */
    public static boolean isNegativeOrZero(BigDecimal bigDecimal) {
        return bigDecimal == null || bigDecimal.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * 是否小于0，或为null
     */
    public static boolean isNullOrNegative(BigDecimal bigDecimal) {
        return bigDecimal == null || bigDecimal.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 是否小于0
     */
    public static boolean isNotNullNegativeOrZero(BigDecimal bigDecimal) {
        return bigDecimal != null && bigDecimal.compareTo(BigDecimal.ZERO) <= 0;
    }
    
    public static boolean isPositive(BigDecimal bigDecimal) {
        return bigDecimal != null && bigDecimal.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * null转0，负数转0
     */
    public static BigDecimal negativeToZero(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return BigDecimal.ZERO;
        }
        if (bigDecimal.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO; 
        }
        return bigDecimal;
    }

    /**
     * 超过某个值，则取一个默认值
     */
    public static BigDecimal maxToThreshold(BigDecimal bigDecimal, BigDecimal threshold) {
        if (bigDecimal == null) {
            return null;
        }
        if (bigDecimal.compareTo(threshold) > 0) {
            return threshold;
        }
        return bigDecimal;
    }

    /**
     * 计算较小的BigDecimal
     */
    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) <= 0) {
            return a;
        } else {
            return b;
        }
    }

    /**
     * 计算较大的BigDecimal
     */
    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) >= 0) {
            return a;
        } else {
            return b;
        }
    }

    /**
     * 是否first等second
     */
    public static boolean eq(BigDecimal first, BigDecimal second) {
        return Objects.nonNull(first) && Objects.nonNull(second) && first.compareTo(second) == 0;
    }

    /**
     * 是否first大于second
     */
    public static boolean gt(BigDecimal first, BigDecimal second) {
        return Objects.nonNull(first) && Objects.nonNull(second) && first.compareTo(second) > 0;
    }

    /**
     * 是否first大于second
     */
    public static boolean ge(BigDecimal first, BigDecimal second) {
        return Objects.nonNull(first) && Objects.nonNull(second) && first.compareTo(second) >= 0;
    }

    /**
     * 是否first小于second
     */
    public static boolean lt(BigDecimal first, BigDecimal second) {
        return Objects.nonNull(first) && Objects.nonNull(second) && first.compareTo(second) < 0;
    }

    /**
     * 是否first小于等于second
     */
    public static boolean le(BigDecimal first, BigDecimal second) {
        return Objects.nonNull(first) && Objects.nonNull(second) && first.compareTo(second) <= 0;
    }

    /**
     * 是否等于0
     */
    public static boolean eqZero(BigDecimal decimal) {
        return Objects.nonNull(decimal) && decimal.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 是否大于0
     */
    public static boolean gtZero(BigDecimal decimal) {
        return Objects.nonNull(decimal) && decimal.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 是否大于等于0
     */
    public static boolean geZero(BigDecimal decimal) {
        return Objects.nonNull(decimal) && decimal.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * 是否小于0
     */
    public static boolean ltZero(BigDecimal decimal) {
        return Objects.nonNull(decimal) && decimal.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 是否小于等于0
     */
    public static boolean leZero(BigDecimal decimal) {
        return Objects.nonNull(decimal) && decimal.compareTo(BigDecimal.ZERO) <= 0;
    }
    
    public static boolean isNullOrLeZero(BigDecimal decimal) {
        return decimal == null || decimal.compareTo(BigDecimal.ZERO) <= 0;
    }

    public static BigDecimal subtractToZero(BigDecimal first, BigDecimal second) {
        BigDecimal sub = first.subtract(second);
        if (sub.compareTo(BigDecimal.ZERO) < 0){
            return BigDecimal.ZERO;
        }
        return sub;
    }

    public static BigDecimal negativeZeroToDefault(BigDecimal bd, BigDecimal d) {
        if (bd == null || bd.compareTo(BigDecimal.ZERO) <= 0) {
            return d;
        }
        return bd;
    }
    
    public static boolean isScaleLimitTwo(BigDecimal bd) {
        bd = bd.stripTrailingZeros();
        return bd.scale() <= 2;
    }

}
