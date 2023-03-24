package com.ck.function.serializable;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * 可序列化的 Consumer 接口
 *
 * @param <T>
 */
public interface BaseBiConsumer<T,U> extends BiConsumer<T,U>, Serializable {
}
