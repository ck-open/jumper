package com.ck.core.configuration;


import com.ck.core.properties.SwaggerProperties;
import com.github.xiaoymin.knife4j.spring.extension.ApiAuthorExtension;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 需要 spring-boot-starter-web 组件的注入Bean  都需要在此配置文件中配置否则影响Gateway服务
 */
@Slf4j
@ConditionalOnProperty(prefix = "knife4j.swagger", name = "enabled", havingValue = "true")
@Configuration
@EnableConfigurationProperties({SwaggerProperties.class})
public class SwaggerAutoConfiguration {

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
    public void applicationReadyEvent() {
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

    /**
     * Knife4j  Swagger  配置注入
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(Knife4jConfigurer.class)
    public Knife4jConfigurer knife4jConfigurer() {
        Knife4jConfigurer webConfigurer = new Knife4jConfigurer();
        log.info("Knife4jConfigurer [{}]", webConfigurer);
        return webConfigurer;
    }


    /**
     * @return Docket
     * @description 配置文档生成
     * @author Cyk
     * @since 14:12 2022/7/13
     **/
    @Bean
    @ConditionalOnMissingBean(Docket.class)
    public Docket createRestApi() {

//        return new Docket(DocumentationType.SWAGGER_2)
        return new Docket(DocumentationType.OAS_30)
                .enable(swaggerProperties.getEnabled())
                .apiInfo(getApiInfo())
                .select()
                // 方式一: 配置扫描 所有想在swagger界面的统一管理接口，都必须在此包下
                .apis(RequestHandlerSelectors.basePackage(swaggerProperties.getBasePackage()))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                // 方式二: 只有当方法上有  @ApiOperation 注解时才能生成对应的接口文档
                 .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))

                .paths(PathSelectors.any())
                .build()
                .globalRequestParameters(getGlobalRequestParameters())
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    private List<SecurityScheme> securitySchemes() {
        return Collections.singletonList(new ApiKey("Authorization", "Authorization", ParameterType.HEADER.getIn(), Collections.singletonList(new ApiAuthorExtension("Bearer "))));
    }

    private List<SecurityContext> securityContexts() {
        return Collections.singletonList(
                SecurityContext.builder()
                        .securityReferences(defaultAuth())
                        .operationSelector(null)
                        .build()
        );
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        return Collections.singletonList(new SecurityReference("Authorization", new AuthorizationScope[]{authorizationScope}));
    }


    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                .title(swaggerProperties.getTitle())
                .description(String.format("<div style='font-size:14px;color:blue;'>%s</div>", swaggerProperties.getDescription()))
                .termsOfServiceUrl(swaggerProperties.getTermsOfServiceUrl())
                .contact(new Contact(swaggerProperties.getContact().getName(), swaggerProperties.getContact().getUrl(), swaggerProperties.getContact().getEmail()))
                .version(swaggerProperties.getVersion())
                .build();
    }

    /**
     * 配置公共参数
     *
     * @return
     */
    private List<RequestParameter> getGlobalRequestParameters() {
        List<RequestParameter> result = new ArrayList<>();

        // 配置固定参数
        RequestParameterBuilder builder = new RequestParameterBuilder();
        RequestParameter parameter = builder.name("access_token")  // 参数key
                .description("授权token")   // 参数描述
                .required(false) // 是否必填参数
                .in(ParameterType.QUERY).build();  // 传参方式：query(url拼接)、header(请求头)
        result.add(parameter);

        RequestParameter headerToken = builder.name("Authorization")  // 参数key
                .description("授权token")   // 参数描述
                .required(false) // 是否必填参数
                .in(ParameterType.HEADER).build();  // 传参方式：query(url拼接)、header(请求头)
        result.add(headerToken);


        if (swaggerProperties.getGlobalOperationParameters() != null) {
            result.addAll(
                    swaggerProperties.getGlobalOperationParameters().stream().map(i -> builder.name(i.getName())  // 参数key
                            .description(i.getDescription()).required(i.getRequired())
                            .in(ParameterType.from(i.getParameterType())).build()).collect(Collectors.toList()));
        }

        return result;
    }


    /**
     * 解决 spring boot 2.6.4  高版本问题 与 Knife4jConfigurer 兼容问题
     *
     * @return
     */
    @Bean
    public static BeanPostProcessor springFoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                List<T> copy = mappings.stream()
                        .filter(mapping -> mapping.getPatternParser() == null)
                        .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                try {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    assert field != null;
                    field.setAccessible(true);
                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }


}
