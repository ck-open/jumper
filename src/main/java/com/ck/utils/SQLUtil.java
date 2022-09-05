package com.ck.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * 数据库SQL相关操作工具
 *
 * @author cyk
 * @since 2021-10-01
 */
public final class SQLUtil {
    private static Logger log = Logger.getLogger(SQLUtil.class.getName());

    /**
     * List<List<String>> 转 List<Bean> 对象
     *
     * @param list  数据参数二维数组，第一行必须为对象属性名称
     * @param setIc 转换中是否忽略key值大小写
     * @param cls   需要转换的对象类型
     * @param <T>   转换后的对象
     * @return
     * @author cyk
     * @since 2020-01-01
     */
    public static <T> List<T> parseListToBeans(List<List<String>> list, boolean setIc, Class<T> cls) {
        List<T> result = new ArrayList<>();

        // 获取类型及继承父类的所有参数
        Class clazzTemp = cls;
        List<Field> fields = new ArrayList();
        while (clazzTemp != null) {
            fields.addAll(Arrays.asList(clazzTemp.getDeclaredFields()));
            clazzTemp = clazzTemp.getSuperclass();
        }
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (setIc) {
                fieldName = fieldName.toUpperCase();
            }
            fieldMap.put(fieldName, field);
        }

        List<String> title = null;
        for (int r = 0; r < list.size(); r++) {
            List<String> row = list.get(r);

            if (r == 0) {
                title = row;
                continue;
            }
            if (row.get(0) == null || "".equals(row.get(0))) {
                continue;
            }

            Field f = null;
            Object v = null;
            try {
                T temp = cls.newInstance();
                result.add(temp);
                for (int c = 1; c < row.size(); c++) {
                    String var = row.get(c);

                    f = fieldMap.get(title.get(c));
                    if (f != null) {
                        v = var;
                        if ("".equals(var)) {
                            v = null;
                        }
                        v = parseDataType(f, v);
                        f.setAccessible(true);
                        f.set(temp, v);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("二维数组第" + (r + 1) + "行 转换实体对象（" + cls.getName() + "）失败：" + row + "\r\n"
                        + cls.getSimpleName() + "." + f.getName() + "=" + (v == null ? v : v.toString()) + "  需要的参数类型：" + f.getType().getName());
            }
        }
        return result;
    }

    /**
     * Map 转 Bean 对象
     *
     * @param map   map
     * @param setIc 转换中是否忽略key值大小写
     * @param clazz 需要转换的对象类型
     * @param <T>   转换后的对象
     * @return
     * @author cyk
     * @since 2020-01-01
     */
    public static <T> T parseMapToBean(Map map, boolean setIc, Class<?> clazz) {
        T obj = null;
        try {

            if (map != null && !map.isEmpty()) {
                if (clazz.equals(Map.class)) {
                    return (T) map;
                }
                obj = (T) clazz.newInstance();

                // 不区分大小写时 将所有key转小写
                if (setIc) {
                    Map mapTemp = new HashMap();
                    for (Object key : map.keySet()) {
                        if (key instanceof String) {
                            mapTemp.put(key.toString().toLowerCase(), map.get(key));
                        } else {
                            mapTemp.put(key, map.get(key));
                        }
                    }
                    map = mapTemp;
                }

                // 获取类型及继承父类的所有参数
                Class clazzTemp = clazz;
                List<Field> fields = new ArrayList();
                while (clazzTemp != null) {
                    fields.addAll(Arrays.asList(clazzTemp.getDeclaredFields()));
                    clazzTemp = clazzTemp.getSuperclass();
                }

                for (int i = 0; i < fields.size(); i++) {
                    Field field = fields.get(i);
                    Object value = map.get(setIc ? field.getName().toLowerCase() : field.getName());
                    if (value != null) {
                        value = parseDataType(field, value);
                        if (value == null) {
                            continue;
                        }
                        field.setAccessible(true);
                        field.set(obj, value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * 数据类型解析转换
     *
     * @param field 实体字段
     * @param value 需要转换的值
     * @return
     */
    public static Object parseDataType(Field field, Object value) {
        if (field == null) return value;
        return parseDataType(field.getType().getName(), value);
    }

    /**
     * 数据类型解析转换
     *
     * @param className 例如：String.class.getName() || Field.getType().getName()
     * @param value     new Object()
     * @return
     */
    public static Object parseDataType(String className, Object value) {
        if (value != null) {
            if (Date.class.getName().equals(className)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    String str = value.toString();
                    if (str.contains("/")) {
                        str = str.replaceAll("/", "-");
                    }
                    if (str.length() < 11) {
                        str += " 00:00:00";
                    }

                    if (str.length() < 19) {
                        String[] v = str.substring(0, str.lastIndexOf(" ")).split("-");
                        String t = "";
                        for (String i : v) {
                            t += "-";
                            if (i.length() < 2) {
                                t += "0";
                            }
                            t += i;
                        }
                        str = t.substring(1) + " " + str.substring(str.lastIndexOf(" "));
                    }

                    value = simpleDateFormat.parse(str);
                } catch (ParseException e) {
                    log.warning("==========SQLUtils  Error  数据类型解析失败，数据：" + value.toString() + "   解析成 Date（yyyy-MM-dd HH:mm:ss）类型是异常：" + e.getMessage());
                }
            } else if (String.class.getName().equals(className)) {
                value = value.toString();
            } else if (Long.class.getName().equals(className) || long.class.getName().equals(className)) {
                value = Long.parseLong(value.toString());
            } else if (Integer.class.getName().equals(className) || int.class.getName().equals(className)) {
                value = Integer.parseInt(value.toString());
            } else if (Float.class.getName().equals(className)
                    || float.class.getName().equals(className)) {
                value = Float.parseFloat(value.toString());
            } else if (Double.class.getName().equals(className)
                    || double.class.getName().equals(className)) {
                value = Double.parseDouble(value.toString());
            } else if (BigDecimal.class.getName().equals(className)) {
                value = new BigDecimal(value.toString());
            }
        }
        return value;
    }


    /**
     * 参数SQL注入校验
     *
     * @param parameter
     * @return
     */
    private static String checkInject(String parameter) {
        if (parameter != null) {
            parameter = parameter.toLowerCase();
            if (parameter.indexOf(" or ") >= 0 || parameter.indexOf("(") >= 0 || parameter.indexOf(")") >= 0 || parameter.indexOf(";") >= 0) {
                return "?";
            }
            parameter = parameter.trim();
            if (parameter.startsWith("and") || parameter.endsWith("and")) {
                return "?";
            } else if (parameter.startsWith("or") || parameter.endsWith("or")) {
                return "?";
            } else if (parameter.indexOf("(") >= 0 || parameter.indexOf(")") >= 0) {
                return "?";
            }
        }
        return parameter;
    }


    /**
     * 解析数据库查询结果集
     *
     * @param resultSet 查询结果集
     * @param cla       结果泛型
     * @param <T>
     * @return
     * @throws SQLException
     */
    public static <T> List<T> parseResultSet(ResultSet resultSet, Class<T> cla) throws SQLException {
        List<T> result = null;
        if (resultSet != null) {
            result = new ArrayList();
            ResultSetMetaData rmd = resultSet.getMetaData();
            if (rmd != null) {
                while (resultSet.next()) {
                    Map bean = new HashMap();
                    for (int i = 1; i <= rmd.getColumnCount(); i++) {
                        if (rmd.getColumnName(i) != null && resultSet.getString(i) != null) {
                            bean.put(rmd.getColumnName(i).toLowerCase(), resultSet.getString(i));
                        }
                    }
                    result.add(parseMapToBean(bean, true, cla));
                }
            }
        }
        return result;
    }


    /**
     * 解析值对象为sql语句
     *
     * @param val
     * @return
     */
    public static Object parseSqlValue(Object val) {
        if (val != null) {
            if (val instanceof String) {
                val = "'" + val.toString() + "'";
            }
            if (val instanceof Calendar) {
                val = ((Calendar) val).getTime();
            }
            if (val instanceof Date) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                val = "TO_DATE('" + simpleDateFormat.format(val) + "', 'YYYY-MM-DD HH24:MI:SS')";
            }
        }
        return val;
    }


    /**
     * Oracle 数据库操作
     */
    public abstract static class Oracle {

        /**
         * 数据新增  批量
         *
         * @param tableName 表名
         * @param pos       数据对象实例列表
         */
        public static List<String> insertBatchSql(String tableName, List pos) {
            List<String> insertSqlList = new ArrayList<>();
            if (pos != null) {
                for (int p = 0; p < pos.size(); p++) {
                    insertSqlList.add(insertSql(tableName, pos.get(p)));
                }
            }
            return insertSqlList;
        }

        /**
         * 数据新增
         *
         * @param tableName 表名
         * @param po        数据对象实例
         */
        public static String insertSql(String tableName, Object po) {
            StringBuffer parameter = new StringBuffer();
            StringBuffer values = new StringBuffer();
            if (po instanceof Map) {
                for (Object key : ((Map) po).keySet()) {
                    Object val = parseSqlValue(((Map) po).get(key));
                    parameter.append("," + key.toString());
                    values.append("," + val);
                }
            } else {
                Field[] fields = po.getClass().getDeclaredFields();
                if (fields != null) {
                    for (int i = 0; i < fields.length; i++) {
                        Field fieldItem = fields[i];
                        // 静态属性时跳过
                        if (Modifier.isStatic(fieldItem.getModifiers())) {
                            continue;
                        }
                        fieldItem.setAccessible(true);
                        try {
                            Object val = parseSqlValue(fieldItem.get(po));
                            if (val != null) {
                                parameter.append("," + fieldItem.getName());
                                values.append("," + val);
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("PO对象取" + fieldItem.getName() + "属性值时发生异常： IllegalAccessException");
                        }
                    }
                }
            }
            String parameterStr = parameter.toString();
            if ("".equals(parameterStr)) {
                throw new RuntimeException("实体对象转换SQL失败： INSERT INTO " + tableName + " (null)  VALUES (null)");
            }

            return "INSERT INTO " + tableName + " (" + parameter.toString().substring(1) + ")  VALUES (" + values.toString().substring(1) + ");";
        }

        /**
         * 生成分页查询SQL
         *
         * @param sql
         * @param pageNo
         * @param pageSize
         * @return String[0] 数据查询sql  String[1] 查询的条数sql
         * @throws Exception
         */
        public static String[] selectPage(String sql, Integer pageNo, Integer pageSize) {
            String[] pageSql = new String[2];
            if (pageNo != null && pageSize != null) {
                Integer startRow = (pageNo - 1) * pageSize;
                Integer endRow = startRow + pageSize;
                pageSql[0] = "SELECT * from (SELECT a.*, ROWNUM rn from (" + sql + ") a where rownum <=" + endRow + ") where rn >" + startRow;
            }

            pageSql[1] = "SELECT COUNT(*) AS total " + sql.substring(sql.toLowerCase().indexOf("from"));
            return pageSql;
        }
    }


    /**
     * MySql 数据库操作
     */
    public abstract static class MySql {

    }

    /**
     * SqlServer 数据库操作
     */
    public abstract static class SqlServer {

    }

    /**
     * InfluxDB 数据库操作
     */
    public abstract static class InfluxDB {

    }


    /**
     * 分页数据实体
     */
    public static class PageResult<E> {
        List<E> data;
        int pageNo;
        int pageSize;
        int pages;
        int total;

        public List<E> getData() {
            return data;
        }

        public void setData(List<E> data) {
            this.data = data;
        }

        public int getPageNo() {
            return pageNo;
        }

        public void setPageNo(int pageNo) {
            this.pageNo = pageNo;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }
}
