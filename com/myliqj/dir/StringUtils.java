package com.myliqj.dir;

import java.util.Map;
import java.util.Map.Entry;

public class StringUtils {
	
	
	private StringUtils() { /* static methods only - hide constructor */
	}
	
	/**
	 * 判断字符串是否为空,空格不认为空，即空对象与空串才是空
	 * 
	 * @param target
	 * @return boolean
	 */
	public static boolean isEmpty(String target) {
		return (null==target || target.length()==0);
	}
	public static boolean isNotEmpty(String target) {
		return !(null==target || target.length()==0);
	}
	
	/* Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。  
	 * @param src byte[] data  
	 * @return hex string  
	 */     
	public static String bytesToHexString(byte[] src){  
	    StringBuilder stringBuilder = new StringBuilder("");  
	    if (src == null || src.length <= 0) {  
	        return null;  
	    }  
	    for (int i = 0; i < src.length; i++) {  
	        int v = src[i] & 0xFF;  
	        String hv = Integer.toHexString(v);  
	        if (hv.length() < 2) {  
	            stringBuilder.append(0);  
	        }  
	        stringBuilder.append(hv);  
	    }  
	    return stringBuilder.toString();  
	}  
	/** 
	 * Convert hex string to byte[] 
	 * @param hexString the hex string 
	 * @return byte[] 
	 */  
	public static byte[] hexStringToBytes(String hexString) {  
	    if (hexString == null || hexString.equals("")) {  
	        return null;  
	    }  
	    hexString = hexString.toUpperCase();  
	    int length = hexString.length() / 2;  
	    char[] hexChars = hexString.toCharArray();  
	    byte[] d = new byte[length];  
	    for (int i = 0; i < length; i++) {  
	        int pos = i * 2;  
	        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
	    }  
	    return d;  
	}  
	/** 
	 * Convert char to byte 
	 * @param c char 
	 * @return byte 
	 */  
	 private static byte charToByte(char c) {  
	    return (byte) "0123456789ABCDEF".indexOf(c);  
	}
	 
	/**
	 * 字符串的空值处理	 
	 */
	public static String changeNullStringToEmpty(String target) {
		return changeEmptyStringToValue(target, "");
	}
	
	/**
	 * 字符串的空值处理	
	 */
	public static String changeEmptyStringToValue(String target, String replacement) {
		if ((target == null) || ("null".equals(target) || "".equals(target.trim()) )) {
			return replacement;
		} else {
			return target.trim();
		}
	}	
	
	/**
	 * 格式化文本, {} 表示占位符<br>
	 * 例如：format("aaa {} ccc", "bbb")   ---->    aaa bbb ccc
	 * 
	 * @param template 文本模板，被替换的部分用 {} 表示
	 * @param values 参数值
	 * @return 格式化后的文本
	 */
	public static String format(String template, Object... values) {
		if (values==null || template == null || template.isEmpty()) {
			return template;
		}

		final StringBuilder sb = new StringBuilder();
		final int length = template.length();

		int valueIndex = 0;
		char currentChar;
		for (int i = 0; i < length; i++) {
			if (valueIndex >= values.length) {
				sb.append(template.substring(i, length));
				break;
			}

			currentChar = template.charAt(i);
			if (currentChar == '{') {
				final char nextChar = template.charAt(++i);
				if (nextChar == '}') {
					sb.append(values[valueIndex++]);
				} else {
					sb.append('{').append(nextChar);
				}
			} else {
				sb.append(currentChar);
			}

		}

		return sb.toString();
	}
	
	/**
	 * 格式化文本，使用 {varName} 占位<br>
	 * map = {a: "aValue", b: "bValue"}
	 * format("{a} and {b}", map)    ---->    aValue and bValue
	 * 
	 * @param template 文本模板，被替换的部分用 {key} 表示
	 * @param map 参数值对
	 * @return 格式化后的文本
	 */
	public static String format(String template, Map<?, ?> map) {
		if (null == map || map.isEmpty()) {
			return template;
		}

		for (Entry<?, ?> entry : map.entrySet()) {
			template = template.replace("{" + entry.getKey() + "}", entry.getValue().toString());
		}
		return template;
	} 
	 
}
