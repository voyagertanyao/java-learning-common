package com.cmcciot.platform.hoapi.http.nio.filter;

import com.cmcciot.platform.common.utils.KeyUtil;
import com.cmcciot.platform.common.utils.PropertyUtil;
import com.cmcciot.platform.common.utils.StringUtil;
import com.cmcciot.platform.hoapi.auth.util.DigestAuthUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Ip_KeyFilter implements Filter {

    @Override
    public void destroy() {
    }

    /**
     * 验证来源IP是否为指定的IP
     *
     * @param req
     * @param resp
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
                         FilterChain filterChain) throws IOException, ServletException {
        //请求信息
        HttpServletRequest request = (HttpServletRequest) req;
        //获取来源IP
        String sourceIP = getRemoteAddrIp(request);
        //取出配置的IP
        String[] ipArray = PropertyUtil.getValue4Array("http.ip.white");
        List<String> listArray = Arrays.asList(ipArray);
        //判断IP是否是服务管理内网IP
        boolean bIp = listArray.contains(sourceIP);
        if (!bIp) {//未在配置中找到IP白名单
            HttpServletResponse response = (HttpServletResponse) resp;
            response.setContentType("text/html;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter()
                    .write("{\"errorCode\":\"9\",\"description\":\"非法来源地址！\"}");
            return;
        }

        //根据请求，取出header中的msgSeq的值
        String[] headerEntries = DigestAuthUtils.splitIgnoringQuotes(request.getHeader("HOA_auth"),
                ',');
        Map<String, String> headerMap = DigestAuthUtils.splitEachArrayElementAndCreateMap(headerEntries,
                "=",
                "\"");
        //头部序列号
        String key = headerMap.get("key");
        String localKey = KeyUtil.makeMD5(PropertyUtil.getValue("http.service.id")
                + PropertyUtil.getValue("http.service.password"));
        if (!key.equals(localKey)) {
            HttpServletResponse response = (HttpServletResponse) resp;
            response.setContentType("text/html;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter()
                    .write("{\"errorCode\":\"9\",\"description\":\"密匙不正确！\"}");
            return;
        }

        //验证成功，继续执行。
        filterChain.doFilter(request, resp);
        return;
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    /**
     * 获取真实IP地址
     *
     * @param request
     * @return
     */
    public String getRemoteAddrIp(HttpServletRequest request) {
        String ipFromNginx = getHeader(request, "X-Real-IP");
        return StringUtil.isEmpty(ipFromNginx) ? request.getRemoteAddr() : ipFromNginx;
    }

    /**
     * 获取Header信息
     *
     * @param request
     * @param headName
     * @return
     */
    private String getHeader(HttpServletRequest request, String headName) {
        String value = request.getHeader(headName);
        return !StringUtil.isEmpty(value) && !"unknown".equalsIgnoreCase(value) ? value : "";
    }
}
