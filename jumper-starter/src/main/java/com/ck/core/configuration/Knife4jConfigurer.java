package com.ck.core.configuration;

import com.ck.core.properties.SwaggerProperties;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @ClassName Knife4jConfig
 * @Description Knife4j 配置
 * @Author Cyk
 * @Version 1.0
 * @since 2022/7/13 10:58
 **/
@Slf4j
@EnableKnife4j
@EnableSwagger2
@EnableOpenApi
public class Knife4jConfigurer {
    @Value("${server.port:?}")
    private String port;

    @Resource
    private SwaggerProperties swaggerProperties;


    /**
     * @param
     * @return void
     * @description 启动后自动打开浏览器
     * @author Cyk
     * @since 14:37 2022/7/15
     **/
    @EventListener({ApplicationReadyEvent.class})
    void applicationReadyEvent() {
        if (!swaggerProperties.getAutoBrowser()) return;

        // 这里需要注url:端口号+测试类方法名
        String url = String.format("http://localhost:%s/doc.html", port);
        log.info("应用已经准备就绪 ... 启动浏览器  " + url);
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
