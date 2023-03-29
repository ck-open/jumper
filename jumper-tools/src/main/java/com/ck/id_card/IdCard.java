package com.ck.id_card;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * 身份证信息对象
 */
public class IdCard {
    private Logger log = Logger.getLogger(IdCard.class.getName());

    /**
     * 地区编码
     */
    private static final Map<String, String> Code = new HashMap<>();

    /**
     * 证件号验证状态  失败为false，原因会再message中注明
     */
    private boolean status = false;

    /**
     * 验证状态说明
     */
    private String message;

    /**
     * 身份证号
     */
    private String idCardNo;

    /**
     * 性别：1男、2女
     */
    private Integer sex;

    /**
     * 性别：1男、2女
     */
    private String sexName;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 生日 yyyy-MM-dd
     */
    private String birthday;

    /**
     * 户籍-省代码
     */
    private String addressProvinceCode;
    /**
     * 户籍-省名称
     */
    private String addressProvinceName;

    /**
     * 户籍-市代码
     */
    private String addressCityCode;
    /**
     * 户籍-市名称
     */
    private String addressCityName;

    /**
     * 户籍-区县代码
     */
    private String addressCountyCode;
    /**
     * 户籍-区县名称
     */
    private String addressCountyName;

    public IdCard(String idCardNo) {
        this.idCardNo = idCardNo;
        this.init();
    }


    /**
     * 判断输入等操作
     */
    private void init() {

        if (this.idCardNo == null || "".equalsIgnoreCase(this.idCardNo)) {
            this.message = "证件号为空。";
            return;
        }

        String id = this.idCardNo;
        if (id.length() == 18) {
            // 前17位必须都是数字
            if (!isNumber(id)) {
                this.message = "存在非法字符。";
            } else if (!checkVerifyCode(id)) {
                this.message = "校验码错误。";
            } else {
                this.status = true;
            }
        } else if (id.length() == 15) {
            id = idCard15to18(id);
            this.status = true;
        } else {
            this.message = "位数错误。";
        }

        // 解析数据
        if (this.status) {
            // 出生日期
            int year = Integer.parseInt(id.substring(6, 10));
            int month = Integer.parseInt(id.substring(10, 12));
            int day = Integer.parseInt(id.substring(12, 14));
            this.birthday = year + "-" + month + "-" + day;

            // 性别
            int x = Integer.parseInt(id.substring(16, 17));
            if (x % 2 == 0) {
                this.sexName = "女";
                this.sex = 2;
            } else {
                this.sexName = "男";
                this.sex = 1;
            }

            // 年龄
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                this.age = getAge(simpleDateFormat.parse(this.birthday));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // 户籍省、市、区县
            this.addressProvinceCode = this.idCardNo.substring(0, 2) + "0000";
            this.addressProvinceName = this.getAreaName(this.addressProvinceCode);
            this.addressCityCode = this.idCardNo.substring(0, 4) + "00";
            this.addressCityName = this.getAreaName(this.addressCityCode);
            this.addressCountyCode = this.idCardNo.substring(0, 6);
            this.addressCountyName = this.getAreaName(this.addressCountyCode);
        }
    }


