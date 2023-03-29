package com.ck.core.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ObjectUtils;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ObjectVendorExtension;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.service.VendorExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName SwaggerProperties
 * @Description 配置文件
 * @Author Cyk
 * @Version 1.0
 * @since 2022/7/13 13:57
 **/
@Data
@Accessors(chain = true)
@ConfigurationProperties("knife4j.swagger")
public class SwaggerProperties {
    /**
     * 是否启用文档
     */
    private Boolean enabled = false;
    /**
     * 自动打开浏览器
     */
    private Boolean autoBrowser = false;
    /**
     * 文档标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;
    /**
     * 版本
     */
    private String version;
    /**
     * 许可信息
     */
    private String license;
    /**
     * 许可信息地址
     */
    private String licenseUrl;
    /**
     * 服务信息地址
     */
    private String termsOfServiceUrl;
    /**
     * 作者信息
     */
    private Contact contact;
    /**
     * 接口扫描的包
     */
    private String basePackage;

    private List<String> basePath;
    private List<String> excludePath;
    private Map<String, DocketInfo> docket;
    private String host;
    private List<GlobalOperationParameter> globalOperationParameters;
    private final List<VendorExtension> vendorExtensions = new ArrayList<>();

    /**
     * @description 构建ApiInfo对象
     * @author Cyk
     * @since 10:23 2022/7/14
     * @param apiInfo
     * @return ApiInfo
     **/
    public ApiInfo build(ApiInfo apiInfo){
        if (ObjectUtils.isEmpty(this.title)){
            this.title = apiInfo.getTitle();
        }
        if (ObjectUtils.isEmpty(this.description)){
            this.description = apiInfo.getDescription();
        }
        if (ObjectUtils.isEmpty(this.version)){
            this.version = apiInfo.getVersion();
        }
        if (ObjectUtils.isEmpty(this.termsOfServiceUrl)){
            this.termsOfServiceUrl = apiInfo.getTermsOfServiceUrl();
        }
        if (ObjectUtils.isEmpty(this.contact)){
            this.contact = new Contact();
            if (!ObjectUtils.isEmpty(apiInfo.getContact())){
                this.contact.setName(apiInfo.getContact().getName()).setUrl(apiInfo.getContact().getUrl()).setEmail(apiInfo.getContact().getEmail());
            }
        }
        if (ObjectUtils.isEmpty(vendorExtensions)){
            ObjectVendorExtension logo = new ObjectVendorExtension("x-logo");
            logo.addProperty(new StringVendorExtension("url", "https://xxx.svg"));
            logo.addProperty(new StringVendorExtension("color", "#090807"));
            this.vendorExtensions.add(logo);
        }

       return new ApiInfo(this.title, this.description, this.version, this.termsOfServiceUrl
               , new springfox.documentation.service.Contact(this.contact.name,this.contact.url,this.contact.email)
               , this.license, this.licenseUrl, this.vendorExtensions);
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode
    public static class Contact {
        private String name;
        private String url;
        private String email;
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode
    public static class DocketInfo {
        private String title;
        private String description;
        private String version;
        private String license;
        private String licenseUrl;
        private String termsOfServiceUrl;
        private Contact contact = new Contact();
        private String basePackage;
        private List<String> basePath = new ArrayList();
        private List<String> excludePath = new ArrayList();
        private List<GlobalOperationParameter> globalOperationParameters;
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode
    public static class GlobalOperationParameter {
        private String name;
        private String description;
        private String modelRef;
        private String parameterType;
        private Boolean required;
    }
}
