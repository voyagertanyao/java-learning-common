package com.cmcciot.platform.common.constants;

/**
 * Redis Key前缀
 * Created by Ginda.Tseng on 2016/2/15.
 */
public class RedisKeyPrefixConstant {

    /* 鉴权信息前缀 */
    public static String REDIS_KEY_PREFIX_CLIENT_NONCE = "hoa.client.nonce.";

    /* 认证失败用户信息前缀 */
    public static String REDIS_KEY_PREFIX_LOCK_USER = "hoa.lockUser.";
}
