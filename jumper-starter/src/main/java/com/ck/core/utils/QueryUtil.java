package com.ck.core.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ck.core.mybatis.IConvert;
import com.ck.core.mybatis.QueryDto;
import com.ck.function.ClassUtils;
import com.ck.function.LambdaUtils;
import org.springframework.core.GenericTypeResolver;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName QueryUtil
 * @Description mybatis plus LambdaQuery 查询公共方法
 * @Author Cyk
 * @Version 1.0
 * @since 2023/1/30 13:37
 **/
public final class QueryUtil {

    /**
     * 构建分页对象
     *
     * @param <T>
     * @return
     */
    public static <T> IPage<T> buildIPage(Integer current, Integer size) {
        return buildIPage(Long.valueOf(current), Long.valueOf(size));
    }

    /**
     * 构建分页对象
     *
     * @param <T>
     * @return
     */
    public static <T> IPage<T> buildIPage(Long current, Long size) {
        return new Page<>(Optional.ofNullable(current).orElse(1L), Optional.ofNullable(size).orElse(20L));
    }

    /**
     * 构建分页对象
     *
     * @param <T>
     * @return
     */
    public static <T, R> IPage<T> buildIPage(IPage<R> page, IConvert<T, R> convert) {
        if (page == null) return null;
        IPage<T> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(convertList(page.getRecords(), convert));
        return result;
    }

    /**
     * 结果集转换Dto对象
     *
     * @param data    查询结果集
     * @param convert Po 转 Dto逻辑
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<T> convertList(List<R> data, IConvert<T, R> convert) {
        if (data == null || convert == null) return null;
        return data.stream().map(convert::convert).collect(Collectors.toList());
    }

    /**
     * 直接使用 IService<R> 进行分页查询并返回Dto分页结果
     *
     * @param service 执行查询的 DBService
     * @param dto     查询条件
     * @param convert Po 转 Dto逻辑
     * @param <T>     dto泛型
     * @param <R>     po泛型
     * @return
     */
    public static <T, R> IPage<T> queryPage(IService<R> service, QueryDto dto, IConvert<T, R> convert) {
        IPage<R> page = queryPage(service, dto);
        return buildIPage(page, convert);
    }


    /**
     * 直接使用 BaseMapper<T> 进行查询并返回Dto分页结果
     *
     * @param mapper  执行查询的 BaseMapper<T>
     * @param dto     查询条件
     * @param convert Po 转 Dto逻辑
     * @param <T>     dto泛型
     * @param <R>     po泛型
     * @return
     */
    public static <T, R> IPage<T> queryPage(BaseMapper<R> mapper, QueryDto dto, IConvert<T, R> convert) {
        IPage<R> poPage = queryPage(mapper, dto);
        return buildIPage(poPage, convert);
    }

    /**
     * 直接使用 IService<R> 进行分页查询并返回Dto分页结果
     *
     * @param service 执行查询的 DBService
     * @param dto     查询条件
     * @param <R>     po泛型
     * @return
     */
    public static <R> IPage<R> queryPage(IService<R> service, QueryDto dto) {
        LambdaQueryChainWrapper<R> lambdaQueryWrapper = setWrapper(service.lambdaQuery(), dto, service.getEntityClass());
        return lambdaQueryWrapper.page(QueryUtil.buildIPage(dto.getCurrent(), dto.getSize()));
    }


    /**
     * 直接使用 BaseMapper<T> 进行查询并返回Dto分页结果
     *
     * @param mapper 执行查询的 BaseMapper<T>
     * @param dto    查询条件
     * @param <R>    po泛型
     * @return
     */
    public static <R> IPage<R> queryPage(BaseMapper<R> mapper, QueryDto dto) {
        LambdaQueryWrapper<R> wrapper = setWrapper(new LambdaQueryWrapper<>(), dto, getMapperEntityClass(mapper));
        return mapper.selectPage(buildIPage(dto.getCurrent(), dto.getSize()), wrapper);
    }


