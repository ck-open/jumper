package com.ck.function.serializable;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * 可序列化的 Consumer 接口
 *
 * @param <T>
 */
public interface BaseConsumer<T> extends Consumer<T>, Serializable {
}
