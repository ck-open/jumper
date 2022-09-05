package com.ck.utils;

import java.util.logging.Logger;

/**
 * 统一社会信用码工具类
 * Unified social credit code
 *
 * @author cyk
 * @since 2020-01-01
 */
public final class USCCUtil {
    private static Logger log = Logger.getLogger(USCCUtil.class.getName());
    /*
        根据中华人民共和国国家标准GB32100-2015:法人和其他组织统一社会信用代码编码规则。

        统一社会信用代码的构成
        社会组织统一社会信用代码是为每个社会组织发放一个唯一的、终身不变的主题标识代码，并以其为载体采集、查询、共享、比对各类主体信用信息，设计为18位。如图：

        统一代码的具体赋码规则如下：
            第一部分（第1位）：登记管理部门代码。暂按国务院序列规则，5表示民政部门。
            第二部分（第2位）：机构类别代码。“1”表示社会团体、“2”表示民办非企业单位、“3”表示基金会、“9”表示其他。
            第三部分（第3-8位）：登记管理机关行政区划码，参照GB/T 2260中华人民共和国行政区划代码标准。（登记机关所在地的行政区划）。
            第四部分（第9-17位）：主体标识码（组织机构代码），其中第17位为主体标识码（组织机构代码）的校验码。第17位校验码算法规则按照《全国组织机构代码编制规则》（国标GB11714—1997）计算。
            第五部分（第18位）：统一社会信用代码的校验码。第18位校验码算法规则按照《GB -2015 法人及其他组织统一社会信用代码编制规则》计算。

        代码字符集对应顺序为:字符0-9对应数字0-9，字母A-Z中去掉I、O、S、V、Z后剩下的20个依次对应数字10-30
     */

    public static void main(String[] args) {
//        toNumber('Z');
        String[] test = {"51420000MJH2003664","51429900M7H200408","52429999MJH23","51420000MJH200395N","51420000MJH2003791 ", "53420000MJH2448303","52420000MJH233402K","52420009MH2333813","52420000MJH23339XT","5142000MJH200387U"};

        for (String code : test){
            System.out.println(check(code));
        }
    }


    public static boolean check(String code) {

        if (code.length() != 18) {
            log.info("统一社会信用码[" + code + "]长度非18位！");
            return false;
        }

        int weight[] = {1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28}; // 权值
        char tempC;
        int tempSum = 0;
        int tempNum = 0;

        for (int i = 0; i <= 16; i++) {
            tempC = code.charAt(i);

            if (i == 0) {
                if (tempC != '1' && tempC != '5' && tempC != '9' && tempC != 'Y') {
                    log.info("统一社会信用码[" + code + "]中第1位，登记管理部门代码错误！部门代码包括[1、0、5、Y]");
                    return false;
                }
            }

            if (i == 1) {
                if (tempC != '1' && tempC != '2' && tempC != '3' && tempC != '9') {
                    log.info("统一社会信用码[" + code + "]中第2位，机构类别代码错误！机构类别代码包括[1、2、3、5]");
                    return false;
                }
            }

            tempNum = charToNum(tempC);
            if (tempNum == -1) {
                log.info("统一社会信用码[" + code + "]中第" + (i + 1) + "位，代码错误！代码包括[0-9 | A-H | J-N | P-R | T | U | W | X]");
                return false;
            }
            tempSum += weight[i] * tempNum;
        }

        tempNum = 31 - tempSum % 31;
        if (tempNum == 31) tempNum = 0;
        if (charToNum(code.charAt(17)) != tempNum){
            log.info("统一社会信用码[" + code + "]中第最后一位，权值错误！");
            return false;
        }
        return true;
    }


    /**
     * 代码字符转数字
     * 代码字符集对应顺序为:字符0-9对应数字0-9，字母A-Z中去掉I、O、S、V、Z后剩下的20个依次对应数字10-30
     *
     * @param code
     * @return
     */
    public static int toNumber(char code) {
        int index = 0;
        for (int i = '0'; i < 'Z'; i++) {
            char c = (char) i;
            if (i < 10) {
                c = (char) index;
            }
            if (c == 'I' || c == 'O' || c == 'S' || c == 'V' || (c > '9' && c < 'A')) {
                continue;
            }
            int number = index++;
//            System.out.println(String.format("case '%s':return %s;", c, number));
            if (code == i)
                return number;
        }
        return -1;
    }

    /**
     * 代码字符转数字
     * 代码字符集对应顺序为:字符0-9对应数字0-9，字母A-Z中去掉I、O、S、V、Z后剩下的20个依次对应数字10-30
     *
     * @param c
     * @return
     */
    public static int charToNum(char c) {
        switch (c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'A':
                return 10;
            case 'B':
                return 11;
            case 'C':
                return 12;
            case 'D':
                return 13;
            case 'E':
                return 14;
            case 'F':
                return 15;
            case 'G':
                return 16;
            case 'H':
                return 17;
            case 'J':
                return 18;
            case 'K':
                return 19;
            case 'L':
                return 20;
            case 'M':
                return 21;
            case 'N':
                return 22;
            case 'P':
                return 23;
            case 'Q':
                return 24;
            case 'R':
                return 25;
            case 'T':
                return 26;
            case 'U':
                return 27;
            case 'W':
                return 28;
            case 'X':
                return 29;
            case 'Y':
                return 30;
            default:
                return -1;
        }
    }
}
