/*
 * 文 件 名:  LockUserInfo.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年12月11日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.http.nio.bean;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author Administrator
 * @version [版本号, 2014年12月11日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class LockUserInfo {
    /**
     * 用户名
     */
    public String userName;

    public int times;

    public String fistTime;

    public int lastTime;

    public boolean isLocked;

    public String lockedTime;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public String getFistTime() {
        return fistTime;
    }

    public void setFistTime(String fistTime) {
        this.fistTime = fistTime;
    }

    public int getLastTime() {
        return lastTime;
    }

    public void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }

    public boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean isLockeduser) {
        this.isLocked = isLockeduser;
    }

    public String getLockedTime() {
        return lockedTime;
    }

    public void setLockedTime(String lockedTime) {
        this.lockedTime = lockedTime;
    }
}
