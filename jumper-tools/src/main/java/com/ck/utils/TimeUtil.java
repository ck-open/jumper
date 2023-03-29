package com.ck.utils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 时间业务操作工具
 *
 * @author cyk
 * @since 2020-01-01
 */
public final class TimeUtil {
    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL_SIMPLE_DATE_FORMAT_s = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL_SIMPLE_DATE_FORMAT_m = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL_SIMPLE_DATE_FORMAT_d = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL_SIMPLE_DATE_FORMAT_M = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL_SIMPLE_DATE_FORMAT_y = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy");
        }
    };

    private static String[] WEEK_DAY_STRING = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    /**
     * 时间字符串解析<br>
     *
     * @param time        时间字符串
     * @param isStartTime 是否时开始时间
     * @param interval    数据间隔粒度调整开始结束时间：y、M、d、h、m、s、w
     * @return
     */
    public static Date parseTime(String time, boolean isStartTime, String interval) {
        Date date = parseTime(time);
        return parseTime(date, isStartTime, interval);
    }

    /**
     * 时间字符串解析<br>
     *
     * @param date        时间字符串
     * @param isStartTime 是否时开始时间
     * @param interval    数据间隔粒度调整开始结束时间：y、M、d、h、m、s、w、q（季度）、yh（半年）
     * @return
     */
    public static Date parseTime(Date date, boolean isStartTime, String interval) {
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            if (interval != null && !"".equalsIgnoreCase(interval.trim())) {
                if (!"M".equals(interval)) {
                    interval = interval.toLowerCase();
                }

                if (isStartTime) {
                    switch (interval) {
                        case "y":
                            calendar.set(calendar.get(Calendar.YEAR), 0, 1, 0, 0, 0);
                            break;
                        case "M":
                            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0);
                            break;
                        case "d":
                            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                            break;
                        case "h":
                            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
                            break;
                        case "m":
                            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                            break;
                        case "s":
                            break;
                        case "w":
                            calendar.add(Calendar.DATE, 2 - (calendar.get(Calendar.DAY_OF_WEEK) == 1 ? 8 : calendar.get(Calendar.DAY_OF_WEEK)));
                            break;
                        case "q":  // 季度
                            int month = calendar.get(Calendar.MONTH);
                            if (month <= 2) {
                                calendar.set(calendar.get(Calendar.YEAR), 0, 1, 0, 0, 0);
                            } else if (month <= 5) {
                                calendar.set(calendar.get(Calendar.YEAR), 3, 1, 0, 0, 0);
                            } else if (month <= 8) {
                                calendar.set(calendar.get(Calendar.YEAR), 6, 1, 0, 0, 0);
                            } else {
                                calendar.set(calendar.get(Calendar.YEAR), 9, 1, 0, 0, 0);
                            }
                            break;
                        case "yh":  // 半年
                            if (calendar.get(Calendar.MONTH) >= 5) {
                                calendar.set(calendar.get(Calendar.YEAR), 5, 1, 0, 0, 0);
                            } else {
                                calendar.set(calendar.get(Calendar.YEAR), 0, 1, 0, 0, 0);
                            }
                            break;
                    }
                } else {
                    switch (interval) {
                        case "y":
                            calendar.set(calendar.get(Calendar.YEAR), 11, 31, 23, 59, 59);
                            break;
                        case "M":
                            int dayLast = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), dayLast, 23, 59, 59);
//                            calendar.roll(Calendar.DAY_OF_MONTH,-1);  // 天回滚到最后一天
                            break;
                        case "d":
                            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
                            break;
                        case "h":
                            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), 59, 59);
                            break;
                        case "m":
                            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 59);
                            break;
                        case "s":
                            break;
                        case "w":
                            calendar.add(Calendar.DATE, 7 - (calendar.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : calendar.get(Calendar.DAY_OF_WEEK) - 1));
                            break;
                        case "q":  // 季度
                            int month = calendar.get(Calendar.MONTH);
                            if (month <= 2) {
                                calendar.set(calendar.get(Calendar.YEAR), 2, 31, 23, 59, 59);
                            } else if (month <= 5) {
                                calendar.set(calendar.get(Calendar.YEAR), 5, 30, 23, 59, 59);
                            } else if (month <= 8) {
                                calendar.set(calendar.get(Calendar.YEAR), 8, 30, 23, 59, 59);
                            } else {
                                calendar.set(calendar.get(Calendar.YEAR), 11, 31, 23, 59, 59);
                            }
                            break;
                        case "yh":  // 半年
                            if (calendar.get(Calendar.MONTH) >= 5) {
                                calendar.set(calendar.get(Calendar.YEAR), 11, 31, 23, 59, 59);
                            } else {
                                calendar.set(calendar.get(Calendar.YEAR), 5, 30, 23, 59, 59);
                            }
                            break;
                    }
                }
            }

            // 开始时间调整  否则结束时间调整
            if (interval == null || "y".equalsIgnoreCase(interval) || "M".equals(interval) || "d".equalsIgnoreCase(interval) || "w".equalsIgnoreCase(interval)) {
//                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
//                if (!isStartTime) {   // 如果是结束时间  则天+1  调整为下个天的开始时间
//                    calendar.add(Calendar.DAY_OF_YEAR, +1);
//                }
            }
            date = calendar.getTime();
        }
        return date;
    }

    /**
     * 时间类型转换-格式盲转 支持-或/
     *
     * @param time
     * @return
     */
    public static Date parseTimeBlind(String time) {
        if (time == null) {
            return null;
        }

        if (time.indexOf("/") != -1) {
            time = time.replaceAll("/", "-");
        }

        String[] temp = time.indexOf(" ") != -1 ? time.split(" ") : new String[]{time};
        if (temp[0].length() < 10) {
            String[] s = temp[0].split("-");

            for (int i = 0; i < s.length; i++) {
                if (i == 0 && s[i].length() < 4) {
                    s[i] = THREAD_LOCAL_SIMPLE_DATE_FORMAT_y.get().format(new Date());
                } else if (s[i].length() < 2) {
                    s[i] = 0 + s[i];
                }
            }
            if (s.length == 0) {
                temp[0] = THREAD_LOCAL_SIMPLE_DATE_FORMAT_y.get().format(new Date()) + "-01-01";
            } else if (s.length == 1) {
                temp[0] = s[0] + "-01-01";
            } else if (s.length == 2) {
                temp[0] = s[0] + "-" + s[1] + "-01";
            } else {
                temp[0] = s[0] + "-" + s[1] + "-" + s[2];
            }
        }

        time = temp[0];
        if (temp.length > 1) {
            if (temp[1].indexOf(":") != -1) {
                String[] s = temp[1].split(":");

                for (int i = 0; i < s.length; i++) {
                    if (s[i].length() < 2) {
                        s[i] = 0 + s[i];
                    }
                }
                if (s.length == 0) {
                    temp[1] = "00:00:00";
                } else if (s.length == 1) {
                    temp[1] = s[0] + ":00:00";
                } else if (s.length == 2) {
                    temp[1] = s[0] + ":" + s[1] + ":00";
                } else {
                    temp[1] = s[0] + ":" + s[1] + ":" + s[2];
                }
            }

            time += " " + temp[1];
        }

        return parseTime(time);
    }

    /**
     * 时间字符串解析
     *
     * @param time
     * @return
     */
    public static Date parseTime(String time) {
        if (time != null && !"".equalsIgnoreCase(time.trim())) {
            time = time.trim();
            time = time.replaceAll("/", "-").replaceAll("\\\\", "-");
            if (time.lastIndexOf("-") < 0) {
                time += "-01-01";
            } else if (time.lastIndexOf("-") < 7) {
                time += "-01";
            }
            // 解析开始结束时间
            if (time.indexOf(":") < 0) {
                time += " 00:00:00";
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return simpleDateFormat.parse(time);
            } catch (ParseException e) {
                System.out.println("==> TonageServerImpl  parseTime()  时间格式转换异常！  time: " + time);

            }
        }
        return null;
    }

    /**
     * 获取当前季度名称  2020-1季度
     *
     * @param time
     * @return
     */
    public static String getTimeQuarterStr(Date time) {
        return parseDateToString_Q(time) + "季度";
    }

    /**
     * 获取当前季度字符串  2020-1
     *
     * @param time
     * @return
     */
    public static String parseDateToString_Q(Date time) {
        Calendar calendar = Calendar.getInstance();
        if (time != null)
            calendar.setTime(time);
        return calendar.get(Calendar.YEAR) + "-" + getQuarter(calendar);
    }

    /**
     * 获取季度数字
     *
     * @param calendar
     * @return
     */
    public static int getQuarter(Calendar calendar) {
        if (calendar == null) calendar = Calendar.getInstance();

        if (calendar.get(Calendar.MONTH) <= 2) {
            return 1;
        } else if (calendar.get(Calendar.MONTH) <= 5) {
            return 2;
        } else if (calendar.get(Calendar.MONTH) <= 8) {
            return 3;
        } else {
            return 4;
        }
    }

    /**
     * 按周格式化时间字符串
     *
     * @param time 时间
     * @return
     */
    public static String formatWeekOfYear(Date time) {
        return formatWeekOfYear(time, null);
    }

    /**
     * 按周格式化时间字符串
     *
     * @param time   时间
     * @param suffix
     * @return
     */
    public static String formatWeekOfYear(Date time, String suffix) {
        if (time != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
                calendar.add(Calendar.DAY_OF_WEEK, -1);
            }
            return calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.WEEK_OF_YEAR) + (suffix == null ? "" : suffix);
        }
        return null;
    }

    /**
     * 获取周中的天字符串<br>
     * 格式：周一...周日
     *
     * @param date
     * @return
     */
    public static String getWeekDayStr(Date date) {
        if (date != null) {
            return WEEK_DAY_STRING[getDayOfWeek(date) - 1];
        }
        return null;
    }

    /**
     * 获取周中的天<br>
     * 周一为第一天
     *
     * @param date
     * @return
     */
    public static Integer getDayOfWeek(Date date) {
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            return day == 1 ? 7 : day - 1;
        }
        return null;
    }

    /**
     * 将时间格式化成字符串
     *
     * @param date
     * @param unit
     * @return
     */
    public static String parseDateToString(Date date, String unit) {
        if (date != null) {
            if ("w".equalsIgnoreCase(unit)) {  // 周
                return formatWeekOfYear(date);
            } else if ("y".equalsIgnoreCase(unit)) { // 年
                return parseDateToString_y(date);
            } else if ("M".equals(unit)) { // 月
                return parseDateToString_M(date);
            } else if ("d".equalsIgnoreCase(unit)) { // 日
                return parseDateToString_d(date);
            } else if ("h".equalsIgnoreCase(unit) || "m".equalsIgnoreCase(unit)) { // 时 或 分
                return parseDateToString_m(date);
            } else if ("s".equalsIgnoreCase(unit) || "interval".equalsIgnoreCase(unit) || "Adapt".equalsIgnoreCase(unit) || "AdaptInterval".equalsIgnoreCase(unit)) { // 秒 或自适应
                return parseDateToString_s(date);
            }
        }
        return "-";
    }

    /**
     * 将时间格式化成字符串
     *
     * @param date
     * @return
     */
    public static String parseDateToString_y(Date date) {
        if (date != null)
            return THREAD_LOCAL_SIMPLE_DATE_FORMAT_y.get().format(date);
        return null;
    }

    /**
     * 将时间格式化成字符串
     *
     * @param date
     * @return
     */
    public static String parseDateToString_M(Date date) {
        if (date != null)
            return THREAD_LOCAL_SIMPLE_DATE_FORMAT_M.get().format(date);
        return null;
    }

    /**
     * 将时间格式化成字符串
     *
     * @param date
     * @return
     */
    public static String parseDateToString_d(Date date) {
        if (date != null)
            return THREAD_LOCAL_SIMPLE_DATE_FORMAT_d.get().format(date);
        return null;
    }

    /**
     * 将时间格式化成字符串
     *
     * @param date
     * @return
     */
    public static String parseDateToString_m(Date date) {
        if (date != null)
            return THREAD_LOCAL_SIMPLE_DATE_FORMAT_m.get().format(date);
        return null;
    }

    /**
     * 将时间格式化成字符串
     *
     * @param date
     * @return
     */
    public static String parseDateToString_s(Date date) {
        if (date != null)
            return THREAD_LOCAL_SIMPLE_DATE_FORMAT_s.get().format(date);
        return null;
    }

    /**
     * 时间进行同比或环比 调整
     *
     * @param time  调整的时间
     * @param unit  调整的时间单位：w/y/M/d/h/m/s  周/年/月/日/时
     * @param isQoq 是否调整环比：true/false  同比（年减一）环比
     * @return
     */
    public static Date getYoYOrQoQ(Date time, String unit, Boolean isQoq) {
        // 查询的数据 范围时间 按照 同比、环比、当前数据 进行调整
        if (time != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);

            if (isQoq != null && isQoq) {  // 环比
                if ("w".equalsIgnoreCase(unit)) {  // 周
                    calendar.add(Calendar.WEEK_OF_YEAR, -1);
                } else if ("y".equalsIgnoreCase(unit)) { // 年
                    calendar.add(Calendar.YEAR, -1);
                } else if ("Q".equalsIgnoreCase(unit)) { // 季度 quarter
//                    int month = calendar.get(Calendar.MONTH);
//                    if (month==1 || month==4||month==7||month==10  || month==3 || month==6 || month==9 || month==12 ) {
//                        calendar.add(Calendar.MONTH, -3);
//                    }
                    calendar.add(Calendar.MONTH, -3);

                } else if ("YH".equals(unit)) { // 半年  Years_Half
                    calendar.add(Calendar.MONTH, -6);
                } else if ("M".equals(unit)) { // 月
                    calendar.add(Calendar.MONTH, -1);
                } else if ("d".equalsIgnoreCase(unit)) { // 日
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                } else if ("h".equalsIgnoreCase(unit)) { // 时
                    calendar.add(Calendar.HOUR_OF_DAY, -1);
                } else if ("m".equals(unit)) { // 时
                    calendar.add(Calendar.MINUTE, -1);
                } else if ("s".equalsIgnoreCase(unit)) { // 时
                    calendar.add(Calendar.SECOND, -1);
                }
            } else {  // 同比
                calendar.add(Calendar.YEAR, -1);
            }
            return calendar.getTime();
        }
        return null;
    }


    /**
     * 按照开始和结束时间 生成 趋势时间轴数组
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param interval  间隔粒度
     * @return
     */
    public static List<Date> createTrendTimes(Date startTime, Date endTime, String interval) {
        if (startTime != null && endTime != null) {
            List<Date> result = new ArrayList<>();
            Calendar time = Calendar.getInstance();
            time.setTime(startTime);

            while (time.getTime().getTime() <= endTime.getTime()) {
                result.add(time.getTime());

                if ("w".equalsIgnoreCase(interval)) {  // 周
                    time.add(Calendar.WEEK_OF_YEAR, 1);
                } else if ("y".equalsIgnoreCase(interval)) { // 年
                    time.add(Calendar.YEAR, 1);
                } else if ("M".equals(interval)) { // 月
                    time.add(Calendar.MONTH, 1);
                } else if ("d".equalsIgnoreCase(interval)) { // 日
                    time.add(Calendar.DAY_OF_MONTH, 1);
                } else if ("h".equalsIgnoreCase(interval)) { // 时
                    time.add(Calendar.HOUR_OF_DAY, 1);
                } else if ("m".equals(interval)) { // 分
                    time.add(Calendar.MINUTE, 1);
                } else if ("s".equalsIgnoreCase(interval)) { // 秒
                    time.add(Calendar.SECOND, 1);
                }
            }
            return result;
        }
        return null;
    }


    /**
     * 获取自适应数据间隔粒度<br>
     * 最小间隔为60s(秒)，最大数据个数限制500个
     *
     * @param startTime 数据范围 开始时间
     * @param endTime   数据范围 结束时间
     * @return 返回的间隔值单位为s(秒)
     */
    public static Integer getAdaptInterval(Date startTime, Date endTime) {
        return getAdaptInterval(startTime, endTime, 60, 500);
    }

    /**
     * 获取自适应数据间隔粒度
     *
     * @param startTime   数据范围 开始时间
     * @param endTime     数据范围 结束时间
     * @param minInterval 最小间隔阈值，单位为s(秒)
     * @param maxParticle 最大数据个数限制阈值
     * @return 返回的间隔值单位为s(秒)
     */
    public static Integer getAdaptInterval(Date startTime, Date endTime, int minInterval, double maxParticle) {
        if (startTime != null && endTime != null) {
            // 自适应  间隔时间粒度m = （（结束时间 - 开始时间（秒值）） / 默认间隔粒度） < 间隔粒度阈值上限 ？ 默认间隔粒度 ： （结束时间 - 开始时间（秒值）） / 间隔粒度阈值上限
            long timeInterval = (endTime.getTime() - startTime.getTime()) / 1000;  // 时间间隔秒值
            double totalParticle = timeInterval / minInterval;
            int intervalTemp = totalParticle < maxParticle ? minInterval : new BigDecimal(timeInterval).divide(new BigDecimal(maxParticle), 0, BigDecimal.ROUND_HALF_UP).intValue();
            return intervalTemp;
        }
        return null;
    }

    /**
     * 解析字符串为时间类型
     *
     * @param time    时间戳字符串
     * @param pattern 格式，默认：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Date format(String time, String pattern) {
        if (pattern == null) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        try {
            return simpleDateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析时间类型为字符串
     *
     * @param date
     * @param pattern 格式，默认：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String format(Date date, String pattern) {
        if (pattern == null) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    /**
     * 根据出生日期计算年龄
     *
     * @param birthDay 生日
     * @return
     */
    public static Integer getAge(Date birthDay) {
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthDay)) { //出生日期晚于当前时间，无法计算
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);  //当前年份
        int monthNow = cal.get(Calendar.MONTH);  //当前月份
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH); //当前日期
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
        int age = yearNow - yearBirth;   //计算整岁数
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) age--;//当前日期在生日之前，年龄减一
            } else {
                age--;//当前月份在生日之前，年龄减一
            }
        }
        return age;
    }


    /**
     * 获取两个日期之间的天数
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static Integer getDaysOfBetween(Date startDate, Date endDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(startDate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Math.abs(Integer.parseInt(String.valueOf(between_days)));
    }


    public static void main(String[] args) {
        Date nowTime = TimeUtil.parseTime("2020-10-08");
        Date time = TimeUtil.parseTimeBlind("20-1-7 1:25");
        System.out.println(nowTime.after(time));

    }

}
