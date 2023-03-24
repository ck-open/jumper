package com.ck.function.serializable;

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * 可序列化的 Function 接口
 *
 * @param <T>
 * @param <R>
 */
public interface BaseBiFunction<T, U, R> extends BiFunction<T, U, R>, Serializable {
}