    /**
     * 直接使用 IService<R> 进行查询并返回Dto分页结果
     *
     * @param service 执行查询的 DBService
     * @param dto     查询条件
     * @param convert Po 转 Dto逻辑
     * @param <T>     dto泛型
     * @param <R>     po泛型
     * @return
     */
    public static <T, R> List<T> query(IService<R> service, QueryDto dto, IConvert<T, R> convert) {
        return convertList(query(service, dto), convert);
    }


    /**
     * 直接使用 BaseMapper<T> 进行分页查询并返回Dto分页结果
     *
     * @param mapper  执行查询的 BaseMapper<T>
     * @param dto     查询条件
     * @param convert Po 转 Dto逻辑
     * @param <T>     dto泛型
     * @param <R>     po泛型
     * @return
     */
    public static <T, R> List<T> query(BaseMapper<R> mapper, QueryDto dto, IConvert<T, R> convert) {
        List<R> pos = query(mapper, dto);
        return convertList(pos, convert);
    }


    /**
     * 直接使用 IService<R> 进行查询
     *
     * @param service 执行查询的 DBService
     * @param dto     查询条件
     * @param <R>     po泛型
     * @return
     */
    public static <R> List<R> query(IService<R> service, QueryDto dto) {
        LambdaQueryChainWrapper<R> lambdaQueryWrapper = setWrapper(service.lambdaQuery(), dto, service.getEntityClass());
        return lambdaQueryWrapper.list();
    }


    /**
     * 直接使用 BaseMapper<T> 进行查询
     *
     * @param mapper 执行查询的 BaseMapper<T>
     * @param dto    查询条件
     * @param <R>    po泛型
     * @return
     */
    public static <R> List<R> query(BaseMapper<R> mapper, QueryDto dto) {
        LambdaQueryWrapper<R> wrapper = setWrapper(new LambdaQueryWrapper<>(), dto, getMapperEntityClass(mapper));
        return mapper.selectList(wrapper);
    }


    /**
     * 设置 LambdaQueryChainWrapper like 查询条件
     *
     * @param lambdaQueryWrapper 查询 lambdaQueryWrapper 对象
     * @param like               like条件的参数对象
     * @param entityClass        查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryChainWrapper<T> setLike(LambdaQueryChainWrapper<T> lambdaQueryWrapper, Object like, Class<T> entityClass) {
        return setWrapper(lambdaQueryWrapper, null, like, null, null, entityClass);
    }

    /**
     * 设置 LambdaQueryWrapper like 查询条件
     *
     * @param lambdaQueryWrapper 查询 lambdaQueryWrapper 对象
     * @param like               like条件的参数对象
     * @param entityClass        查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryWrapper<T> setLike(LambdaQueryWrapper<T> lambdaQueryWrapper, Object like, Class<T> entityClass) {
        return setWrapper(lambdaQueryWrapper, null, like, null, null, entityClass);
    }

    /**
     * 设置 LambdaQueryChainWrapper eq 查询条件
     *
     * @param lambdaQueryWrapper 查询 lambdaQueryWrapper 对象
     * @param eq                 eq条件的参数对象
     * @param entityClass        查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryChainWrapper<T> setEq(LambdaQueryChainWrapper<T> lambdaQueryWrapper, Object eq, Class<T> entityClass) {
        return setWrapper(lambdaQueryWrapper, eq, null, null, null, entityClass);
    }

    /**
     * 设置 LambdaQueryWrapper eq 查询条件
     *
     * @param lambdaQueryWrapper 查询 lambdaQueryWrapper 对象
     * @param eq                 eq条件的参数对象
     * @param entityClass        查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryWrapper<T> setEq(LambdaQueryWrapper<T> lambdaQueryWrapper, Object eq, Class<T> entityClass) {
        return setWrapper(lambdaQueryWrapper, eq, null, null, null, entityClass);
    }

    /**
     * 设置 LambdaQueryWrapper eq 和 like 查询条件
     *
     * @param lambdaQueryWrapper 查询 lambdaQueryWrapper 对象
     * @param eq                 eq条件的参数对象
     * @param like               like条件的参数对象
     * @param entityClass        查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryChainWrapper<T> setEqAndLike(LambdaQueryChainWrapper<T> lambdaQueryWrapper, Object eq, Object like, Class<T> entityClass) {

        if (ObjectUtils.isNotEmpty(lambdaQueryWrapper) && ObjectUtils.isNotEmpty(entityClass)) {
            setWrapper(lambdaQueryWrapper, eq, like, null, null, entityClass);
        }
        return lambdaQueryWrapper;
    }


    /**
     * 设置 LambdaQueryWrapper eq 和 like 查询条件
     *
     * @param service 查询 IService<T> 对象
     * @param eq      eq条件的参数对象
     * @param like    like条件的参数对象
     * @param <T>
     */
    public static <T> LambdaQueryChainWrapper<T> setEqAndLike(IService<T> service, Object eq, Object like) {
        if (ObjectUtils.isNotEmpty(service)) {
            LambdaQueryChainWrapper<T> lambdaQueryWrapper = service.lambdaQuery();
            Class<T> entityClass = service.getEntityClass();
            setWrapper(lambdaQueryWrapper, eq, like, null, null, entityClass);
            return lambdaQueryWrapper;
        }
        return null;
    }


