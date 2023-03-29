package com.ck.core.configuration;


import com.ck.core.feign.FeignClientUtils;
import com.ck.core.mybatis.JumperQueryController;
import com.ck.core.properties.JumperProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@ConditionalOnProperty(prefix = "jumper", name = "enabled", havingValue = "true")
@Configuration
@EnableConfigurationProperties({JumperProperties.class})
public class JumperAutoConfiguration {

//    /**
//     * 接口Body报文体对象非空规则自动校验
//     *
//     * @return
//     */
//    @Bean
//    @ConditionalOnClass(RequestBodyAdviceAdapter.class)
//    @ConditionalOnMissingBean(CheckRequestBodyInterceptor.class)
//    public CheckRequestBodyInterceptor requestBodyAdviceAdapter() {
//        CheckRequestBodyInterceptor requestBodyAdviceAdapter = new CheckRequestBodyInterceptor();
//        log.info("CheckRequestBodyInterceptor [{}]", requestBodyAdviceAdapter);
//        return requestBodyAdviceAdapter;
//    }


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

}
