package com.ck.function.serializable;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 可序列化的 Function 接口
 *
 * @param <T>
 * @param <R>
 */
public interface BaseFunction<T, R> extends Function<T, R>, Serializable {
}