    /**
     * 设置 LambdaQueryWrapper eq 和 like 查询条件
     *
     * @param lambdaQueryWrapper 查询 lambdaQueryWrapper 对象
     * @param eq                 eq条件的参数对象
     * @param like               like条件的参数对象
     * @param entityClass        查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryWrapper<T> setEqAndLike(LambdaQueryWrapper<T> lambdaQueryWrapper, Object eq, Object like, Class<T> entityClass) {

        if (ObjectUtils.isNotEmpty(lambdaQueryWrapper) && ObjectUtils.isNotEmpty(entityClass)) {
            setWrapper(lambdaQueryWrapper, eq, like, null, null, entityClass);
        }
        return lambdaQueryWrapper;
    }


    /**
     * 设置 LambdaQueryWrapper like 查询条件
     *
     * @param eq          eq条件的参数对象
     * @param like        like条件的参数对象
     * @param entityClass 查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryWrapper<T> setEqAndLike(Object eq, Object like, Class<T> entityClass) {
        return setWrapper(new LambdaQueryWrapper<>(), eq, like, null, null, entityClass);
    }


    /**
     * 设置 LambdaQueryWrapper lt 和 gt 查询条件
     *
     * @param lambdaQueryWrapper 查询 lambdaQueryWrapper 对象
     * @param lt                 小于条件的参数对象
     * @param ge                 大于等于条件的参数对象
     * @param entityClass        查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryChainWrapper<T> setLtAndGe(LambdaQueryChainWrapper<T> lambdaQueryWrapper, Object lt, Object ge, Class<T> entityClass) {

        if (ObjectUtils.isNotEmpty(lambdaQueryWrapper) && ObjectUtils.isNotEmpty(entityClass)) {
            setWrapper(lambdaQueryWrapper, null, null, lt, ge, entityClass);
        }
        return lambdaQueryWrapper;
    }

    /**
     * 设置 LambdaQueryWrapper lt 和 gt 查询条件
     *
     * @param lambdaQueryWrapper 查询 lambdaQueryWrapper 对象
     * @param lt                 小于条件的参数对象
     * @param ge                 大于等于条件的参数对象
     * @param entityClass        查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryWrapper<T> setLtAndGe(LambdaQueryWrapper<T> lambdaQueryWrapper, Object lt, Object ge, Class<T> entityClass) {

        if (ObjectUtils.isNotEmpty(lambdaQueryWrapper) && ObjectUtils.isNotEmpty(entityClass)) {
            setWrapper(lambdaQueryWrapper, null, null, lt, ge, entityClass);
        }
        return lambdaQueryWrapper;
    }

    /**
     * 设置 LambdaQueryChainWrapper eq 和 like 查询条件
     *
     * @param lambdaQueryWrapper 查询 lambdaQueryWrapper 对象
     * @param dto                查询条件
     * @param entityClass        查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryChainWrapper<T> setWrapper(LambdaQueryChainWrapper<T> lambdaQueryWrapper, QueryDto<?> dto, Class<T> entityClass) {
        if (ObjectUtils.isNotEmpty(lambdaQueryWrapper) && ObjectUtils.isNotEmpty(entityClass) && dto != null) {
            setWrapper(dto.getEq(), dto.getLike(), dto.getLt(), dto.getGe(), entityClass, lambdaQueryWrapper::eq, lambdaQueryWrapper::like, lambdaQueryWrapper::lt, lambdaQueryWrapper::ge);
            in(dto.getIn(), entityClass, lambdaQueryWrapper);
            orderBy(dto.getOrderByAsc(), false, entityClass, lambdaQueryWrapper);
            orderBy(dto.getOrderByDesc(), true, entityClass, lambdaQueryWrapper);
        }
        return lambdaQueryWrapper;
    }

    /**
     * 设置 LambdaQueryChainWrapper eq 和 like 查询条件
     *
     * @param lambdaQueryWrapper 查询 lambdaQueryWrapper 对象
     * @param dto                查询条件
     * @param entityClass        查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryWrapper<T> setWrapper(LambdaQueryWrapper<T> lambdaQueryWrapper, QueryDto<?> dto, Class<T> entityClass) {
        if (ObjectUtils.isNotEmpty(lambdaQueryWrapper) && ObjectUtils.isNotEmpty(entityClass) && dto != null) {
            setWrapper(dto.getEq(), dto.getLike(), dto.getLt(), dto.getGe(), entityClass, lambdaQueryWrapper::eq, lambdaQueryWrapper::like, lambdaQueryWrapper::lt, lambdaQueryWrapper::ge);
            in(dto.getIn(), entityClass, lambdaQueryWrapper);
            orderBy(dto.getOrderByAsc(), false, entityClass, lambdaQueryWrapper);
            orderBy(dto.getOrderByDesc(), true, entityClass, lambdaQueryWrapper);
        }
        return lambdaQueryWrapper;
    }


    /**
     * 设置 LambdaQueryChainWrapper eq 和 like 查询条件
     *
     * @param lambdaQueryWrapper 查询 lambdaQueryWrapper 对象
     * @param eq                 eq条件的参数对象
     * @param like               like条件的参数对象
     * @param lt                 小于条件的参数对象
     * @param ge                 大于等于条件的参数对象
     * @param entityClass        查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryChainWrapper<T> setWrapper(LambdaQueryChainWrapper<T> lambdaQueryWrapper, Object eq, Object like, Object lt, Object ge, Class<T> entityClass) {
        if (ObjectUtils.isNotEmpty(lambdaQueryWrapper) && ObjectUtils.isNotEmpty(entityClass)) {
            setWrapper(eq, like, lt, ge, entityClass, lambdaQueryWrapper::eq, lambdaQueryWrapper::like, lambdaQueryWrapper::lt, lambdaQueryWrapper::ge);
        }
        return lambdaQueryWrapper;
    }

    /**
     * 设置 LambdaQueryWrapper eq 和 like 查询条件
     *
     * @param lambdaQueryWrapper 查询 lambdaQueryWrapper 对象
     * @param eq                 eq条件的参数对象
     * @param like               like条件的参数对象
     * @param lt                 小于条件的参数对象
     * @param ge                 大于等于条件的参数对象
     * @param entityClass        查询的实体类对象
     * @param <T>
     */
    public static <T> LambdaQueryWrapper<T> setWrapper(LambdaQueryWrapper<T> lambdaQueryWrapper, Object eq, Object like, Object lt, Object ge, Class<T> entityClass) {

        if (ObjectUtils.isNotEmpty(lambdaQueryWrapper) && ObjectUtils.isNotEmpty(entityClass)) {
            setWrapper(eq, like, lt, ge, entityClass, lambdaQueryWrapper::eq, lambdaQueryWrapper::like, lambdaQueryWrapper::lt, lambdaQueryWrapper::ge);
        }
        return lambdaQueryWrapper;
    }

