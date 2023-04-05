package com.ck.core.mybatis;

import com.ck.core.properties.JumperProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ClassName SqlCompileBaseMapper
 * @Description 依据自定义SQL 生成 mybatis plus BaseMapper  并注入到 Spring 容器
 * @Author Cyk
 * @Version 1.0
 * @since 2023/1/30 13:37
 **/
@Slf4j
public class SqlCompileBaseMapper {

    @Resource
    private ApplicationContext applicationContext;
    private ConfigurableListableBeanFactory listableBeanFactory;
    private SqlSessionTemplate sqlSessionTemplate;
    private JumperProperties jumperProperties;

    public SqlCompileBaseMapper(ConfigurableListableBeanFactory listableBeanFactory, SqlSessionTemplate sqlSessionTemplate, JumperProperties jumperProperties) {
        this.listableBeanFactory = listableBeanFactory;
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.jumperProperties = jumperProperties;
    }

    /**
     * 编译自定义 sql 为 BaseMapper 接口并注入到 Spring 容器
     * <p>
     * 生成的 Mapper.class 存储的包路径  优先 配置文件中的 jumper.package_mapper 进行配置
     * 默认 保存在项目路径 jumper.db.mapper 路径下
     *
     * @param beanName
     * @param sql
     * @return
     */
    public boolean registryBaseMapper(String beanName, String sql) {
        try {
            String packagePath = "jumper.db.mapper";
            if (!ObjectUtils.isEmpty(this.jumperProperties) && !ObjectUtils.isEmpty(this.jumperProperties.getPackage_mapper())) {
                packagePath = this.jumperProperties.getPackage_mapper();
            }
            Map<String, Class<?>> mapperClassMap = SqlCompileUtils.getBaseMapperBySql(packagePath, beanName, sql);
            mapperClassMap.forEach((k, v) -> {
                sqlSessionTemplate.getConfiguration().addMapper(v);
                this.listableBeanFactory.registerSingleton(SqlCompileUtils.camelCase(v.getSimpleName()), sqlSessionTemplate.getMapper(v));
            });
        } catch (Exception e) {
            log.error(" 向Spring 注入自定义BaseMapper接口失败", e);
            return false;
        }
        return true;
    }


    /**
     * 启动事件监听  自动注入自定义SQL 生成 BaseMapper
     *
     * @author Cyk
     * @since 14:37 2022/7/15
     **/
    @EventListener({ApplicationReadyEvent.class})
    public void applicationReadyEvent() {
        if (this.applicationContext != null) {
            Map<String, SqlCompileSupplier> sqlCompileSuppliers = this.applicationContext.getBeansOfType(SqlCompileSupplier.class);
            if (ObjectUtils.isEmpty(sqlCompileSuppliers)) {
                log.info("无需要自动生成 BaseMapper 的 SqlCompileSupplier");
                return;
            }
            sqlCompileSuppliers.forEach((name, sqlCompileSupplier) -> {
                Map<String, String> sqlMap = sqlCompileSupplier.getSql();
                if (!ObjectUtils.isEmpty(sqlMap)) {
                    sqlMap.forEach(this::registryBaseMapper);
                }
            });
        }
    }
}
