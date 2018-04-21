/*
 * 文 件 名:  NonceUtil.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月9日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.common.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author Administrator
 * @version [版本号, 2014年4月9日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class KeyUtil {
    public static void main(String[] args) {
        System.out.println(generateOpaque("1A740E80025F8991936340E5E2A62BB5"));
        System.out.println(generateNonce());
    }

    public static String generateNonce() {
        Random rand = new Random();
        String time = System.currentTimeMillis() + "";
        String source = time + "-" + rand.nextInt(10000);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }

        return new String(Hex.encode(digest.digest(source.getBytes())));
    }

    /**
     * 生成会话标识
     *
     * @param str
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String generateOpaque(String str) {
        if (StringUtil.isEmpty(str)) {
            str = System.currentTimeMillis() + ";"
                    + new Random().nextInt(20040410);
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }
        return new String(Hex.encode(digest.digest(str.getBytes())));
    }

    /**
     * MD5加密
     * <功能详细描述>
     *
     * @param str
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String makeMD5(String str) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            String returnStr = new BigInteger(1, md.digest()).toString(16);
            return returnStr;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return str;
    }

}