    /**
     * 设置排序
     *
     * @param in                 in条件
     * @param entityClass        查询实体类型
     * @param lambdaQueryWrapper
     */
    public static <T> void in(Map<String, List<?>> in, Class<?> entityClass, LambdaQueryChainWrapper<T> lambdaQueryWrapper) {
        in(in, entityClass, lambdaQueryWrapper::in);
    }

    /**
     * 设置排序
     *
     * @param in                 in条件
     * @param entityClass        查询实体类型
     * @param lambdaQueryWrapper
     */
    public static <T> void in(Map<String, List<?>> in, Class<?> entityClass, LambdaQueryWrapper<T> lambdaQueryWrapper) {
        in(in, entityClass, lambdaQueryWrapper::in);
    }


    /**
     * 设置排序
     *
     * @param order              排序字段列表
     * @param desc               是否降序  false为升序
     * @param entityClass        查询实体类型
     * @param lambdaQueryWrapper
     */
    public static <T> void orderBy(List<String> order, boolean desc, Class<?> entityClass, LambdaQueryChainWrapper<T> lambdaQueryWrapper) {
        if (desc) {
            orderBy(order, entityClass, lambdaQueryWrapper::orderByDesc);
        } else {
            orderBy(order, entityClass, lambdaQueryWrapper::orderByAsc);
        }
    }

