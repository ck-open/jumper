package com.ck.core.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ck.core.properties.JumperProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.ObjectUtils;

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

    private DefaultListableBeanFactory listableBeanFactory;
    private SqlSessionTemplate sqlSessionTemplate;
    private JumperProperties jumperProperties;
    private String packagePath = "jumper.db.mapper";

    public SqlCompileBaseMapper(DefaultListableBeanFactory listableBeanFactory, SqlSessionTemplate sqlSessionTemplate, JumperProperties jumperProperties) {
        this.listableBeanFactory = listableBeanFactory;
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.jumperProperties = jumperProperties;
        if (!ObjectUtils.isEmpty(this.jumperProperties) && !ObjectUtils.isEmpty(this.jumperProperties.getPackage_mapper())) {
            this.packagePath = this.jumperProperties.getPackage_mapper();
        }
    }

    /**
     * 编译自定义 sql 为 BaseMapper 接口并注入到 Spring 容器
     * <p>
     * 生成的 Mapper.class 存储的包路径  优先 配置文件中的 jumper.package_mapper 进行配置
     * 默认 保存在项目路径 jumper.db.mapper 路径下
     *
     * @param className
     * @param sql
     * @return
     */
    public boolean resetBaseMapper(String className, String sql) {
        try {
            Map<String, Class<?>> mapperClassMap = SqlCompileUtils.getBaseMapperBySql(packagePath, className, sql);
            mapperClassMap.forEach((k, v) -> {
                String beanName = SqlCompileUtils.camelCase(className);

                Object o = this.listableBeanFactory.getBean(beanName);
                if (!BaseMapper.class.isAssignableFrom(o.getClass())) {
                    throw new RuntimeException(String.format("重新生成失败，beanName [%s] 已存在", beanName));
                }

                this.listableBeanFactory.removeBeanDefinition(beanName);

                sqlSessionTemplate.getConfiguration().addMapper(v);
                this.listableBeanFactory.registerSingleton(beanName, sqlSessionTemplate.getMapper(v));
            });
        } catch (Exception e) {
            log.error(" 重置 Spring 注入自定义BaseMapper接口失败", e);
            return false;
        }
        return true;
    }

    /**
     * 编译自定义 sql 为 BaseMapper 接口并注入到 Spring 容器
     * <p>
     * 生成的 Mapper.class 存储的包路径  优先 配置文件中的 jumper.package_mapper 进行配置
     * 默认 保存在项目路径 jumper.db.mapper 路径下
     *
     * @param className
     * @param sql
     * @return
     */
    public boolean registryBaseMapper(String className, String sql) {
        try {
            Map<String, Class<?>> mapperClassMap = SqlCompileUtils.getBaseMapperBySql(packagePath, className, sql);
            mapperClassMap.forEach((k, v) -> {
                sqlSessionTemplate.getConfiguration().addMapper(v);
                this.listableBeanFactory.registerSingleton(SqlCompileUtils.camelCase(className), sqlSessionTemplate.getMapper(v));
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
        if (this.listableBeanFactory != null) {
            Map<String, SqlCompileSupplier> sqlCompileSuppliers = this.listableBeanFactory.getBeansOfType(SqlCompileSupplier.class);
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
