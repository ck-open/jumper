package com.ck.core.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ck.api.TResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * @ClassName JumperQueryController
 * @Description 依赖与 QueryUtil 工具构建的 公共跨服务查询接口
 * @Author Cyk
 * @Version 1.0
 * @since 2023/3/15 16:53
 **/
@Api(tags = "公共跨服务查询接口")
@RequestMapping("/jumper")
public class JumperQueryController {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 查询分页数据
     * @param mapperBeanName
     * @param dto
     * @param <T>
     * @return
     */
    @ApiOperation(value = "查询分页数据")
    @ResponseBody
    @PostMapping("page/{mapperBeanName}")
    public <T> TResult<IPage<?>> queryPage(@PathVariable(value = "mapperBeanName") String mapperBeanName, @RequestBody(required = false) QueryUtil.QueryDto dto) {

        BaseMapper<T> mapper = getBaseMapper(mapperBeanName);

        if (mapper != null) {

            IPage<T> page = QueryUtil.queryPage(mapper, dto);

            TResult result = convertDto(mapper, iConvert -> TResult.ok(QueryUtil.buildIPage(page, iConvert)));
            if (result != null) {
                return result;
            }

            // 返回数据对象
            return TResult.ok(page);
        }
        // 返回方法不存在提示
        return TResult.build(0, "无效的请求地址");
    }

    /**
     * 查询列表数据
     * @param mapperBeanName
     * @param dto
     * @param <T>
     * @return
     */
    @ApiOperation(value = "查询列表数据")
    @ResponseBody
    @PostMapping("list/{mapperBeanName}")
    public <T> TResult<List<?>> queryList(@PathVariable(value = "mapperBeanName") String mapperBeanName, @RequestBody(required = false) QueryUtil.QueryDto dto) {

        BaseMapper<T> mapper = getBaseMapper(mapperBeanName);

        if (mapper != null) {

            List<T> list = QueryUtil.query(mapper, dto);

            TResult result = convertDto(mapper, iConvert -> TResult.ok(QueryUtil.convertList(list, iConvert)));
            if (result != null) {
                return result;
            }

            // 返回数据对象
            return TResult.ok(list);
        }
        // 返回方法不存在提示
        return TResult.build(0, "无效的请求地址");
    }

    /**
     * 转换 Dto 对象
     *
     * @param mapper
     * @param function
     */
    private <T> TResult<?> convertDto(BaseMapper<T> mapper, Function<QueryUtil.IConvert, TResult<?>> function) {
        Map<String, QueryUtil.IConvert> convertBeans = applicationContext.getBeansOfType(QueryUtil.IConvert.class);
        for (QueryUtil.IConvert convert : convertBeans.values()) {
            Method convertMethod = convert.getClass().getMethods()[0];
            Class<?> poType = convertMethod.getParameterTypes()[0];
            if (poType.isAssignableFrom(QueryUtil.getMapperEntityClass(mapper))) {
                // 返回转换Dto后的结果
                return function.apply(convert);
            }
        }
        return null;
    }

    /**
     * 获取 BaseMapper<T> 实例
     *
     * @param mapperBeanName
     * @param <T>
     * @return
     */
    private <T> BaseMapper<T> getBaseMapper(String mapperBeanName) {
        try {
            if (!mapperBeanName.endsWith("Mapper")) mapperBeanName += "Mapper";
            return applicationContext.getBean(mapperBeanName, BaseMapper.class);
        } catch (Exception ignored) {
            return applicationContext.getBean(mapperBeanName.replaceAll("Mapper","Dao"), BaseMapper.class);
        }
    }
}
