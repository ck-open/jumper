package com.ck.check_bean;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Random;

/**
 * @author cyk
 * @since 2020-01-01
 */
public final class NumberFormatUtil {

	public final static Integer RATE = 100;

	/**
	 * 格式化数字，保留两位小数
	 * 
	 * @param obj
	 * @return
	 */
	public static String numberFormat(Object obj) {
		DecimalFormat df = new DecimalFormat("######0.0");
		if (obj == null) {
			return df.format(0.0);
		}
		return df.format(obj);
	}

	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static String numberDecimal(Object obj) {
		
		String val = String.valueOf(obj);

		try {
			
			BigDecimal bigDecimal = new BigDecimal(val);
			
			Double absVal = Math.abs(bigDecimal.doubleValue());
			
			if (absVal>=1000) {
				
				val = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
				
			}else if (absVal>=100 && absVal<1000) {
				
				val = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
				
			}else if (absVal>=0 && absVal<100) {
				
				val = bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).toString();
				
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return val;

	}
	
	public static void main(String[] args) {
		
		BigDecimal bigDecimal = new BigDecimal(String.valueOf("0.002541"));
		
		System.out.println(bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).toString());
		
	}

	/**
	 * 保留一位小数
	 * 
	 * @param obj
	 * @return
	 */
	public static Double numberFormatDecimal(Object obj) {
		if (obj == null) {
			return 0.00;
		}
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(1);
		nf.setGroupingUsed(false);
		return Double.parseDouble(nf.format(obj));
	}

	public static Double numberFormat(String str) {
		Double r = 0.0;
		String[] s = str.split("\\.");
		if (s.length > 0 && s[1].length() > 4) {
			str = str.substring(0, str.indexOf(".") + 3);
			r = Double.valueOf(str);
		}
		return r;
	}

	public static Double numberYearEnergyHour(Double total) {
		Double r = 0.0;
		Calendar calendar = Calendar.getInstance();

		int nowHour = calendar.get(Calendar.HOUR_OF_DAY);

		if (nowHour >= 6 && nowHour <= 19) {
			r = (1 - Math.cos((nowHour - 5.0) / (20.0 - 6.0) * Math.PI)) / 2 * total;
			// r = (Math.cos(((nowHour - 1 - 6) / (19 - 6)) * Math.PI) -
			// Math.cos((nowHour - 6) / (19 - 6) * Math.PI)) / 2 * total;
		} else if (nowHour > 19) {
			r = total;
		}
		return r;
	}

	/**
	 * 保留指定位数的小数
	 * 
	 * @param obj
	 * @return
	 */
	public static String numberFormat(Object obj, int length) {
		if (obj == null) {
			String reString = "0.";
			for (int i = 1; i <= length; i++) {
				reString = reString + "0";
			}
			return reString;
		}
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(length);
		nf.setGroupingUsed(false);
		return nf.format(obj);
	}

	public static Double numberEnergyHour(Double total) {
		Double r = 0.0;
		Calendar calendar = Calendar.getInstance();

		int nowHour = calendar.get(Calendar.HOUR_OF_DAY);

		if (nowHour >= 6 && nowHour <= 19) {
			r = (1 - Math.cos((nowHour - 5.0) / (20.0 - 6.0) * Math.PI)) / 2 * total;
			// r = (Math.cos(((nowHour - 1 - 6) / (19 - 6)) * Math.PI) -
			// Math.cos((nowHour - 6) / (19 - 6) * Math.PI)) / 2 * total;
		} else if (nowHour > 19) {
			r = total;
		}

		Random random = new Random();
		Double d = random.nextDouble();
		while (d < 0.8) {
			d = random.nextDouble();
		}
		return r * d;
	}

	public static Double numberEnergyHour(int nowHour, Double total) {
		Double r = 0.0;
		if (nowHour >= 6 && nowHour <= 19) {
			// r = (1 - Math.cos((nowHour - 5.0) / (19.0 - 6.0) * Math.PI)) / 2
			// * total;
			r = (Math.cos(((nowHour - 1 - 5.0) / (20.0 - 6.0)) * Math.PI) - Math.cos((nowHour - 5.0) / (19.0 - 6.0) * Math.PI)) / 2 * total;
		} else if (nowHour > 19) {
			r = total;
		}
		Random random = new Random();
		Double d = random.nextDouble();
		while (d < 0.8) {
			d = random.nextDouble();
		}
		return r * d;
	}

	public static Double numberEnergyMinute(Double total) {
		Double r = 0.0;
		Calendar calendar = Calendar.getInstance();

		double five = 5.0 * 60;
		double six = 6.0 * 60;
		double nineteen = 19.0 * 60;
		// double one = 1.0 * 60;

		int nowHour = calendar.get(Calendar.HOUR_OF_DAY);

		int nowHourMinute = calendar.get(Calendar.MINUTE);

		int minute = nowHour * 60 + nowHourMinute;

		if (nowHour > 6 && nowHour < 19) {
			r = (1 - Math.cos((minute - five) / (nineteen - six) * Math.PI)) / 2 * total;
			// r = (Math.cos(((minute - one - six) / (nineteen - six)) *
			// Math.PI) - Math.cos((nowHour- six) / (nineteen - six) * Math.PI))
			// / 2 * total;
		} else if (nowHour > 19) {
			r = total;
		}
		return r;
	}
}
