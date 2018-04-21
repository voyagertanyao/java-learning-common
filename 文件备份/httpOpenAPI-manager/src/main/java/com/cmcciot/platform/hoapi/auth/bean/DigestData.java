/*
 * 文 件 名:  DigestData.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月9日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.auth.bean;

import com.cmcciot.platform.common.utils.StringUtil;
import com.cmcciot.platform.hoapi.auth.util.DigestAuthUtils;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Map;

/**
 * 客户端Head鉴权信息
 *
 * @author Administrator
 * @version [版本号, 2014年4月9日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class DigestData implements Serializable {
    private static final long serialVersionUID = -1994769041376162848L;
    private Logger logger = Logger.getLogger(this.getClass());
    //会话标识
    private String opaque;
    //用户名
    private String username;
    //域名
    private String realm;
    //摘要质询参数
    private String nonce;
    //保护质量
    private String qop;
    //uri
    private String uri;
    //对比字符串
    private String response;
    //nonce计数参数
    private String nc;
    //客户端nonce值
    private String cnonce;
    //鉴权内容
    private String section212response;
    //算法
    private String algorithm;

    public DigestData(String header) {
        section212response = header.substring(7);
        String[] headerEntries = DigestAuthUtils.splitIgnoringQuotes(section212response, ',');
        Map<String, String> headerMap = DigestAuthUtils.splitEachArrayElementAndCreateMap(headerEntries, "=", "\"");

        username = headerMap.get("username");
        realm = headerMap.get("realm");
        nonce = headerMap.get("nonce");
        uri = headerMap.get("uri");
        response = headerMap.get("response");
        qop = headerMap.get("qop"); // RFC 2617 extension
        nc = headerMap.get("nc"); // RFC 2617 extension
        cnonce = headerMap.get("cnonce"); // RFC 2617 extension
        algorithm = headerMap.get("algorithm");
        opaque = headerMap.get("opaque");
    }

    /**
     * 判断Digest Auth中必要的参数是否存在
     *
     * @return boolean [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public boolean isUsernameExsit() {
        if (StringUtil.isEmpty(username)) {
            logger.error("username is null");
            return false;
        }

        return true;
    }

    /**
     * 判断存在nonce的请求是否合法
     *
     * @return boolean [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public boolean isRequestFormat() {

        if (StringUtil.isEmpty(username)) {
            logger.error("username is null");
            return false;
        }
        if (StringUtil.isEmpty(opaque)) {
            logger.error("opaque is null");
            return false;
        }
        if (StringUtil.isEmpty(nonce)) {
            logger.error("nonce is null");
            return false;
        }
        if (StringUtil.isEmpty(response)) {
            logger.error("response is null");
            return false;
        }
        if (StringUtil.isEmpty(realm)) {
            logger.error("realm is null");
            return false;
        }
        if (StringUtil.isEmpty(qop)) {
            logger.error("qop is null");
            return false;
        }
        if (StringUtil.isEmpty(nc)) {
            logger.error("nc is null");
            return false;
        }
        if (StringUtil.isEmpty(cnonce)) {
            logger.error("cnonce is null");
            return false;
        }
        if (StringUtil.isEmpty(uri)) {
            logger.error("uri is null");
            return false;
        }
        return true;

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
     * @return 返回 realm
     */
    public String getRealm() {
        return realm;
    }

    /**
     * @param 对realm进行赋值
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * @return 返回 nonce
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * @param 对nonce进行赋值
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * @return 返回 uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param 对uri进行赋值
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return 返回 response
     */
    public String getResponse() {
        return response;
    }

    /**
     * @param 对response进行赋值
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * @return 返回 qop
     */
    public String getQop() {
        return qop;
    }

    /**
     * @param 对qop进行赋值
     */
    public void setQop(String qop) {
        this.qop = qop;
    }

    /**
     * @return 返回 nc
     */
    public String getNc() {
        return nc;
    }

    /**
     * @param 对nc进行赋值
     */
    public void setNc(String nc) {
        this.nc = nc;
    }

    /**
     * @return 返回 cnonce
     */
    public String getCnonce() {
        return cnonce;
    }

    /**
     * @param 对cnonce进行赋值
     */
    public void setCnonce(String cnonce) {
        this.cnonce = cnonce;
    }

    /**
     * @return 返回 section212response
     */
    public String getSection212response() {
        return section212response;
    }

    /**
     * @param 对section212response进行赋值
     */
    public void setSection212response(String section212response) {
        this.section212response = section212response;
    }

    /**
     * @return 返回 algorithm
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * @param 对algorithm进行赋值
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * @return 返回 opaque
     */
    public String getOpaque() {
        return opaque;
    }

    /**
     * @param 对opaque进行赋值
     */
    public void setOpaque(String opaque) {
        this.opaque = opaque;
    }

    /**
     * @return 返回 logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * @param 对logger进行赋值
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }


}
