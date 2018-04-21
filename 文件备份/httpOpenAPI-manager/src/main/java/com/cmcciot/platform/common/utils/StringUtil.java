/*
 * 文 件 名:  StringUtils.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月10日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.common.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author Administrator
 * @version [版本号, 2014年4月10日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class StringUtil {
    /**
     * 判断字符串是否为空串
     * <功能详细描述>
     *
     * @param str
     * @return boolean [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.trim().length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 去掉字符串前后的 引号
     * <功能详细描述>
     *
     * @param str
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String trimQuot(String str) {
        String[] quots = {"\"", "'"};
        if (str == null) {
            return str;
        }
        str = str.trim();
        for (String quot : quots) {
            if (str.startsWith(quot)) {
                str = str.substring(quot.length());
            }
            if (str.endsWith(quot)) {
                str = str.substring(0, str.length() - quot.length());
            }
        }

        return str;
    }

    /**
     * 将流转换为字符串
     * <功能详细描述>
     *
     * @param is
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String convertStreamToString(InputStream is) {
        StringBuilder sb1 = new StringBuilder();
        byte[] bytes = new byte[4096];
        int size = 0;

        try {
            while ((size = is.read(bytes)) > 0) {
                String str = new String(bytes, 0, size, "UTF-8");
                sb1.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb1.toString();
    }

    /**
     * 转换为int类型
     * <功能详细描述>
     *
     * @param valueStr
     * @param defVal
     * @return int [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static int parseInt(String valueStr, int defVal) {
        int value = defVal;
        try {
            value = Integer.parseInt(valueStr);
        } catch (Exception e) {
            value = defVal;
        }
        return value;
    }

    public static void main(String[] args) {
        String s = "\"wxwtest'ww\"";
        System.out.println(trimQuot(s));
    }
}
