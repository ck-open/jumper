package com.ck.core.configuration;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.ck.core.feign.FeignClientUtils;
import com.ck.core.feign.JumperFeign;
import com.ck.core.interceptor.CheckRequestBodyInterceptor;
import com.ck.core.mybatis.JumperQueryController;
import com.ck.core.mybatis.SqlCompileBaseMapper;
import com.ck.core.mybatis.SqlCompileBaseMapperController;
import com.ck.core.properties.JumperProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * @ClassName JumperAutoConfiguration
 * @Author Cyk
 * @Version 1.0
 * @since 2023/1/30 13:37
 **/
@Slf4j
@ConditionalOnProperty(prefix = "jumper", name = "enabled", havingValue = "true")
@Configuration
@EnableConfigurationProperties({JumperProperties.class})
public class JumperAutoConfiguration {

    /**
     * 接口Body报文体对象非空规则自动校验
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "jumper", name = "checkBody", havingValue = "true")
    @ConditionalOnClass(RequestBodyAdviceAdapter.class)
    @ConditionalOnMissingBean(CheckRequestBodyInterceptor.class)
    public CheckRequestBodyInterceptor requestBodyAdviceAdapter() {
        CheckRequestBodyInterceptor requestBodyAdviceAdapter = new CheckRequestBodyInterceptor();
        log.info("CheckRequestBodyInterceptor [{}]", requestBodyAdviceAdapter);
        return requestBodyAdviceAdapter;
    }


    /**
     * 依赖与 QueryUtil 工具构建的 公共数据查询接口
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(FeignClientUtils.class)
    public FeignClientUtils feignClientDynamic() {
        FeignClientUtils feignClientUtils = new FeignClientUtils();
        log.info("FeignClientDynamic [{}]", feignClientUtils);
        return feignClientUtils;
    }


    /**
     * 依赖与 QueryUtil 工具构建的 公共数据查询接口
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(JumperFeign.class)
    public JumperFeign jumperFeign(ConfigurableListableBeanFactory beanFactory, ObjectMapper objectMapper) {
        JumperFeign jumperFeign = new JumperFeign(beanFactory, objectMapper);
        log.info("JumperFeign [{}]", jumperFeign);
        return jumperFeign;
    }


    /**
     * 依赖与 QueryUtil 工具构建的 公共数据查询接口
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(JumperQueryController.class)
    public JumperQueryController jumperQueryController() {
        JumperQueryController exceptionHandler = new JumperQueryController();
        log.info("JumperQueryController [{}]", exceptionHandler);
        return exceptionHandler;
    }

    /**
     * 依赖与 QueryUtil 工具构建的 公共数据查询接口
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "jumper.SqlCompile", name = "controller", havingValue = "true")
    @ConditionalOnMissingBean(SqlCompileBaseMapperController.class)
    public SqlCompileBaseMapperController sqlCompileBaseMapperController() {
        SqlCompileBaseMapperController exceptionHandler = new SqlCompileBaseMapperController();
        log.info("SqlCompileBaseMapperController [{}]", exceptionHandler);
        return exceptionHandler;
    }

    /**
     * 依赖与 QueryUtil 工具构建的 公共数据查询接口
     *
     * @return
     */
    @Bean("sqlCompileBaseMapper")
    @ConditionalOnMissingBean(SqlCompileBaseMapper.class)
    public SqlCompileBaseMapper sqlCompileBaseMapper(DefaultListableBeanFactory beanFactory, SqlSessionTemplate sqlSessionTemplate, JumperProperties jumperProperties) {
        SqlCompileBaseMapper sqlCompileBaseMapper = new SqlCompileBaseMapper(beanFactory, sqlSessionTemplate, jumperProperties);
        log.info("SqlCompileBaseMapper [{}]", sqlCompileBaseMapper);
        return sqlCompileBaseMapper;
    }

    /**
     * MyBatisPlus  分页支持
     *
     * @return
     */
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    @Bean("mybatisPlusInterceptor")
    public MybatisPlusInterceptor mybatisPlusInterceptor(JumperProperties jumperProperties) {
        DbType dbType = null;
        if (jumperProperties != null && jumperProperties.getDbType() != null) {
            dbType = DbType.getDbType(jumperProperties.getDbType());
        }
        if (dbType == null) dbType = DbType.MYSQL;

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));
        log.info("MybatisPlusInterceptor [{}]", interceptor);
        return interceptor;
    }
}
