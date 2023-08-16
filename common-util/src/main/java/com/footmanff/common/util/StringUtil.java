package com.footmanff.common.util;

/**
 * @author footmanff on 2021/5/17.
 */
public class StringUtil {

    /**
     * 根据pattern和参数输出组装的字符结果
     *
     * @param pattern 带有占位符的字符串，比如 A:{} B:{}
     * @param param   占位符的值
     * @return 组装以后的结果
     */
    public static String format(String pattern, Object... param) {
        if (pattern == null || pattern.isEmpty()) {
            return "";
        }
        // TODO
//        try {
//            return MessageFormatter.arrayFormat(pattern, param).getMessage();
//        } catch (Throwable e) {
//        }
        return "拼接pattern串失败 pattern: " + pattern;
    }
    
    public static String nullToBlank(String str) {
        if (str == null) {
            return "";
        }
        return str;
    }

    public static String nullToDefault(String str, String def) {
        if (str == null) {
            return def;
        }
        return str;
    }

    /**
     * 字符是否全为数字
     *
     * @param str str
     * @return 空字符或null返回false
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 字符是否为空
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String valueOf(Object obj) {
        if (obj == null) {
            return null;
        }
        return String.valueOf(obj);
    }
    
}
