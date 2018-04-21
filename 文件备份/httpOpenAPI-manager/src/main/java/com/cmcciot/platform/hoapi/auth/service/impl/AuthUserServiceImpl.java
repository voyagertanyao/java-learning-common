/*
 * 文 件 名:  AuthUserServiceImpl.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月10日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.auth.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cmcciot.common.cache.redis.FailoverJedisPool;
import com.cmcciot.platform.common.constants.RedisKeyPrefixConstant;
import com.cmcciot.platform.common.http.client.HttpUtil;
import com.cmcciot.platform.common.utils.DateUtil;
import com.cmcciot.platform.common.utils.PropertyUtil;
import com.cmcciot.platform.common.utils.StringUtil;
import com.cmcciot.platform.hoapi.auth.bean.FailLoginInfo;
import com.cmcciot.platform.hoapi.auth.bean.JsonUser;
import com.cmcciot.platform.hoapi.auth.bean.User;
import com.cmcciot.platform.hoapi.auth.service.AuthUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;
import com.googlecode.ehcache.annotations.Property;

/**
 * 查询用户信息service
 * <p/>
 * 此实现，通过调用接口获取用户信息。并通过ehcache对查询结果进行缓存
 *
 * @author Administrator
 * @version [版本号, 2014年4月10日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Component(value = "authUserService")
public class AuthUserServiceImpl implements AuthUserService {
//    /**
//     * 用于封装用户的登录信息
//     */
//    public static ConcurrentHashMap<String, FailLoginInfo> userConHashMap
//            = new ConcurrentHashMap<String, FailLoginInfo>();
    /**
     * 多次失败校验时间
     */
    public static int failedTimes
            = StringUtil.parseInt(PropertyUtil.getValue("auth_failed_times"), 600);
    /**
     * 失败次数
     */
    public static int failCount = StringUtil.parseInt(PropertyUtil.getValue("auth_failed_count"), 5);
    /**
     * 锁定时间
     */
    public static int lockTimes = StringUtil.parseInt(PropertyUtil.getValue("Lock_user_times"), 18000);
    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private FailoverJedisPool jedisPool;

    public static void main(String[] args) throws Exception {
//		StringBuilder postStr = new StringBuilder();
//        postStr.append("{\"version\":\"0\",\"msgType\":\"MSG_GET_USERINFO_REQ\","
//        		+ " \"msgSeq\":\"0\",\"errorCode\":\"00\",\"userID\":\"10001\"}");
//        ObjectMapper mapper = new ObjectMapper();
//        JsonUser user = (JsonUser)mapper.readValue(postStr.toString(), JsonUser.class);
//        System.out.println(user);

        AuthUserServiceImpl serivce = new AuthUserServiceImpl();
        User u = serivce.queryUser("fengwanfeng");
        System.out.println(u);
    }

    /**
     * 根据用户名查找用户
     * <p/>
     * 对应ehcache中的缓存为 userInfoCache
     *
     * @param username
     * @return User [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @Override
    @Cacheable(cacheName = "userInfoCache", keyGenerator = @KeyGenerator(
            name = "StringCacheKeyGenerator",
            properties = @Property(name = "includeMethod", value = "false")
    ))
    public User findUserByUsername(String username) {
        logger.debug("recache username:" + username);
        User user = this.queryUser(username);
        return user;
    }

    /**
     * 调用接口，获取用户信息
     *
     * @param username
     * @return User [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    private User queryUser(String username) {
        //1. 请求参数字符串
        final StringBuilder postStr = new StringBuilder();
        postStr.append("{\"version\":0,\"msgType\":\"MSG_GET_USERINFOPASS_REQ\","
                + " \"msgSeq\":0,\"userName\":\"" + username + "\",\"clientType\":1}");
        //2.调用服务管理
        String url = PropertyUtil.getValue("restful.url.user");
        String resp = HttpUtil.postHttp(url, postStr.toString(), "UTF-8");
        if(resp==null)
        {
        	logger.error("用户查询错误：根据用户名[" + username + "]查询用户信息错误.查询结果为空");
        	return null;
        }
        try
		{
			ObjectMapper mapper = new ObjectMapper();
			JsonUser ju = (JsonUser) mapper.readValue(resp, JsonUser.class);
			Integer successCode = 0x00;
			if (!successCode.equals(ju.getErrorCode())
			        || StringUtil.isEmpty(ju.getUserID())) {
			    logger.error("用户查询错误：根据用户名[" + username + "]查询用户信息错误.查询结果为：" + resp);
			    return null;
			}
			if (StringUtil.isEmpty(ju.getPassword())) {
			    logger.warn("调用获取用户信息接口，密码为空:" + ju);
			}
			User user = new User();
			user.setUsername(username);
			user.setPassword(ju.getPassword());
			user.setUserid(ju.getUserID());
			user.setUserStatus(ju.getUserStatus());
			return user;
		} 
        catch (Exception e)
		{
			logger.error("用户查询错误：根据用户名[" + username + "]查询用户信息错误.查询结果为：" + resp);
		}        
        return null;
    }

    /**
     * 判断是否为锁定用户
     *
     * @param userName 用户名
     * @return Boolean 是否为锁定用户
     */
    public boolean isLockedUser(String userName) {
        //获取登录失败的信息
        FailLoginInfo failLoginInfo = getLockUser(userName);

        //获取当前时间
        int newTime = DateUtil.getIntSecondFromDate();

        if (null == failLoginInfo) {
            return false;
        }

        //特殊处理1：如果配置成0 则不需要锁定
        if (failCount == 0) {
            return false;
        }

        //获取第一次请求中的
        int fistTime = failLoginInfo.getFistTime();

        //获取系统中失败次数
        int counts = failLoginInfo.getTimes();

        //失败次数达到配置
        if (counts >= failCount) {
            if (0 == failedTimes) {
                if (0 == lockTimes) {
                    return true;
                } else {
                    if (failLoginInfo.getLastTime() + lockTimes >= newTime) {
                        return true;
                    } else {
                        removeLockUser(userName);
                        return false;
                    }
                }
            } else {
                if (fistTime + failedTimes > newTime) {
                    logger.warn("由于多次请求失败，用户已被锁定，user = " + userName);
                    return true;
                } else {
                    if (0 == lockTimes) {
                        return true;
                    } else if (lockTimes + failLoginInfo.getLastTime() >= newTime) {
                        return true;
                    } else {
                        removeLockUser(userName);
                        return false;
                    }
                }
            }
        } else {
            if (0 == failedTimes) {
                return false;
            } else {
                if (fistTime + failedTimes < newTime) {
                    removeLockUser(userName);
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 锁定账号
     * <功能详细描述>
     *
     * @param userName [参数说明]
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public void lockUser(String userName) {
        //获取登录失败的信息
        FailLoginInfo failLoginInfo = getLockUser(userName);

        //获取当前时间
        int newTime = DateUtil.getIntSecondFromDate();

        if (failCount == 0) {
            return;
        }

        //如果未空则创建
        if (null == failLoginInfo) {
            FailLoginInfo newFailLoginInfo = new FailLoginInfo();
            newFailLoginInfo.setFistTime(DateUtil.getIntSecondFromDate());
            newFailLoginInfo.setTimes(1);
            newFailLoginInfo.setUserName(userName);
            newFailLoginInfo.setLastTime(DateUtil.getIntSecondFromDate());
            cacheLockUser(userName, newFailLoginInfo);
        } else {
            failLoginInfo.setTimes(failLoginInfo.getTimes() + 1);
            failLoginInfo.setLastTime(newTime);
            cacheLockUser(userName, failLoginInfo);
        }
    }

    /**
     * 获取缓存key
     *
     * @param userName 用户名
     * @return 缓存key
     */
    private String getLockUserKey(String userName) {
        return RedisKeyPrefixConstant.REDIS_KEY_PREFIX_LOCK_USER + userName;
    }

    /**
     * 缓存锁定用户
     *
     * @param userName
     * @param newFailLoginInfo
     */
    private void cacheLockUser(String userName, FailLoginInfo newFailLoginInfo) {
        String expiredSeconds = PropertyUtil.getValue("redis.lockUser.expiredSeconds");
        if (StringUtil.isEmpty(expiredSeconds)) {
            jedisPool.getJedis().saveOrUpdateEx(getLockUserKey(userName), newFailLoginInfo);
        } else {
            jedisPool.getJedis().saveOrUpdateEx(getLockUserKey(userName), newFailLoginInfo, Integer.valueOf(expiredSeconds));
        }
    }

    @Override
    public void removeLockUser(String userName) {
        jedisPool.getJedis().del(getLockUserKey(userName));
    }

    /**
     * 获取锁定用户信息
     *
     * @param userName 用户名
     * @return 锁定用户
     */
    private FailLoginInfo getLockUser(String userName) {
        return jedisPool.getJedis().getValue(getLockUserKey(userName), FailLoginInfo.class);
    }
}