    /**
     * 设置排序
     *
     * @param order              排序字段列表
     * @param desc               是否降序  false为升序
     * @param entityClass        查询实体类型
     * @param lambdaQueryWrapper
     */
    public static <T> void orderBy(List<String> order, boolean desc, Class<?> entityClass, LambdaQueryWrapper<T> lambdaQueryWrapper) {
        if (desc) {
            orderBy(order, entityClass, lambdaQueryWrapper::orderByDesc);
        } else {
            orderBy(order, entityClass, lambdaQueryWrapper::orderByAsc);
        }
    }

    /**
     * 设置 LambdaQueryWrapper eq 和 like 查询条件
     *
     * @param eq           eq条件的参数对象
     * @param like         like条件的参数对象
     * @param lt           小于条件的参数对象
     * @param ge           大于等于条件的参数对象
     * @param entityClass  查询的实体类对象
     * @param eqConsumer   等条件实现函数
     * @param likeConsumer 包含条件实现函数
     * @param likeConsumer 小于条件实现函数
     * @param geConsumer   大于等于条件实现函数
     */
    private static void setWrapper(Object eq, Object like, Object lt, Object ge, Class<?> entityClass, BiConsumer<SFunction, Object> eqConsumer, BiConsumer<SFunction, Object> likeConsumer, BiConsumer<SFunction, Object> ltConsumer, BiConsumer<SFunction, Object> geConsumer) {
        if (ObjectUtils.isNotEmpty(entityClass)) {
            setWrapper(eq, eqConsumer, entityClass);
            setWrapper(like, likeConsumer, entityClass);
            setWrapper(lt, ltConsumer, entityClass);
            setWrapper(ge, geConsumer, entityClass);
        }
    }

