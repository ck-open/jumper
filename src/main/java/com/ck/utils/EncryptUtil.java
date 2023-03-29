package com.ck.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 加密
 *
 * @author cyk
 * @since 2020-01-01
 */
public final class EncryptUtil {
    public static void main(String[] args) {
        System.out.println(getUUID());
        System.out.println(strToMD5("asdfg"));
        System.out.println(strToMD5("asdfg","dsaf55"));
        System.out.println(compileStr("dsagdfg",true));
        System.out.println(compileStr("sadgfdgfdterf5465f6sd4gf5d-dgfd",true));
    }

    private void test() throws Exception {
        String sKey="1234567890123456";
        String ivParameter="1234567890123456";

        // 需要加密的字串
        String cSrc = "123456";
        System.out.println("加密前的字串是："+cSrc);
        // 加密
        String enString = EncryptUtil.encryptAES(cSrc,"utf-8",sKey,ivParameter);
        System.out.println("加密后的字串是："+ enString);
        System.out.println("1jdzWuniG6UMtoa3T6uNLA==".equals(enString));
        // 解密
        String DeString = EncryptUtil.decryptAES(enString,"utf-8",sKey,ivParameter);
        System.out.println("解密后的字串是：" + DeString);
    }






    // 加密 AES-128-CBC
    public static String encryptAES(String sSrc, String encodingFormat, String sKey, String ivParameter) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes(encodingFormat));
        return new BASE64Encoder().encode(encrypted);//此处使用BASE64做转码。
    }
    // 解密 AES-128-CBC
    public static String decryptAES(String sSrc, String encodingFormat, String sKey, String ivParameter){
        try {
            byte[] raw = sKey.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);//先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original,encodingFormat);
            return originalString;
        } catch (Exception ex) {
            return null;
        }
    }


    /**
     * 获取uuid随机值
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }


    /**
     * 字符串进行MD5 加密
     *
     * @param plainText 加密的字符串
     * @param salt      盐值
     * @return
     */
    public static String strToMD5(String plainText, String salt) {
        return strToMD5(plainText + strToMD5(salt));
    }

    /**
     * 字符串进行MD5 加密
     *
     * @param plainText 加密的字符串
     * @return
     */
    public static String strToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        return md5code;
    }

    /**
     * 字符串自定义字典乱序加密
     *
     * @param str 加密的字符串
     * @param sw  乱序/还原   true/false
     * @return
     */
    public static String compileStr(String str, boolean sw) {
        List<String> chars = Arrays.asList("!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/"
                , ":", ";", "<", "=", ">", "?", "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~"
                , "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
                , "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
                , "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
        );

        List<String> chars1 = Arrays.asList("E", "L", "M", "$", "e", "o", "[", "m", "`", "f", "V", "D", "2", "5", "?", "c", "\"", "'", "\\"
                , "_", "w", ",", "i", "H", "0", "h", "x", "-", "j", "=", "J", "+", "d", "&", "}", "l", "]", "q", "1", "~", "!", "3"
                , "R", "n", "P", "T", "t", "6", "<", "#", "b", "k", "A", "U", "u", "F", "I", "Y", "*", "S", "G", ">", "9", "K", "^"
                , "Q", "Z", ":", "8", "{", "y", "s", "O", "z", ".", "g", "a", "@", "v", ")", "X", "%", "r", ";", "/", "|", "4", "p", "7", "W", "(", "C", "N", "B");

        List<String> chars2 = Arrays.asList("V", "T", "<", "D", ".", ",", ":", "e", "}", "$", "G", "2", "h", "@", "1", "[", "`", "P", "8", "U", "*", "Z", "l"
                , "b", "t", "Y", "W", "5", "R", "M", "=", "]", "K", "4", "0", "v", "O", "^", "~", "k", "+", "E", "_", "y", "q", "H", "3", "/", "Q"
                , "?", "g", "F", "i", "I", "m", "a", "9", "(", "{", "%", "#", "&", "n", "X", "u", "p", "C", "!", "-", ">", "A", "'", "7", "x", "c"
                , "d", "\\", "f", "|", "z", "N", "6", "s", "B", "L", "j", "S", "w", ";", "\"", ")", "J", "o", "r");


        if (str != null && !"".equalsIgnoreCase(str.trim())) {
            char[] strChar = str.toCharArray();
            // 字符串长度为偶数用格式1乱序  否则用2乱序
            List<String> charsTemp = strChar.length % 2 == 0 ? chars1 : chars2;

            StringBuilder stringBuilder = new StringBuilder();
            for (char c : strChar) {
                String item = String.valueOf(c);
                if (!chars.contains(item))
                    throw new RuntimeException("自定义的字典中不包含该字符：<" + item + ">");

                // 控制 编译和反编译
                if (sw) {
                    // 乱序编译
                    stringBuilder.append(charsTemp.get(chars.indexOf(item)));
                } else {
                    // 反编译
                    stringBuilder.append(chars.get(charsTemp.indexOf(item)));
                }
            }

            str = stringBuilder.toString();
        }

        return str;
    }
}
