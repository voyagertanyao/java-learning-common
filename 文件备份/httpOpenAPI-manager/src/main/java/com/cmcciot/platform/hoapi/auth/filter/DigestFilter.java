/*
 * 文 件 名:  DigestFilter.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月9日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.auth.filter;

import com.cmcciot.common.cache.redis.FailoverJedisPool;
import com.cmcciot.platform.common.constants.RedisKeyPrefixConstant;
import com.cmcciot.platform.common.utils.KeyUtil;
import com.cmcciot.platform.common.utils.PropertyUtil;
import com.cmcciot.platform.common.utils.StringUtil;
import com.cmcciot.platform.hoapi.auth.AuthKeyConstant;
import com.cmcciot.platform.hoapi.auth.bean.ClientNonce;
import com.cmcciot.platform.hoapi.auth.bean.DigestData;
import com.cmcciot.platform.hoapi.auth.bean.User;
import com.cmcciot.platform.hoapi.auth.cache.UserInfoCache;
import com.cmcciot.platform.hoapi.auth.service.AuthUserService;
import com.cmcciot.platform.hoapi.auth.util.DigestAuthUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 权限验证filter
 *
 * @author Administrator
 * @version [版本号, 2014年4月9日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class DigestFilter implements Filter {
    private Logger logger = Logger.getLogger(this.getClass());

    private AuthUserService authUserService;

    private FailoverJedisPool jedisPool;

    @Override
    public void destroy() {
    }

    /**
     * @param req
     * @param resp
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String header = request.getHeader("Authorization");
        HttpSession session = request.getSession(true);
        if (header == null) {
            // 首次挑战，生成nonce返回
            logger.debug("首次挑战，没有Authorization头。");
            String opaque = KeyUtil.generateOpaque(session.getId());
            String nonce = KeyUtil.generateNonce();
            ClientNonce cn = new ClientNonce(opaque, nonce);

            cacheNonce(opaque, cn);
            this.sendUnAuthResponse(response, cn, null);
            return;
        }
        if (!header.startsWith("Digest ")) {
            // 不正确的鉴权头，重新生成nonce
            logger.error("Authorization头不是以Digest 开始，客户端header为[" + header + "]。");
            String opaque = KeyUtil.generateOpaque(session.getId());
            String nonce = KeyUtil.generateNonce();
            ClientNonce cn = new ClientNonce(opaque, nonce);

            cacheNonce(opaque, cn);
            this.sendUnAuthResponse(response, cn, "Incorrect Authorization");
            return;
        }

        //解析鉴权头
        DigestData data = new DigestData(header);
        if (data == null || !data.isRequestFormat()) {
            // 不正确的鉴权头
            logger.error("解析Authorization头错误，客户端header为[" + header + "]。");
            String opaque = KeyUtil.generateOpaque(session.getId());
            String nonce = KeyUtil.generateNonce();
            ClientNonce cn = new ClientNonce(opaque, nonce);

            cacheNonce(opaque, cn);
            this.sendUnAuthResponse(response, cn, "Incorrect Authorization");
            return;
        }

        //验证会话是否存在（一般为nonce超期后被清除的情况下发生）
        ClientNonce find = getNonce(data.getOpaque());
        if (find == null) {
            logger.debug("服务器端没有存储会话id为[" + data.getOpaque() + "]的nonce信息。");
            String opaque = KeyUtil.generateOpaque(session.getId());
            String nonce = KeyUtil.generateNonce();
            ClientNonce cn = new ClientNonce(opaque, nonce);

            cacheNonce(opaque, cn);
            this.sendUnAuthResponse(response, cn, "Incorrect Authorization");
            return;
        }

        //验证nonce是否超期
        if (find.isNonceExpired()) {
            logger.debug("会话id为[" + data.getOpaque() + "]的客户端nonce超时。");
            String opaque = data.getOpaque();
            String nonce = KeyUtil.generateNonce();
            ClientNonce cn = new ClientNonce(opaque, nonce);

            cacheNonce(opaque, cn);
            this.sendUnAuthResponse(response, cn, "Incorrect Authorization, nonce expired");
            return;
        } else if (!find.getNonce().equals(data.getNonce())) {
            logger.debug("会话id为[" + data.getOpaque() + "]的客户端nonce非法。");
            this.sendUnAuthResponse(response, find, "Incorrect Authorization, nonce invalid");
            return;
        }

        //如果账号已锁定直接返回
        boolean isLockeduser = authUserService.isLockedUser(data.getUsername());
        if (isLockeduser) {
            logger.error("会话id为[" + data.getOpaque() + "]用户名为[" + data.getUsername() + "]的用户已被锁定。");
            this.sendBackLockedUser(response);
            return;
        }

        //验证response是否正确
        User user = UserInfoCache.getUserByUsername(data.getUsername());
        if (user == null) {
            logger.error("会话id为[" + data.getOpaque() + "]找不到用户名为[" + data.getUsername() + "]的用户。");
            String opaque = data.getOpaque();
            String nonce = KeyUtil.generateNonce();
            ClientNonce cn = new ClientNonce(opaque, nonce);

            cacheNonce(opaque, cn);
            this.sendUnAuthResponse(response, cn, "Incorrect Response");
            return;
        }

        //如果账号被属于被冻结直接返回 0:正常 1：冻结
//        if (AuthKeyConstant.USER_FROZEN.equals(user.getUserStatus())) {
//            logger.error("会话id为[" + data.getOpaque() + "]用户名为[" + data.getUsername() + "]的用户账号被冻结。");
//            this.sendFreezeMsg(response);
//            return;
//        }

        //生成服务端比较Str
        String servResponse = DigestAuthUtils.generateResponse(data, request.getMethod(), user, find);
        if (data.getResponse().equals(servResponse)) {
            authUserService.removeLockUser(data.getUsername());
            //正确时继续往下
            this.setCorrectHeader(response, find);
            chain.doFilter(request, response);
            return;
        }
        else
        {
        	//鉴权不通过时，重新加载一次用户信息后再试
            user = UserInfoCache.getAndReladUser(data.getUsername());
            servResponse = DigestAuthUtils.generateResponse(data, request.getMethod(), user, find);
            if(data.getResponse().equals(servResponse))
            {
            	authUserService.removeLockUser(data.getUsername());
                //正确时继续往下
                this.setCorrectHeader(response, find);
                chain.doFilter(request, response);
                return;
            }
        }
        
        

        //鉴权不通过app需要查看报文。
        String postStr = null;
        try {
            //1.获取参数值
            StringBuffer buffer = new StringBuffer();
            InputStream in;
            in = request.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(in);
            byte[] bt = new byte[1024];
            int iRead;
            while ((iRead = bis.read(bt)) != -1) {
                buffer.append(new String(bt, 0, iRead, "UTF-8"));
            }
            //接口参数
            postStr = buffer.toString();
        } catch (Exception e) {
            logger.error("测试使用:解析业务请求报文错误：" + e);
        }

        //response不正确，重新生成nonce返回错误
        logger.error("会话id为[" + data.getOpaque() + "]验证错误,期望得到response[" + servResponse + "]，但收到response["
                + data.getResponse() + "]。" + "请求报文：" + postStr);
        logger.error("客户端header为[" + header + "]");
        logger.error("服务器端的参数字符串为" + DigestAuthUtils.getDigestParams(data, request.getMethod(), user, find));
        String opaque = data.getOpaque();
        String nonce = KeyUtil.generateNonce();
        ClientNonce cn = new ClientNonce(opaque, nonce);

        cacheNonce(opaque, cn);

        //添加锁定账号信息
        authUserService.lockUser(data.getUsername());
        this.sendUnAuthResponse(response, cn, "Incorrect Response");
        return;

    }

    /**
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        WebApplicationContext wac = WebApplicationContextUtils
                .getWebApplicationContext(config.getServletContext());
        try {
            authUserService = (AuthUserService) wac.getBean("authUserService");
            if (authUserService == null) {
                logger.error("初始化加载authUserService出错。。。");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        // 初始化加载FailoverJedisPool
        try {
            jedisPool = (FailoverJedisPool) wac.getBean("jedisPool");
            if (jedisPool == null) {
                logger.error("初始化加载FailoverJedisPool出错。。。");
            }
        } catch (Exception e1) {
            logger.error(e1);
        }

        //加载配置项
        String miniute = PropertyUtil.getValue("auth.nonce.expired.miniutes");
        if (StringUtil.isEmpty(miniute)) {
            miniute = "10";
        }
        try {
            AuthKeyConstant.NONCE_EXPIRED = Integer.parseInt(miniute) * 60 * 1000;
        } catch (Exception e) {
            AuthKeyConstant.NONCE_EXPIRED = 10 * 60 * 1000;
        }
        String realm = PropertyUtil.getValue("auth.reaml");
        if (StringUtil.isEmpty(realm)) {
            realm = "cmcc.cn";
        }
        AuthKeyConstant.DEFAULT_REALM = realm;
    }

    /**
     * 发送未鉴权或鉴权失败的响应
     *
     * @param resp
     * @param cn
     * @param errMsg
     * @return void [返回类型说明]
     * @throws IOException [参数说明]
     * @throws throws      [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public void sendUnAuthResponse(HttpServletResponse resp, ClientNonce cn,
                                   String errMsg) throws IOException {
        if (StringUtil.isEmpty(errMsg)) {
            errMsg = "Full authentication is required to access this resource";
        }
        //		StringBuilder body = new StringBuilder();
        //		body.append("{\"version\":0, \"msgType\":\"MSG_GET_USERINFO_RESP\", \"msgSeq\":1,\"errorCode\":\"0x1004\",\"description\":\""+errMsg+"\"}");
        StringBuilder authMsg = new StringBuilder("Digest ");
        authMsg.append("realm=\"" + AuthKeyConstant.DEFAULT_REALM + "\"");
        authMsg.append(", algorithm=\"MD5\"");
        authMsg.append(", qop=\"auth\"");
        authMsg.append(", nonce=\"" + cn.getNonce() + "\"");
        authMsg.append(", opaque=\"" + cn.getOpaque() + "\"");
        resp.setHeader("WWW-Authenticate", authMsg.toString());
        resp.setContentType("application/json");
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, errMsg);
        //		resp.getWriter().print(body.toString());
        resp.getWriter().flush();
    }

    public void sendUnAuthResponseWithBody(HttpServletResponse resp, ClientNonce cn,
                                           String body) throws IOException {
        if (StringUtil.isEmpty(body)) {
            body = "{}";
        }

        StringBuilder authMsg = new StringBuilder("Digest ");
        authMsg.append("realm=\"" + AuthKeyConstant.DEFAULT_REALM + "\"");
        authMsg.append(", algorithm=\"MD5\"");
        authMsg.append(", qop=\"auth\"");
        authMsg.append(", nonce=\"" + cn.getNonce() + "\"");
        authMsg.append(", opaque=\"" + cn.getOpaque() + "\"");
        resp.setHeader("WWW-Authenticate", authMsg.toString());
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().print(body);
        resp.getWriter().flush();
    }

    /**
     * 设置正确的Header
     *
     * @param resp
     * @param cn   [参数说明]
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public void setCorrectHeader(HttpServletResponse resp, ClientNonce cn) {
        StringBuilder authMsg = new StringBuilder();
        authMsg.append("qop=\"auth\"");
        authMsg.append(", nextnonce=\"" + cn.getNonce() + "\"");
        resp.setHeader("Authorization-Info", authMsg.toString());
    }

    /**
     * @return 返回 authUserService
     */
    public AuthUserService getAuthUserService() {
        return authUserService;
    }

    /**
     * @param authUserService
     */
    public void setAuthUserService(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    /**
     * 返回用户被锁定消息
     * <功能详细描述>
     *
     * @param response
     * @return void [返回类型说明]
     * @throws IOException
     * @throws throws      [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public void sendBackLockedUser(HttpServletResponse response) throws IOException {
        //6.2.向app返回值
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        //超时返回报文
        String timeoutResponse = "{\"description\":\"账号已被锁定，请稍后尝试\",\"msgSeq\":\"\",\"errorCode\""
                + ":\"4103\",\"msgType\":\" MSG_FROZEN_USER_RSP\",\"version\":\"16\"}";
        response.getWriter().write(timeoutResponse);
    }

    /**
     * 第三方用户账号被冻结
     * <功能详细描述>
     *
     * @param response 返回对象
     * @return void [返回类型说明]
     * @throws IOException [参数说明]
     * @throws throws      [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public void sendFreezeMsg(HttpServletResponse response) throws IOException {
        //6.2.向app返回值
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        //超时返回报文
        String responseStr = "{\"description\":\"账号已被冻结，请联系客服\",\"msgSeq\":\"\",\"errorCode\""
                + ":\"4115\",\"msgType\":\" MSG_FROZEN_USER_RSP\",\"version\":\"16\"}";
        response.getWriter().write(responseStr);
    }

    /**
     * 缓存鉴权信息
     *
     * @param opaque key
     * @param cn     value
     */
    private void cacheNonce(String opaque, ClientNonce cn) {
        String expiredSeconds = PropertyUtil.getValue("redis.nonce.expiredSeconds");
        if (StringUtil.isEmpty(expiredSeconds)) {
            jedisPool.getJedis().saveOrUpdateEx(RedisKeyPrefixConstant.REDIS_KEY_PREFIX_CLIENT_NONCE + opaque, cn);
        } else {
            jedisPool.getJedis().saveOrUpdateEx(RedisKeyPrefixConstant.REDIS_KEY_PREFIX_CLIENT_NONCE + opaque, cn, Integer.valueOf(expiredSeconds));
        }
    }

    /**
     * 获取鉴权信息
     *
     * @param opaque key
     * @return 鉴权信息
     */
    private ClientNonce getNonce(String opaque) {
        return jedisPool.getJedis().getValue(RedisKeyPrefixConstant.REDIS_KEY_PREFIX_CLIENT_NONCE + opaque, ClientNonce.class);
    }
}
