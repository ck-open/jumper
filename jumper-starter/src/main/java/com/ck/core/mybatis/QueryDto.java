package com.ck.core.mybatis;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.ck.function.LambdaUtils;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.stream.Stream;

/**
 * 统一查询条件报文体
 *
 * @param <T>
 */
@Data
@Accessors(chain = true)
public class QueryDto<T> {

    /**
     * 分页-单页数量
     */
    private Integer size = 20;

    /**
     * 分页-页码
     */
    private Integer current = 1;

    /**
     * 等条件
     */
    private T eq;

    /**
     * 包含条件
     */
    private T like;

    /**
     * 小于条件
     */
    private T lt;

    /**
     * 大于等于条件
     */
    private T ge;

    /**
     * 其他条件
     */
    private T other;

    /**
     * in条件
     */
    private Map<String, List<?>> in;

    /**
     * 排序字段-升序
     */
    private List<String> orderByAsc;

    /**
     * 排序字段-降序
     */
    private List<String> orderByDesc;

    /**
     * 添加 IN 条件
     *
     * @param name
     * @param val
     * @return
     */
    public QueryDto<T> eq(SFunction<T, ?> name, Object val) {
        in(name, Collections.singletonList(val));
        return this;
    }


    /**
     * 添加 IN 条件
     *
     * @param name
     * @param in
     * @return
     */
    public QueryDto<T> in(SFunction<T, ?> name, List in) {
        in(LambdaUtils.fieldName(name), in);
        return this;
    }

    /**
     * 添加 升序 字段
     *
     * @param name
     * @return
     */
    @SafeVarargs
    public final QueryDto<T> orderByAsc(SFunction<T, ?>... name) {
        if (name != null) {
            Stream.of(name).forEach(i -> orderByAsc(LambdaUtils.fieldName(i)));
        }
        return this;
    }

    /**
     * 添加 降序 字段
     *
     * @param name
     * @return
     */
    @SafeVarargs
    public final QueryDto<T> orderByDesc(SFunction<T, ?>... name) {
        if (name != null) {
            Stream.of(name).forEach(i -> orderByDesc(LambdaUtils.fieldName(i)));
        }
        return this;
    }

    /**
     * 添加 IN 条件
     * 此方法不建议使用  推荐指定 T 泛型 通过 Lambda 函数方式传递参数，避免 字符串 指定 Name 带来的后续代码变动不能及时发现的问题
     *
     * @param name
     * @param in
     * @return
     */
    @Deprecated
    public QueryDto<T> in(String name, List in) {
        if (this.in == null) this.in = new TreeMap<>();

        if (!this.in.containsKey(name)) {
            this.in.put(name, new ArrayList<>());
        }
        this.in.get(name).addAll(in);
        return this;
    }

    /**
     * 添加 升序 字段
     * 此方法不建议使用  推荐指定 T 泛型 通过 Lambda 函数方式传递参数，避免 字符串 指定 Name 带来的后续代码变动不能及时发现的问题
     *
     * @param name
     * @return
     */
    @Deprecated
    public QueryDto<T> orderByAsc(String... name) {
        if (this.orderByAsc == null) this.orderByAsc = new ArrayList<>();
        this.orderByAsc.addAll(Arrays.asList(name));
        return this;
    }


    /**
     * 添加 降序 字段
     * 此方法不建议使用  推荐指定 T 泛型 通过 Lambda 函数方式传递参数，避免 字符串 指定 Name 带来的后续代码变动不能及时发现的问题
     *
     * @param name
     * @return
     */
    @Deprecated
    public QueryDto<T> orderByDesc(String... name) {
        if (this.orderByDesc == null) this.orderByDesc = new ArrayList<>();
        this.orderByDesc.addAll(Arrays.asList(name));
        return this;
    }
}
