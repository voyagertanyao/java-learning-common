/*
 * 文 件 名:  Restful4NioServiceImpl.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年12月11日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.http.nio.service.impl;

import com.cmcciot.common.cache.redis.FailoverJedisPool;
import com.cmcciot.platform.common.constants.RedisKeyPrefixConstant;
import com.cmcciot.platform.common.utils.DateUtil;
import com.cmcciot.platform.common.utils.PropertyUtil;
import com.cmcciot.platform.common.utils.StringUtil;
import com.cmcciot.platform.hoapi.auth.bean.FailLoginInfo;
import com.cmcciot.platform.hoapi.auth.service.AuthUserService;
import com.cmcciot.platform.hoapi.http.nio.bean.LockUserInfo;
import com.cmcciot.platform.hoapi.http.nio.service.Restful4NioService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author Administrator
 * @version [版本号, 2014年12月11日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Component(value = "restful4NioService")
public class Restful4NioServiceImpl implements Restful4NioService {

    @Resource
    private AuthUserService AuthUserService;

    /**
     * 日志记录
     */
    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private FailoverJedisPool jedisPool;

    /**
     * 构建map
     *
     * @param userName
     * @return
     */
    public LockUserInfo buildLockMap(String userName) {
//        FailLoginInfo failLoginInfo = AuthUserServiceImpl.userConHashMap.get(userName);
        FailLoginInfo failLoginInfo = jedisPool.getJedis().getValue(getUserFailLoginKey(userName), FailLoginInfo.class);
        if (null != failLoginInfo) {
            //如果账号已锁定直接返回
            boolean isLockeduser = AuthUserService.isLockedUser(userName);
            if (isLockeduser) {
                LockUserInfo lockuserInfo = new LockUserInfo();
                lockuserInfo.setFistTime(DateUtil.trunsLogTime(failLoginInfo.getFistTime()));
                lockuserInfo.setIsLocked(isLockeduser);
                lockuserInfo.setTimes(failLoginInfo.getTimes() / 2);
                lockuserInfo.setLastTime(failLoginInfo.getLastTime());
                lockuserInfo.setUserName(failLoginInfo.getUserName());
                int lockTime = 0;
                int unlockTime = StringUtil.parseInt(PropertyUtil.getValue("Lock_user_times"), 18000)
                        + failLoginInfo.getLastTime();
                lockTime = (unlockTime - DateUtil.getIntSecondFromDate()) / 60;
                lockTime = lockTime < 1 ? 1 : lockTime;
                lockuserInfo.setLockedTime(String.valueOf(lockTime));
                return lockuserInfo;
            }
        }
        return null;
    }

    private String getUserFailLoginKey(String userName) {
        return RedisKeyPrefixConstant.REDIS_KEY_PREFIX_LOCK_USER + userName;
    }
}
