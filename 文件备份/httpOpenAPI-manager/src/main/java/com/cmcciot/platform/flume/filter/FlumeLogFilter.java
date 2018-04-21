/**
 * 
 */
package com.cmcciot.platform.flume.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.log4j.MDC;

import com.cmcciot.platform.common.utils.StringUtil;
import com.cmcciot.platform.flume.facade.IFlumeLogHandler;
import com.cmcciot.platform.flume.handler.FlumeLogHandlerFactory;
import com.cmcciot.platform.flume.surport.FlumeLogConstant;
import com.cmcciot.platform.flume.thread.FlumeLogTask;
import com.cmcciot.platform.flume.thread.FlumeLogThreadPool;


/**
 * 大数据平台日志收集filter
 * @author tanyao
 *
 */
public class FlumeLogFilter implements Filter {
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// 初始化线程池
		FlumeLogThreadPool.getInstance().init();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletResponse httpResponse = null;
		HttpServletRequest httpRequest = null;
		
		
		StringBuffer buffer1 = new StringBuffer();
		ServletInputStream in1 = request.getInputStream();
		byte[] bt1 = new byte[1024];
        int iRead1;
        while ((iRead1 = in1.read(bt1)) != -1) {
            buffer1.append(new String(bt1, 0, iRead1, "UTF-8"));
        }
        //接口参数
        String requestBody1 = buffer1.toString();
        MDC.put("postStr", requestBody1);
        
        
		chain.doFilter(request, response);
		
		if(response instanceof HttpServletResponse) {
			httpResponse = (HttpServletResponse) response;
		}
		if(request instanceof HttpServletRequest) {
			httpRequest = (HttpServletRequest) request;
		}
		
		// 获取osh会传响应
		String msgType = httpResponse.getHeader("msgType");
		String errorCode = httpResponse.getHeader("errorCode");
		
		// 响应正确
		if(httpResponse.getStatus() == HttpStatus.SC_OK) {
			// 
			if(!StringUtil.isEmpty(msgType) && !StringUtil.isEmpty(errorCode)) {
				// osh服务管理成功处理请求
				if(errorCode.equals("0")) {
					String handlerName = getHandlerName(msgType);
					// 调用相应handler，处理日志
					IFlumeLogHandler handler = FlumeLogHandlerFactory.getFlumeLogHandler(handlerName);
					// 如果请求的msgType需要通过flume同步日志，则解析request报文
					if(handler != null) {
						
						String requestBody = (String) MDC.get("postStr");
						
						FlumeLogThreadPool.getInstance().submitTask(new FlumeLogTask(handler,requestBody));
					}
				}
				
				// 删除添加的response header 信息
				httpResponse.setHeader("msgType", "");
				httpResponse.setHeader("errorCode", "");
			}
		}
	}
	
	/**
	 * handler名称：取msgType中"_"分割的字符串，去掉MSG和REQ，其他中间字符串按顺序驼峰命名；最后加上后缀"Handler"
	 * 例子：msgType = MSG_P_BIND_CAMERA_RSP ==> handler名称 = PBindCameraHandler
	 * @param msgType
	 * @return
	 */
	private String getHandlerName(String msgType) {
		String handlerName = null;
		if(!StringUtil.isEmpty(msgType)) {
			String[] words = msgType.split("_");
			StringBuffer buffer = new StringBuffer();
			for(String word : words) {
				if(word.equalsIgnoreCase("MSG") || word.equalsIgnoreCase("REQ")) {
					continue;
				}
				String temp = word.toLowerCase();
				temp = temp.substring(0, 1).toUpperCase() + temp.substring(1).toLowerCase();
				buffer.append(temp);
			}
			buffer.append(FlumeLogConstant.FLUME_HANDLER_NAME_SUFFIX);
			handlerName = buffer.toString();
			handlerName = handlerName.substring(0, 1).toLowerCase() + handlerName.substring(1);
		}
		return handlerName;
	}

	@Override
	public void destroy() {
				
	}

}
