package com.wjg.util;

import org.apache.commons.lang3.ObjectUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wjg
 * @date 2018/8/15 16:13
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {
    private static Pattern p = Pattern.compile("\\{(\\d+)\\}");

    /**
     * 格式化字符串
     * @param str   文本串
     * @param args  参数
     * @return      格式化之后的字符串
     */
    public static String format(String str, Object... args) {
        String result = str;
        Matcher m = p.matcher(str);
        while (m.find()) {
            int index = Integer.parseInt(m.group(1));
            if (index < args.length) {
                result = result.replace(m.group(),
                        ObjectUtils.defaultIfNull(args[index], "").toString());
            }
        }
        return result;
    }

}
