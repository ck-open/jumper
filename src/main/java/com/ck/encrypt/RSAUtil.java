package com.ck.encrypt;


import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * 签名加密
 *
 * @author cyk
 * @return
 * @since 2020-01-01
 */
public final class RSAUtil {
    private static final Logger log = Logger.getLogger(RSAUtil.class.getName());
    /**
     * 签名加密算法
     * <p>将正文通过MD5加密后,将密文再次通过生成的RSA密钥加密,生成数字签名</p>
     */
    private static final String algorithm_MD5withRSA = "MD5withRSA";
    /**
     * 签名加密算法
     * <p>将正文通过sha1哈希加密后,将密文再次通过生成的RSA密钥加密,生成数字签名</p>
     */
    private static final String Algorithm_SHA1WithRSA = "SHA1WithRSA";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * Base64 编码字符串转换
     * @param encryptedData
     * @return
     */
    private static String base64EncodeToString(byte[] encryptedData) {
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * Base64 加密
     *
     * @return
     */
    public static String base64Encrypt(String str) {
        String encoder = Base64.getEncoder().encodeToString(str.getBytes());
        return encoder;
    }

    /**
     * Base64 加密
     *
     * @return
     */
    public static String base64Encrypt(byte[] data) {
        return new BASE64Encoder().encodeBuffer(data);
    }

    /**
     * Base64 解密
     *
     * @return
     */
    public static String base64DecryptString(String str) {
        return new String(Objects.requireNonNull(base64Decrypt(str)));
    }

    /**
     * Base64 解密
     *
     * @return
     */
    public static byte[] base64Decrypt(String str) {
        try {
            return new BASE64Decoder().decodeBuffer(str);
        } catch (IOException e) {
            log.warning(String.format("Base64 Decrypt Error：%s", e.getMessage()));
        }
        return null;
    }

    /**
     * 获取密钥对
     *
     * @return 密钥对
     */
    public static KeyPair getKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(512);
        return generator.generateKeyPair();
    }