    /**
     * 获取地区名称
     *
     * @param areaCode
     * @return
     */
    public String getAreaName(String areaCode) {
        if (Code.isEmpty()) {
            BufferedReader reader = null;
            try {
                File file = new File(System.getProperty("user.dir") + File.separator + "IdCardAreaCode");
//                File file = new File(this.getClass().getResource("").getPath() + "\\IdCardAreaCode");
                if (!file.isFile()) {
                    return null;
                }
                reader = new BufferedReader(new FileReader(file));
                String tempStr;
                while ((tempStr = reader.readLine()) != null) {
                    if (tempStr.startsWith("//") || tempStr.startsWith("-")) {
                        continue;
                    }
                    String[] temp = tempStr.split(",");
                    if (temp.length > 1) {
                        Code.put(temp[0], temp[1]);
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        if (Code.get(areaCode) != null) {
            return Code.get(areaCode);
        }
        return null;
    }


    /**
     * 根据出生日期计算年龄
     *
     * @param birthDay 生日
     * @return
     */
    private static Integer getAge(Date birthDay) {
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthDay)) { //出生日期晚于当前时间，无法计算
            throw new IllegalArgumentException("The birthDay is before Now.It's unbelievable!");
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
     * 判断输入是否全是数字
     *
     * @param idCard
     * @return
     */

    private boolean isNumber(String idCard) {
        int size = idCard.length();
        if (size == 18) {
            size--;
        }
        int ch;
        for (int i = 0; i < size; i++) {
            ch = idCard.codePointAt(i);
            if (ch < 48 || ch > 57) {
                return false;
            }
        }
        return true;
    }

    /**
     * 身份证校验码验证
     *
     * @param idCard
     * @return
     */
    private boolean checkVerifyCode(String idCard) {
        // 最后一位只能是“X”或者数字
        String last = idCard.substring(17, 18);
        if (last.equals("X") || isNumber(last)) {
            //生成校验码与输入值比较
            if (calculateVerifyCode(idCard).equals(last)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 计算校验码
     *
     * @param idCard
     * @return VerifyCode
     * @Title: calculateVerifyCode
     * @Description: 校验码（第十八位数）
     * 十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0...16 ，先对前17位数字的权求和
     * Ai:表示第i位置上的身份证号码数字值
     * Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
     * 计算模 Y = mod(S, 11)
     * 通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 X 9 8 7 6 5 4 3 2
     */
    private String calculateVerifyCode(String idCard) {
        String[] codes = new String[]{"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            // 加权因子的算法
            int ai = idCard.charAt(i) - '0';
            int wi = (int) ((Math.pow(2, 17 - i)) % 11);
            sum += ai * wi;
        }
        sum %= 11;
        return codes[sum];
    }

    /**
     * 15位身份证号码转化为18位的身份证。如果是18位的身份证则直接返回，不作任何变化。
     *
     * @param idCard,15位的有效身份证号码
     * @return idCard18 返回18位的有效身份证
     */
    public String idCard15to18(String idCard) {
        if (!isNumber(idCard)) {
            log.warning("错误：输入15位，存在非数字字符。");
        } else {
            // 格式转换
            idCard = idCard.substring(0, 6) + "19" + idCard.substring(6);
            idCard += calculateVerifyCode(idCard);
            log.warning("输入15位，转换为新号码为：" + idCard);
        }
        return idCard;
    }


    /**
     * 调用三方查询
     *
     * @param id
     * @param key
     * @return
     */
    public static String seatchID(String id, String key) {
        String url = "http://apis.juhe.cn/idcard/index?key=" + key + "&cardno=" + id;
        URL urlNet = null;
        InputStream is = null;
        ByteArrayOutputStream bao = null;
        String result = null;
        try {
            urlNet = new URL(url);
            try {
                HttpURLConnection conn = (HttpURLConnection) urlNet.openConnection();
                conn.setReadTimeout(5 * 1000);
                conn.setRequestMethod("GET");
                is = conn.getInputStream();
                int len = -1;
                byte[] buf = new byte[128];
                bao = new ByteArrayOutputStream();
                while ((len = is.read(buf)) != -1) {
                    bao.write(buf, 0, len);

                }
                bao.flush();
                result = new String(bao.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bao != null) {
                try {
                    bao.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {


        IdCard idCard = new IdCard("152327199202153518");

        System.out.println(idCard.age);
        System.out.println(idCard.getAddressProvinceName());
        System.out.println(idCard.getAddressCityName());
        System.out.println(idCard.getAddressCountyName());
    }


    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getSexName() {
        return sexName;
    }

    public void setSexName(String sexName) {
        this.sexName = sexName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAddressProvinceCode() {
        return addressProvinceCode;
    }

    public void setAddressProvinceCode(String addressProvinceCode) {
        this.addressProvinceCode = addressProvinceCode;
    }

    public String getAddressProvinceName() {
        return addressProvinceName;
    }

    public void setAddressProvinceName(String addressProvinceName) {
        this.addressProvinceName = addressProvinceName;
    }

    public String getAddressCityCode() {
        return addressCityCode;
    }

    public void setAddressCityCode(String addressCityCode) {
        this.addressCityCode = addressCityCode;
    }

    public String getAddressCityName() {
        return addressCityName;
    }

    public void setAddressCityName(String addressCityName) {
        this.addressCityName = addressCityName;
    }

    public String getAddressCountyCode() {
        return addressCountyCode;
    }

    public void setAddressCountyCode(String addressCountyCode) {
        this.addressCountyCode = addressCountyCode;
    }

    public String getAddressCountyName() {
        return addressCountyName;
    }

    public void setAddressCountyName(String addressCountyName) {
        this.addressCountyName = addressCountyName;
    }
}
