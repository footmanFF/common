package com.footmanff.common.util.biz;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RetryUtil {

    /**
     * 当业务逻辑抛出异常时候重试
     */
    private final static Function<Throwable, Boolean> RETRY_WHEN_EXCEPTION = (t) -> {
        // 无异常，不需要重试
        if (t == null) {
            return false;
        } else {
            return true;
        }
    };

    /**
     * 业务逻辑执行重试工具，当售后单更新版本冲突时重试
     *
     * @param logPrefix  日志信息前缀
     * @param retryCount 重试次数
     * @param runnable   执行业务逻辑的函数
     */
    public static void retryWhenVersionConflict(String logPrefix, int retryCount, Runnable runnable) {
        retry(logPrefix, retryCount, runnable, RETRY_WHEN_EXCEPTION);
    }

    /**
     * 业务逻辑执行重试工具，当售后单更新版本冲突时重试
     *
     * @param logPrefix  日志信息前缀
     * @param retryCount 重试次数
     * @param callable   执行业务逻辑的函数
     */
    public static <R> R retryWhenVersionConflict(String logPrefix, int retryCount, Callable<R> callable) {
        return retry(logPrefix, retryCount, callable, RETRY_WHEN_EXCEPTION);
    }

    /**
     * 业务逻辑执行重试工具，当抛异常时重试
     *
     * @param logPrefix  日志信息前缀
     * @param retryCount 重试次数
     * @param runnable   执行业务逻辑的函数
     */
    public static void retryWhenException(String logPrefix, int retryCount, Runnable runnable) {
        retry(logPrefix, retryCount, runnable, RETRY_WHEN_EXCEPTION);
    }

    /**
     * 业务逻辑执行重试工具
     *
     * @param logPrefix  日志信息前缀
     * @param retryCount 重试次数
     * @param runnable   执行业务逻辑的函数
     * @param retryFunc  控制是否需要重试函数，返回true则需要重试
     */
    public static void retry(String logPrefix, int retryCount, Runnable runnable,
                             Function<Throwable, Boolean> retryFunc) {
        BiFunction<Void, Throwable, Boolean> retryBiFunc = (v, t) -> retryFunc.apply(t);
        Callable<Void> callable = () -> {
            runnable.run();
            return null;
        };
        retry(logPrefix, retryCount, callable, retryBiFunc);
    }

    /**
     * 业务逻辑执行重试工具
     *
     * @param logPrefix  日志信息前缀
     * @param retryCount 重试次数
     * @param callable   执行业务逻辑的函数
     * @param retryFunc  控制是否需要重试函数，返回true则需要重试
     */
    public static <R> R retry(String logPrefix, int retryCount, Callable<R> callable,
                              Function<Throwable, Boolean> retryFunc) {
        BiFunction<R, Throwable, Boolean> retryBiFunc = (v, t) -> retryFunc.apply(t);
        return retry(logPrefix, retryCount, callable, retryBiFunc);
    }

    /**
     * 业务逻辑执行重试工具
     *
     * @param logPrefix  日志信息前缀
     * @param retryCount 重试次数
     * @param callable   执行业务逻辑的函数
     * @param retryFunc  控制是否需要重试函数，返回true则需要重试
     * @param <R>        业务逻辑函数返回结果
     * @return 最后一次调用业务逻辑函数的的结果
     */
    public static <R> R retry(String logPrefix, int retryCount, Callable<R> callable,
                              BiFunction<R, Throwable, Boolean> retryFunc) {
        if (retryCount <= 0) {
            throw new IllegalArgumentException("重试次数不得小于0");
        }
        if (callable == null) {
            throw new IllegalArgumentException("执行任务不得为null");
        }
        R lastResult = null;
        for (int i = 1; i <= retryCount; i++) {
            R result = null;
            Throwable throwable = null;
            try {
                result = callable.call();
            } catch (Throwable e) {
                throwable = e;
            }
            Boolean needRetry = false;
            if (retryFunc != null) {
                needRetry = retryFunc.apply(result, throwable);
            }
            if (needRetry != null && needRetry) {
                if (i >= retryCount) {
                    lastResult = getLastResultAndCheckException(throwable, result);
                }
            } else {
                // 方法结束，返回结果或抛异常
                lastResult = getLastResultAndCheckException(throwable, result);
                // 不需要重试，直接break
                break;
            }
        }
        return lastResult;
    }

    private static <R> R getLastResultAndCheckException(Throwable throwable, R result) {
        if (throwable != null) {
            throw new RuntimeException(throwable);
        } else {
            return result;
        }
    }

}
