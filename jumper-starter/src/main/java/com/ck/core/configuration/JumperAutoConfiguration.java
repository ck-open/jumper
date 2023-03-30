package com.ck.core.configuration;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.ck.core.feign.FeignClientUtils;
import com.ck.core.interceptor.CheckRequestBodyInterceptor;
import com.ck.core.mybatis.JumperQueryController;
import com.ck.core.properties.JumperProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;


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
    @ConditionalOnMissingBean(JumperQueryController.class)
    public JumperQueryController jumperQueryController() {
        JumperQueryController exceptionHandler = new JumperQueryController();
        log.info("JumperQueryController [{}]", exceptionHandler);
        return exceptionHandler;
    }

    /**
     * MyBatisPlus  分页支持
     *
     * @return
     */
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    @Bean("mybatisPlusInterceptor")
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        log.info("MybatisPlusInterceptor [{}]", interceptor);
        return interceptor;
    }

}
