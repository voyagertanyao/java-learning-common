/*
 * 文 件 名:  AuthKeyConstant.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月9日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.auth;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author Administrator
 * @version [版本号, 2014年4月9日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public abstract class AuthKeyConstant {

    //nonce超期时间
    public static long NONCE_EXPIRED = 20 * 60 * 1000;

    //默认的域名
    public static String DEFAULT_REALM = "cmcc.cn";

    /**
     * 用户被冻结
     */
    public static String USER_FROZEN = "1";
}
