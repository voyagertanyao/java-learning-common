/*
 * 文 件 名:  JsonUser.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月14日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.auth.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * 内部的获取用户信息接口的返回结果
 *
 * @author Administrator
 * @version [版本号, 2014年4月14日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonUser implements Serializable {
    private static final long serialVersionUID = -4382980041637832724L;
    //协议版本
    private Integer version;
    //消息类型
    private String msgType;
    //消息序号
    private Integer msgSeq;
    //错误码
    private Integer errorCode;
    //描述
    private String description;
    //用户id
    private String userID;
    //用户密码
    private String password;

    /**
     * 用户状态
     */
    private String userStatus;

    /**
     * @return 返回 version
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * @param version 对version进行赋值
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * @return 返回 msgType
     */
    public String getMsgType() {
        return msgType;
    }

    /**
     * @param msgType 对msgType进行赋值
     */
    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    /**
     * @return 返回 msgSeq
     */
    public Integer getMsgSeq() {
        return msgSeq;
    }

    /**
     * @param msgSeq 对msgSeq进行赋值
     */
    public void setMsgSeq(Integer msgSeq) {
        this.msgSeq = msgSeq;
    }

    /**
     * @return 返回 errorCode
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode 对errorCode进行赋值
     */
    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return 返回 description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description 对description进行赋值
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return 返回 userID
     */
    public String getUserID() {
        return userID;
    }

    /**
     * @param userID 对userID进行赋值
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * @return 返回 password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password 对password进行赋值
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    /**
     * @return
     */
    @Override
    public String toString() {
        return "JsonUser [version=" + version + ", msgType=" + msgType
                + ", msgSeq=" + msgSeq + ", errorcode=" + errorCode
                + ", description=" + description + ", userID=" + userID
                + "userStatus = " + userStatus + "]";
    }
}
