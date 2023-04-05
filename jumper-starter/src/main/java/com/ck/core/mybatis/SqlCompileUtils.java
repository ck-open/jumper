package com.ck.core.mybatis;

import com.ck.function.JavaCompilerUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @ClassName Dynamic Loading MyBatis Plus BaseMapper
 * @Description 动态生成加载 BaseMapper 工具类
 * @Author Cyk
 * @Version 1.0
 * @since 2023/3/31 9:51
 **/
@Slf4j
public class SqlCompileUtils {
    private static final StringBuilder sourceCodeFormat = new StringBuilder();

    static {
        sourceCodeFormat
                .append("package PackagePath;").append("\n")
                .append("import com.baomidou.mybatisplus.annotation.TableField;").append("\n")
                .append("import com.baomidou.mybatisplus.annotation.TableName;").append("\n")
                .append("import org.apache.ibatis.annotations.Mapper;").append("\n")
                .append("import com.baomidou.mybatisplus.core.mapper.BaseMapper;").append("\n")
                .append("import lombok.Data;").append("\n")
                .append("import java.io.Serializable;").append("\n")
                .append("public interface PoClassNameMapper extends BaseMapper<PoClassNameMapper.PoClassName> {").append("\n")
                .append("    @Data").append("\n")
                .append("    @TableName(value = \"SQLTableName\")").append("\n")
                .append("    public static class PoClassName implements Serializable{").append("\n")
                .append("SQLFields").append("\n")
                .append("    }").append("\n")
                .append("}");
    }

    /**
     * 根据自定义 Sql 生成 MyBatis Plus BaseMapper<PO> 接口源码
     *
     * @param className
     * @param sql
     * @return
     */
    public static Map<String, Class<?>> getBaseMapperBySql(String packagePath, String className, String sql) {
        Objects.requireNonNull(packagePath, "未指定动态 BaseMapper package 地址");
        Objects.requireNonNull(className, "未指定动态 BaseMapper ClassName 名称");
        Objects.requireNonNull(sql, "未指定动态 BaseMapper Sql 语句");

        String javaSource = sourceCodeFormat.toString();
        javaSource = javaSource.replaceAll("PackagePath", packagePath);
        javaSource = javaSource.replaceAll("PoClassName", className);

        sql = sql.replaceAll("\\r", " ").replaceAll("\\n", " ");
        javaSource = javaSource.replaceAll("SQLTableName", getSqlTableName(sql));
        javaSource = javaSource.replaceAll("SQLFields", getSqlPoFields(sql));

        log.info("动态生成 MyBatis Plus BaseMapper 查询接口\n {}", javaSource);
        return JavaCompilerUtils.compilerString(javaSource);
    }

    /**
     * 获取自定义 sql 表名
     *
     * @param sql
     * @return
     */
    private static String getSqlTableName(String sql) {
        StringBuilder sqlTableName = new StringBuilder();

        if (sql.contains("FROM")) {
            sql.replace("FROM", "from");
        }
        if (sql.contains("where")) {
            sqlTableName.append(sql, sql.indexOf("from "), sql.indexOf("where"));
        } else if (sql.contains("order by")) {
            sqlTableName.append(sql, sql.indexOf("from "), sql.indexOf("order by"));
        } else if (sql.contains("group by")) {
            sqlTableName.append(sql, sql.indexOf("from "), sql.indexOf("group by"));
        } else if (sql.contains("limit")) {
            sqlTableName.append(sql, sql.indexOf("from "), sql.indexOf("limit"));
        } else {
            sqlTableName.append(sql, sql.indexOf("from "), sql.length());
        }
        return sqlTableName.append(" ").toString().replace("from", "");
    }

    /**
     * 获取自定义 sql Po字段
     *
     * @param sql
     * @return
     */
    private static String getSqlPoFields(String sql) {
        StringBuilder sqlFields = new StringBuilder();

        String fieldsSql = sql.substring(0, sql.indexOf(" from")).replace("select ", "");
        String[] fields = fieldsSql.split(",");
        Stream.of(fields).forEach(field -> {
            field = field.replaceAll(" {2}", " ");
            if (field.startsWith(" ")) {
                field = field.substring(1);
            }
            if (field.endsWith(" ")) {
                field = field.substring(0, field.lastIndexOf(" "));
            }
            if (field.contains(" ")) {
                field = field.replace(" ", " AS ");
            }

            if (field.contains(" As ")) {
                field = field.replace(" As ", " AS ");
            } else if (field.contains(" as ")) {
                field = field.replace(" as ", " AS ");
            }

            String tableField;
            String classField;
            if (field.contains(" AS ")) {
                String[] s = field.split(" AS ");
                tableField = s[0].trim();
                classField = s[1].trim();
                classField = classField.contains(".") ? classField.substring(classField.indexOf(".") + 1) : field;
            } else {
                tableField = field;
                classField = field.contains(".") ? field.substring(field.indexOf(".") + 1) : field;
            }

            sqlFields.append("        @TableField(\"").append(tableField).append("\")").append("\n")
                    .append("        private ").append("Object").append(" ").append(camelCase(classField)).append(";").append("\n");
        });

        return sqlFields.toString();
    }

    /**
     * 驼峰命名
     *
     * @param name
     * @return
     */
    public static String camelCase(String name) {
        if (name.contains("_")) {
            StringBuilder f = new StringBuilder();
            String[] s = name.split("_");
            for (String i : s) {
                if (f.length() < 1) {
                    f.append(i.toLowerCase());
                } else {
                    if (i.length() < 1) {
                        f.append(i.toUpperCase());
                    } else {
                        f.append(i.substring(0, 1).toUpperCase());
                        if (i.length() > 1) {
                            f.append(i.substring(1));
                        }
                    }
                }
            }
            name = f.toString();
        } else if (name.length() > 0) {
            if (name.length()>1){
                name = name.substring(0, 1).toLowerCase() + name.substring(1);
            }else{
                name = name.substring(0, 1).toLowerCase();
            }
        }
        return name;
    }
}
