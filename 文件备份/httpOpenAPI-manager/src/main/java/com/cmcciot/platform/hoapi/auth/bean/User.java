/*
 * 文 件 名:  User.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月10日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.auth.bean;

import java.io.Serializable;

/**
 * 用户信息
 *
 * @author Administrator
 * @version [版本号, 2014年4月10日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class User implements Serializable {
    private static final long serialVersionUID = -1192740790344038928L;

    private String username;
    private String password;
    private String userid;

    /**
     * 用户状态
     */
    private String userStatus;

    private long lastLoadTime;
    /**
     * <默认构造函数>
     */
    public User() {
        super();
    }

    /**
     * <默认构造函数>
     */
    public User(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, String userid) {
        super();
        this.username = username;
        this.password = password;
        this.userid = userid;
    }

    /**
     * @return 返回 username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param 对username进行赋值
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return 返回 password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param 对password进行赋值
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return 返回 userid
     */
    public String getUserid() {
        return userid;
    }

    /**
     * @param 对userid进行赋值
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

	public long getLastLoadTime()
	{
		return lastLoadTime;
	}

	public void setLastLoadTime(long lastLoadTime)
	{
		this.lastLoadTime = lastLoadTime;
	}
    
}
