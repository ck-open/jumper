package com.ck.core.properties;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;


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
public class JumperProperties {
    /**
     * 是否启用文档
     */
    private Boolean enabled = false;

    /**
     * 是否报文规则校验
     */
    private Boolean checkBody = false;
    /**
     * 使用的数据库类型 参考${com.baomidou.mybatisplus.annotation.DbType}
     */
    private String DbType;
    /**
     * 动态生成的 BaseMapper 接口包路径
     */
    private String package_mapper;

}
