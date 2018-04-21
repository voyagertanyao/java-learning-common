/*
 * 文 件 名:  ClientNonce.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月10日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.auth.bean;

import com.cmcciot.platform.hoapi.auth.AuthKeyConstant;

import java.io.Serializable;

/**
 * 待验证的客户端nonce
 *
 * @author Administrator
 * @version [版本号, 2014年4月10日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ClientNonce implements Serializable {
    private static final long serialVersionUID = 7558007762969467324L;

    //客户端会话标识
    private String opaque;
    //为客户端分配的摘要质询参数
    private String nonce;
    //nonce创建的时间戳
    private long timestamp = System.currentTimeMillis();


    public ClientNonce(String opaque, String nonce) {
        super();
        this.opaque = opaque;
        this.nonce = nonce;
    }

    /**
     * @return 返回 opaque
     */
    public String getOpaque() {
        return opaque;
    }

    /**
     * @param opaque 对opaque进行赋值
     */
    public void setOpaque(String opaque) {
        this.opaque = opaque;
    }

    /**
     * @return 返回 nonce
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * @param nonce 对nonce进行赋值
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * @return
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((opaque == null) ? 0 : opaque.hashCode());
        return result;
    }

    /**
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClientNonce other = (ClientNonce) obj;
        if (opaque == null) {
            if (other.opaque != null)
                return false;
        } else if (!opaque.equals(other.opaque))
            return false;
        return true;
    }

    /**
     * @return 返回 timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 检测nonce是否超期
     *
     * @return boolean [true: 已过期，false：未过期]
     */
    public boolean isNonceExpired() {
        long time = System.currentTimeMillis() - this.getTimestamp();
        if (time > AuthKeyConstant.NONCE_EXPIRED) {
            return true;
        }
        return false;
    }
}
