package com.ck.core.mybatis;

import com.ck.function.JavaCompilerUtils;

/**
 * @ClassName DynamicLoadingMapper
 * @Description 动态生成加载Mapper工具类
 * @Author Cyk
 * @Version 1.0
 * @since 2023/3/31 9:51
 **/
public class DynamicLoadingMapper {

    public static void main(String[] args) {
        String classStr = getBaseMapperJavaSource("User", "");
        JavaCompilerUtils.compilerString(classStr);

    }

    private static final String packagePath = "com.ck.db.mapper";
    private static final StringBuilder sourceCodeFormat = new StringBuilder();

    static {
        sourceCodeFormat
                .append("package ").append(packagePath).append(";")
                .append("import com.baomidou.mybatisplus.core.mapper.BaseMapper;")
                .append("public interface PoClassNameMapper extends BaseMapper<PoClassName> {")
                .append("@TableName(value = \"SQLTableName\")")
                .append(" static class PoClassName implements Serializable{")

                .append("SQLFields")
                .append("}")
                .append("}");
    }

    /**
     * 根据自定义 Sql 生成 MyBatis Plus BaseMapper<PO> 接口源码
     *
     * @param poClassName
     * @param sql
     * @return
     */
    public static String getBaseMapperJavaSource(String poClassName, String sql) {
        String javaSource = sourceCodeFormat.toString();
        javaSource = javaSource.replaceAll("PoClassName", poClassName);
        javaSource = javaSource.replaceAll("SQLTableName", getSqlTableName(sql));
        javaSource = javaSource.replaceAll("SQLFields", getSqlPoFields(sql));
        return javaSource;
    }

    /**
     * 获取自定义 sql 表名
     *
     * @param sql
     * @return
     */
    public static String getSqlTableName(String sql) {
        StringBuilder sqlTableName = new StringBuilder();

        sqlTableName.append("user");
        return sqlTableName.toString();
    }

    /**
     * 获取自定义 sql Po字段
     *
     * @param sql
     * @return
     */
    public static String getSqlPoFields(String sql) {
        StringBuilder sqlFields = new StringBuilder();
        sqlFields.append("@TableField(\"a.name\")")
                .append("private String name;");

        return sqlFields.toString();
    }
}
