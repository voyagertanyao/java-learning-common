/*
 * 文 件 名:  QueryLockUserRespons.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年12月11日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.http.nio.bean;

import java.util.List;

/**
 * 搜定账号
 * <功能详细描述>
 *
 * @author xuxiaochuan
 * @version [版本号, 2014年12月11日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class QueryLockUserRespons {
    /**
     * 锁定的用户
     */
    private List<LockUserInfo> lockUsers;

    /**
     * 返回码
     */
    private String erroCode;

    /**
     * 返回描述
     */
    private String description;

    public List<LockUserInfo> getLockUsers() {
        return lockUsers;
    }

    public void setLockUsers(List<LockUserInfo> lockUsers) {
        this.lockUsers = lockUsers;
    }

    public String getErroCode() {
        return erroCode;
    }

    public void setErroCode(String erroCode) {
        this.erroCode = erroCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
