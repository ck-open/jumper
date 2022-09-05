package com.ck.utils;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * 随机码生成
 * @author cyk
 *
 * @author cyk
 * @since 2020-01-01
 */
public final class RandomUtil {

	/**
	 * 生成UUID
	 * @return
	 */
	public static String createUUID() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        // 去掉“-”
        String temp = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
        return temp;
    }
	
	/**
	 * 将字符串进行MD5加密<br>
	 * 如果slat盐值不为空，则字符串加密后拼接上盐值再次进行加密
	 * @param str  加密的字符串
	 * @param slat 盐值
	 * @return
	 */
	public static String strToMD5(String str,String slat) {
		if(str==null || "".equals(str.trim())) {
			return null;
		}
		StringBuilder result = new StringBuilder();
			try {
				MessageDigest m = MessageDigest.getInstance("MD5");
				m.update(str.getBytes("UTF8"));
				byte s[] = m.digest();

				for (int i = 0; i < s.length; i++) {
					result.append(Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		if(slat==null || "".equals(slat.trim())) {
			return result.toString();
		}else {
			return strToMD5(result.append(slat).toString(), null);
		}
	}
	
	
	/**
	 * 生成随机20位的编号
	 * 
	 * @param prefix 前缀
	 * @param date   日期
	 * @return 编号
	 */
	public static String randomNumber(String prefix, Date date) {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
		
		String serialNumber = prefix + simpleDateFormat.format(date);
		Random random = new Random();
		String numStr = String.valueOf(random.nextInt(99));

		int length = 2 - numStr.length();
		for (int i = 0; i < length; i++) {
			numStr = "0" + numStr;
		}

		return serialNumber + numStr;
	}

}