    /**
     * 设置查询条件
     *
     * @param conditionBean 条件值对象
     * @param consumer      条件实现函数
     * @param entityClass   Po对象类型
     */
    private static void setWrapper(Object conditionBean, BiConsumer<SFunction, Object> consumer, Class<?> entityClass) {
        if (ObjectUtils.isNotEmpty(conditionBean) && ObjectUtils.isNotEmpty(consumer)) {
            // Po对象字段名称列表
            Set<String> poFieldNames = ClassUtils.getFieldNames(entityClass);

            if (Map.class.isAssignableFrom(conditionBean.getClass())) {
                Map temp = ((Map) conditionBean);
                for (Object key : temp.keySet()) {
                    if (temp.get(key) == null || "".equals(temp.get(key).toString().trim()) || poFieldNames.add(key.toString()))
                        continue;
                    consumer.accept(getFieldSFunction(key.toString(), entityClass), dateToStr(temp.get(key)));
                }
            } else {
                Stream.of(conditionBean.getClass().getDeclaredFields()).filter(i -> !poFieldNames.add(i.getName())).forEach(i -> {
                    try {
                        i.setAccessible(true);
                        Object val = i.get(conditionBean);
                        if (val != null && "".equals(val.toString().trim())) {
                            consumer.accept(getFieldSFunction(i.getName(), entityClass), dateToStr(val));
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private static <T> SFunction<T, ?> getFieldSFunction(String fieldName, Class<T> entityClass) {
        try {
            return (SFunction<T, ?>) LambdaUtils.getFieldLambdaMethodHandle(fieldName, entityClass, SFunction.class).invokeExact();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    /**
     * 设置 in条件
     *
     * @param in          in条件
     * @param entityClass 查询实体类型
     * @param inConsumer  排序具体实现
     */
    private static <T> void in(Map<String, List<?>> in, Class<?> entityClass, BiConsumer<SFunction, List<?>> inConsumer) {
        if (ObjectUtils.isNotEmpty(in) && ObjectUtils.isNotEmpty(entityClass) && ObjectUtils.isNotEmpty(inConsumer)) {
            // Po对象字段名称列表
            Set<String> poFieldNames = ClassUtils.getFieldNames(entityClass);
            in.keySet().stream().filter(poFieldNames::contains).forEach(name -> inConsumer.accept(getFieldSFunction(name, entityClass), in.get(name)));
        }
    }

    /**
     * 设置 排序条件
     *
     * @param order         排序字段列表
     * @param entityClass   查询实体类型
     * @param orderConsumer 排序具体实现
     */
    private static void orderBy(List<String> order, Class<?> entityClass, Consumer<SFunction> orderConsumer) {
        if (ObjectUtils.isNotEmpty(order) && ObjectUtils.isNotEmpty(entityClass) && ObjectUtils.isNotEmpty(orderConsumer)) {
            // Po对象字段名称列表
            Set<String> poFieldNames = ClassUtils.getFieldNames(entityClass);
            order.stream().filter(poFieldNames::contains).forEach(name -> orderConsumer.accept(getFieldSFunction(name, entityClass)));
        }
    }

    /**
     * 获取泛型类型
     *
     * @param clazz
     * @param genericIfc
     * @param index
     * @return
     */
    private static <T> Class<T> getSuperClassGenericType(final Class<?> clazz, final Class<?> genericIfc, final int index) {
        Class<?>[] typeArguments = GenericTypeResolver.resolveTypeArguments(ClassUtils.getUserClass(clazz), genericIfc);
        return null == typeArguments ? null : (Class<T>) typeArguments[index];
    }


    /**
     * MyBatis 生成Sql时 无配置  不会转换日期类型
     *
     * @param val
     * @return
     */
    private static Object dateToStr(Object val) {
        if (Date.class.isAssignableFrom(val.getClass())) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            val = simpleDateFormat.format(val);
        }
        return val;
    }

    /**
     * 获取 BaseMapper<T> 中T的类型
     *
     * @param mapper
     * @param <T>
     * @return
     */
    public static <T> Class<T> getMapperEntityClass(BaseMapper<T> mapper) {
        return getSuperClassGenericType(mapper.getClass(), BaseMapper.class, 0);
    }


    /**
     * 构建查询对象
     *
     * @param eq
     * @param like
     * @param <T>
     * @return
     */
    public static <T> QueryDto<T> buildQueryDto(T eq, T like) {
        return new QueryDto<T>().setEq(eq).setLike(like);
    }

    /**
     * 构建查询对象
     *
     * @param <T>
     * @return
     */
    public static <T> QueryDto<T> buildQueryDto() {
        return new QueryDto<T>();
    }

    /**
     * 根据大于等于时间 加分量获取小于时间
     * 例如：2023-01-01 00:00:00  小于时间为2023-01-02 00:00:00
     * 用法：lt = getLtByGe(ge,"D",1);
     *
     * @param ge
     * @param unit
     * @param val
     * @return
     */
    public static Date getLtByGe(Date ge, char unit, int val) {
        if (ge == null) return null;
        Calendar lt = Calendar.getInstance();
        lt.setTime(ge);
        switch (unit) {
            case 'Y':
                lt.add(Calendar.YEAR, val);
                break;
            case 'M':
                lt.add(Calendar.MONTH, val);
                break;
            case 'D':
                lt.add(Calendar.DAY_OF_YEAR, val);
                break;
            case 'H':
                lt.add(Calendar.HOUR_OF_DAY, val);
                break;
            case 'm':
                lt.add(Calendar.MINUTE, val);
                break;
        }
        return lt.getTime();
    }
}
