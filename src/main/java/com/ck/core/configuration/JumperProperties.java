package com.ck.core.configuration;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


/**
 * @ClassName SwaggerProperties
 * @Description 配置文件
 * @Author Cyk
 * @Version 1.0
 * @since 2022/7/13 13:57
 **/
@Data
@Accessors(chain = true)
@ConfigurationProperties("jumper")
@EnableConfigurationProperties
public class JumperProperties {
    /**
     * 是否启用文档
     */
    private Boolean enabled = false;

}
