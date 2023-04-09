package com.ck.core.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ck.core.event.EventHandler;
import com.ck.core.properties.JumperProperties;
import com.ck.function.JavaCompilerUtils;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName Dynamic Compile Loading MyBatis Plus BaseMapper
 * @Description 依据自定义 javaCode 动态生成 mybatis plus BaseMapper  并注入到 Spring 容器
 * @Author Cyk
 * @Version 1.0
 * @since 2023/1/30 13:37
 **/
@Slf4j
public class SqlCompileBaseMapper {
    private static final Set<String> baseMapperBeanNames = Collections.synchronizedSet(new HashSet<>());

    private DefaultListableBeanFactory listableBeanFactory;
    private SqlSessionTemplate sqlSessionTemplate;
    private String packagePath = "jumper.db.mapper";
    private SqlCompileConfiguration sqlCompileConfiguration;
    private JavaCompilerHandler JavaCompilerHandler;

    public SqlCompileBaseMapper(DefaultListableBeanFactory listableBeanFactory, JumperProperties jumperProperties) {
        this.listableBeanFactory = listableBeanFactory;

        try {
            this.sqlSessionTemplate = this.listableBeanFactory.getBean(SqlSessionTemplate.class);
        } catch (Exception e) {
            throw new RuntimeException("构建 SqlCompileBaseMapper 失败，未获取到 SqlSessionTemplate 实例");
        }

        if (!ObjectUtils.isEmpty(jumperProperties) && !ObjectUtils.isEmpty(jumperProperties.getPackage_mapper())) {
            this.packagePath = jumperProperties.getPackage_mapper();
        }

        try {
            this.sqlCompileConfiguration = this.listableBeanFactory.getBean(SqlCompileConfiguration.class);
        } catch (Exception e) {
            this.sqlCompileConfiguration = new SqlCompileConfiguration();
        }

        try {
            this.JavaCompilerHandler = this.listableBeanFactory.getBean(JavaCompilerHandler.class);
        } catch (Exception e) {
            this.JavaCompilerHandler = JavaCompilerUtils::compilerString;
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
    public boolean registryBaseMapper(String className, String sql) {
        String beanName = this.sqlCompileConfiguration.toHump(this.sqlCompileConfiguration.getMapperName(className.trim()));
        try {
            if (this.listableBeanFactory.containsBean(beanName)) {
                log.info(String.format("动态 Sql 编译BaseMapper失败，ClassName: %s 已存在容器中", beanName));
                return false;
            }
            if (baseMapperBeanNames.add(beanName)) {
                String javaCode = this.sqlCompileConfiguration.getBaseMapperJavaCode(packagePath, className, sql);
                Map<String, Class<?>> mapperClassMap = this.JavaCompilerHandler.compiler(javaCode);
                if (ObjectUtils.isEmpty(mapperClassMap)) {
                    EventHandler.publish(new EventHandler.SqlCompileError(className));
                    throw new RuntimeException(String.format("动态 Sql 编译BaseMapper失败，ClassName: %s", className));
                }
                mapperClassMap.forEach((k, v) -> {
                    this.sqlSessionTemplate.getConfiguration().addMapper(v);
                    this.listableBeanFactory.registerSingleton(this.sqlCompileConfiguration.toHump(beanName), this.sqlSessionTemplate.getMapper(v));
                });
                return true;
            }
        } catch (Exception e) {
            baseMapperBeanNames.remove(beanName);
            log.error(" 向Spring 注入自定义BaseMapper接口失败", e);
        }
        return false;
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
            if (destroyBaseMapper(className)) {
                return registryBaseMapper(className, sql);
            }
        } catch (Exception e) {
            log.error(" 重置 Spring 注入自定义BaseMapper接口失败", e);
        }
        return false;
    }

    /**
     * 卸载容器中的 BaseMapper 对象
     *
     * @param beanName
     * @return
     */
    public boolean destroyBaseMapper(String beanName) {
        try {
            beanName = this.sqlCompileConfiguration.toHump(this.sqlCompileConfiguration.getMapperName(beanName.trim()));
            if (baseMapperBeanNames.contains(beanName)) {
                Object o = this.listableBeanFactory.getBean(beanName);
                if (!BaseMapper.class.isAssignableFrom(o.getClass())) {
                    log.info(String.format("禁止破坏容器中非BaseMapperBean的对象 BeanName:%s", beanName));
                    return false;
                }

                this.listableBeanFactory.destroySingleton(beanName);
                baseMapperBeanNames.remove(beanName);
                return true;
            } else {
                log.info(String.format("禁止破坏容器中非动态编译的BaseMapperBean对象 BeanName:%s", beanName));
            }
        } catch (Exception e) {
            log.error("卸载 BaseMapper 失败 Error: " + e.getMessage(), e);
        }
        return false;
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
                    sqlMap.forEach((k, v) -> {
                        try {
                            registryBaseMapper(k, v);
                        } catch (Exception e) {
                            log.error("初始化自定义Sql 编译BaseMapper失败", e);
                        }
                    });
                }
            });
        }
    }

}