    /**
     * 获取私钥
     *
     * @param privateKey 私钥字符串
     * @return
     */
    public static PrivateKey getPrivateKey(String privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] decodedKey = base64Decrypt(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 获取公钥
     *
     * @param publicKey 公钥字符串
     * @return
     */
    public static PublicKey getPublicKey(String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] decodedKey = base64Decrypt(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * RSA加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return
     */
    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        int inputLen = data.getBytes().length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data.getBytes(), offset, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data.getBytes(), offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        // 获取加密内容使用base64进行编码,并以UTF-8为标准转化成字符串
        // 加密后的字符串
        return new String(base64EncodeToString(encryptedData));
    }


    /**
     * RSA解密
     *
     * @param data       待解密数据
     * @param privateKey 私钥
     * @return
     */
    public static String decrypt(String data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] dataBytes = base64Decrypt(data);
        int inputLen = dataBytes.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(dataBytes, offset, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        // 解密后的内容
        return new String(decryptedData, "UTF-8");
    }

    /**
     * 签名
     *
     * @param data       待签名数据
     * @param privateKey 私钥
     * @return 签名
     */
    public static String sign(String data, PrivateKey privateKey) throws Exception {
        byte[] keyBytes = privateKey.getEncoded();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey key = keyFactory.generatePrivate(keySpec);
        Signature signature = Signature.getInstance(Algorithm_SHA1WithRSA);
        signature.initSign(key);
        signature.update(data.getBytes());
        return base64Encrypt(signature.sign());
    }

    /**
     * 验签
     *
     * @param srcData   原始字符串
     * @param publicKey 公钥
     * @param sign      签名
     * @return 是否验签通过
     */
    public static boolean verify(String srcData, PublicKey publicKey, String sign) throws Exception {
        byte[] keyBytes = publicKey.getEncoded();
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey key = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(Algorithm_SHA1WithRSA);
        signature.initVerify(key);
        signature.update(srcData.getBytes());
        return signature.verify(base64Decrypt(sign));
    }


    /**
     * 测试
     */
    public static void test() {
        try {
            // 生成密钥对
            KeyPair keyPair = getKeyPair();
            String privateKey = base64Encrypt(keyPair.getPrivate().getEncoded());
            String publicKey = base64Encrypt(keyPair.getPublic().getEncoded());
            System.out.println("私钥:" + privateKey);
            System.out.println("公钥:" + publicKey);
            // RSA加密
            String data = "待加密的文字内容";
            String encryptData = encrypt(data, getPublicKey(publicKey));
            System.out.println("加密后内容:" + encryptData);
            // RSA解密
            String decryptData = decrypt(encryptData, getPrivateKey(privateKey));
            System.out.println("解密后内容:" + decryptData);

            // RSA签名
            String sign = sign(data, getPrivateKey(privateKey));
            // RSA验签
            boolean result = verify(data, getPublicKey(publicKey), sign);
            System.out.println("验签结果:" + result);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("加解密异常");
        }
    }

    public static void main(String[] args) throws Exception {
        test();
//        yaoL();
    }


    public static void yaoL() throws Exception {
        String privateKey =
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCsvSsELH6Df8v8" +
                        "umY0bwKhvM/Q+9QC9Z4OCd9iDyW8TI125Dy/lYDsjvz7I1VozKFJZaHHuq5JzK2n" +
                        "YSYJ5+y8YpegHbBLMXc8zvzA39p6p1mK8lZZtXvSlzI83PS1dYsDgFRU7+Eul38X" +
                        "Mr66AoY9pNj6D0XnXIWMBKxDy3BPAfCjzVPtcweZXed3QoproDZaSLxI8Kb3b4I5" +
                        "nluPwHaGHZjvlhh/C5sVmjlIGr9ltbBxKrrzcKuQYI6enYKixrhhgnajWeXGdh+W" +
                        "OwfDmzTTae12u0kBXtVbvoD6tcMG0NsA9qfgITYKpr3/rgfuoXlulAxR7dNPxwLu" +
                        "R5FvcRDLAgMBAAECggEABthULQ0qUkGtLq2gQTGo9AnMXmasRxW+lqXTgUpCX4zV" +
                        "CkIPGjRtcHJWGgmTZ9y8A3GQkJ6YK3p94yUZYckb+3cYtRKYGtKx++nj4Cy+tp+N" +
                        "D9F8lH95kaXGKt1CoTIwOQpLzTXYQYVlVuD+59YTxbuTkY0rYviqnhrNTL5yC99i" +
                        "HuDxwsBthSU8ZubRyfnFmudhrSB5bQxsYwd52qzliRc94SE9Vp0FjPwn7nNTo8AC" +
                        "x5tpe9NCPvqx3AsgWLaDhba1l3cyP7ibq/TpWMzDBgemcRc+AUSfIUd7PYiDaGll" +
                        "4xYUg/z6e1th4HJgCiX7q5+DLSh0GJBZppOtVUQrwQKBgQDdGdFQEZeTOPVfs3YP" +
                        "2hZtffswhm6ND4hg2dAHm95KBwXHhnSTKRlsAJYF1QMAHL1i2hUSlgf9gfqeMEn7" +
                        "xgOoqS//EWrZgBVY2zSnVhJP13nrqwWwpyi2QvPhgxFQcZcmyI7xKACJeZ6ZOeVM" +
                        "VuUCwb4++5Q1krikF0SCf5mO4QKBgQDIASfRaxGLrrN9/gjvmPmDAK2yvtiYhghO" +
                        "rJwQ6BQ6sUoxYuiyATwAKNvkVRm1tzYn+No1NJ/GxnGvfuNohFqMZTNJhKWjM6mu" +
                        "6xJ4tLIEjHsmGNONdXR6+pEzbWoyBgPA/MUzzfZwAhxtV0Gi/vE4HmA4t+Cjprvw" +
                        "vQ3pFu0xKwKBgGAOL1lRy0AypqeFF+2bGdNHwDE8thqifOVWu4ISvWf71Q7x4wNr" +
                        "/5dkSckO2dbapYykojMI6z+/kFnZMMspI73KgweVUY5cjumjkiSAyEPXoSg5jKdK" +
                        "d+12+O0oPAVu0/QNfcxXTKRtKfH3rR6VQbkI4tYKBkGgFId1dFYdiWEBAoGAHYnG" +
                        "JrCoa7fGfJIbIb+3AfkErkRvWr9Y1L27YufTrQNoELkp4rg581AVgbhrzqGbVvXy" +
                        "zC7UdhfyzFdyIIqDkP6VP4Nerya/Jb1EAh21uORCf9Lk05yIMm11KmI7b93higwK" +
                        "1+bQQaSeLCvZ3sfGOmKKFXZar/C9CwXw2v7u0vcCgYEA2VyTb0KNuYFSgT6P5ohj" +
                        "ErUH9jqM1tEavuOv5/vexZuY40E9DLxFNMFFx9TcmWjeB2152I9xX0POxTZTn6W2" +
                        "rl1dsl8qAc9dqYZDnH2DXtugJ0Fio6XhVvopu0gIabn0rArCOp8jzrbDkIjCuxw/" +
                        "G85cUxzW8DcJlOIwJ3QlN9w=";

        String content = "YX0000000000" + "1622338938" + "31520202111010000009701";

        // RSA签名
        String sign = sign(content, getPrivateKey(privateKey));

        System.out.println("药联-sign:\n" + sign);

        System.out.println(System.currentTimeMillis());

    }
}