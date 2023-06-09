package com.ck.core.mybatis;

import com.ck.check_bean.RegExUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @ClassName SqlCompileConfiguration
 * @Description 依据自定义SQL 解析成 mybatis plus BaseMapper java源码
 * @Author Cyk
 * @Version 1.0
 * @since 2023/3/31 9:51
 **/
@Slf4j
public class SqlCompileConfiguration {
    private static final StringBuilder sourceCodeFormat = new StringBuilder();

    static {
        sourceCodeFormat
                .append("package #PackagePath#;").append("\n")
                .append("#ImportList# ").append("\n")
                .append("public interface #PoClassNameMapper# extends BaseMapper<#PoClassNameMapper#.#PoClassName#> {").append("\n")
                .append("    @Data").append("\n")
                .append("    @TableName(value = \"#SQLTableName#\")").append("\n")
                .append("    public static class #PoClassName# implements Serializable{").append("\n")
                .append("#SQLFields#").append("\n")
                .append("    }").append("\n")
                .append("}");
    }

    /**
     * 依赖
     */
    protected List<String> imports = new ArrayList<>(Arrays.asList(
            "com.baomidou.mybatisplus.annotation.TableField;"
            , "com.baomidou.mybatisplus.annotation.TableName;"
            , "org.apache.ibatis.annotations.Mapper;"
            , "com.baomidou.mybatisplus.core.mapper.BaseMapper;"
            , "lombok.Data;"
            , "java.io.Serializable;"
    ));

    /**
     * 获取依赖类列表
     *
     * @return
     */
    protected String getImports() {
        StringBuilder imports = new StringBuilder();
        this.imports.forEach(i -> {
            imports.append("import ").append(i.trim());
            if (!i.endsWith(";")) {
                imports.append(";");
            }
            imports.append("\n");
        });
        return imports.toString();
    }

    /**
     * 获取自定义 sql 表名
     *
     * @param sql
     * @return
     */
    protected String getSqlTableName(String sql) {
        StringBuilder sqlTableName = new StringBuilder();

        if (sql.contains("WHERE")) {
            sqlTableName.append(sql, sql.indexOf("FROM "), sql.indexOf("WHERE"));
        } else if (sql.contains("ORDER BY")) {
            sqlTableName.append(sql, sql.indexOf("FROM "), sql.indexOf("ORDER BY"));
        } else if (sql.contains("GROUP BY")) {
            sqlTableName.append(sql, sql.indexOf("FROM "), sql.indexOf("GROUP BY"));
        } else if (sql.contains("LIMIT")) {
            sqlTableName.append(sql, sql.indexOf("FROM "), sql.indexOf("LIMIT"));
        } else {
            sqlTableName.append(sql, sql.indexOf("FROM "), sql.length());
        }
        return sqlTableName.append(" ").toString().replace("FROM", "");
    }

    /**
     * 获取自定义 sql Po字段
     *
     * @param sql
     * @return
     */
    protected String getSqlPoFields(String sql) {
        StringBuilder sqlFields = new StringBuilder();

        String fieldsSql = sql.substring(0, sql.indexOf("FROM")).replace("SELECT", "");
        String[] fields = fieldsSql.split(",");
        Stream.of(fields).forEach(field -> {
            if (field.startsWith(" ")) {
                field = field.substring(1);
            }
            if (field.endsWith(" ")) {
                field = field.substring(0, field.lastIndexOf(" "));
            }
            if (field.contains(" ") && !field.contains(" AS ")) {
                field = field.replace(" ", " AS ");
            }

            String tableField;
            String classField;
            if (field.contains(" AS ")) {
                String[] s = field.split(" AS ");
                tableField = s[0].trim();
                classField = s[1].trim();
            } else {
                tableField = field;
                classField = field.contains(".") ? field.substring(field.indexOf(".") + 1) : field;
            }

            sqlFields.append(getSqlPoField(tableField, classField));
        });

        return sqlFields.toString();
    }

    /**
     * 获取自定义 sql Po字段
     *
     * @param tableField
     * @param classField
     * @return
     */
    protected String getSqlPoField(String tableField, String classField) {
        return "        @TableField(\"" + tableField + "\")" + "\n" +
                "        private " + "Object" + " " + toHump(classField) + ";" + "\n";
    }

    /**
     * 根据自定义 Sql 生成 MyBatis Plus BaseMapper<PO> 接口源码
     *
     * @param className
     * @param sql
     * @return
     */
    public String getBaseMapperJavaCode(String packagePath, String className, String sql) {
        Objects.requireNonNull(packagePath, "未指定动态 BaseMapper package 地址");
        Objects.requireNonNull(className, "未指定动态 BaseMapper ClassName 名称");
        Objects.requireNonNull(sql, "未指定动态 BaseMapper Sql 语句");

        sql = resetKeyword(sql);


        String javaSource = sourceCodeFormat.toString();
        javaSource = javaSource.replaceAll("#PackagePath#", packagePath);
        javaSource = javaSource.replaceAll("#ImportList#", getImports());
        javaSource = javaSource.replaceAll("#PoClassNameMapper#", getMapperName(className));
        javaSource = javaSource.replaceAll("#PoClassName#", className);

        sql = sql.replaceAll("\\r", " ").replaceAll("\\n", " ");
        javaSource = javaSource.replaceAll("#SQLTableName#", getSqlTableName(sql));
        javaSource = javaSource.replaceAll("#SQLFields#", getSqlPoFields(sql));

        log.info("动态生成 MyBatis Plus BaseMapper 查询接口\n {}", javaSource);
        return javaSource;
    }


    /**
     * 命名BaseMapper
     *
     * @param className
     * @return
     */
    public String getMapperName(String className) {
        if (className != null && !className.endsWith("Mapper")) {
            return className + "Mapper";
        }
        return className;
    }

    /**
     * 重置sql关键字大写
     *
     * @param sql
     * @return
     */
    public String resetKeyword(String sql) {
        sql = sql.replaceAll("\\r", " ").replaceAll("\\n", " ");
        sql = sql.replaceAll(" {2}", " ");
        if (sql.contains("  ")) {
            sql = resetKeyword(sql);
        }

        if (sql.endsWith(";")) {
            sql = sql.replace(";", "");
        }

        if (sql.contains("select")) {
            sql = sql.replaceAll("select", "SELECT");
        } else if (sql.contains("Select")) {
            sql = sql.replaceAll("Select", "SELECT");
        }

        if (sql.contains("distinct")) {
            sql = sql.replaceAll("distinct ", "");
        } else if (sql.contains("DISTINCT")) {
            sql = sql.replaceAll("DISTINCT", "");
        }

        if (sql.contains(" As ")) {
            sql = sql.replaceAll(" As ", " AS ");
        } else if (sql.contains(" as ")) {
            sql = sql.replaceAll(" as ", " AS ");
        } else if (sql.contains(" aS ")) {
            sql = sql.replaceAll(" aS ", " AS ");
        }

        if (sql.contains("from")) {
            sql = sql.replaceAll("from", "FROM");
        }

        if (sql.contains("where")) {
            sql = sql.replaceAll("where", "WHERE");
        }
        if (sql.contains("order by")) {
            sql = sql.replaceAll("order by", "ORDER BY");
        }
        if (sql.contains("group by")) {
            sql = sql.replaceAll("group by", "GROUP BY");
        }
        if (sql.contains("limit")) {
            sql = sql.replaceAll("limit", "LIMIT");
        }

        return sql;
    }

    /**
     * 驼峰命名
     *
     * @param name
     * @return
     */
    public String toHump(String name) {
        return RegExUtil.toHump(name);
    }
}
