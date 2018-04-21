package com.cmcciot.platform.hoapi.auth.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.cmcciot.platform.common.http.client.HttpUtil;
import com.cmcciot.platform.common.utils.PropertyUtil;
import com.cmcciot.platform.common.utils.StringUtil;
import com.cmcciot.platform.hoapi.auth.bean.JsonUser;
import com.cmcciot.platform.hoapi.auth.bean.User;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 用户信息缓存
 * @author wangxiaowei
 *
 */
public class UserInfoCache
{
	//两次加载用户时间间隔不得少于3s
	public static final long RELOAD_TIMEOUT = 3*1000;
	
	private static Logger logger = Logger.getLogger(UserInfoCache.class);
	//用户名-用户，缓存
	private static ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap<String,User>();
		
	/**
	 * 获取用户信息
	 * @param username
	 * @return
	 */
	public static User getUserByUsername(String username)	
	{
		User result =  userMap.get(username);
		if(result==null)
		{
			result = loadUser(username);
		}
		return result;
	}
	
	/**
	 * 重新加载并获取用户信息
	 * 当发现返回的用户信息有错误时，可调用此方法
	 * @param username
	 * @return
	 */
	public static User getAndReladUser(String username)
	{
		User result =  userMap.get(username);
		if(result==null)
		{
			result = loadUser(username);
		}
		else
		{
			long now = System.currentTimeMillis();
			if(now - result.getLastLoadTime() > RELOAD_TIMEOUT)
			{
				result = loadUser(username);
			}
		}
		return result;
	}

	/**
	 * 加载用户信息
	 * @param username
	 */
	private static User loadUser(String username)
	{
		User u = queryUser(username);
		if(u==null)
		{
			logger.error("加载用户信息失败，返回为空");
			return null;
		}
		String userId = u.getUserid();
		if(StringUtil.isEmpty(userId))
		{
			logger.error("加载用户信息失败，找不到用户名为["+username+"]的用户");
			return null;
		}
		u.setLastLoadTime(System.currentTimeMillis());
		userMap.put(username, u);
		return u;
	}
	
	/**
	 * 调用osh查询用户信息
	 * @param username
	 * @return
	 */
	private static User queryUser(String username) 
	{
		// 1. 请求参数字符串
		final StringBuilder postStr = new StringBuilder();
		postStr.append("{\"version\":0,\"msgType\":\"MSG_GET_USERINFOPASS_REQ\"," + " \"msgSeq\":0,\"userName\":\""
				+ username + "\",\"clientType\":1}");
		// 2.调用服务管理
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
			logger.info("获取用户信息接口：成功查询用户==>" + ju);
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
	
}
