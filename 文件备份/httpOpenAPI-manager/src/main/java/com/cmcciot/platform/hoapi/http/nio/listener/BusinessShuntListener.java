package com.cmcciot.platform.hoapi.http.nio.listener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.cmcciot.platform.common.http.client.HttpUtil;
import com.cmcciot.platform.hoapi.http.nio.separate.BusinessShuntHandler;

/**
 * 业务分流监听器
 * 
 * @author hujinghua 2017-4-25
 */
public final class BusinessShuntListener implements ServletContextListener
{

	/**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(BusinessShuntListener.class);

	public void contextInitialized(ServletContextEvent sce)
	{
		logger.info("===============hoa业务分流：业务分流监听器启动============");
		// 获取业务分流配置文件路径（如：/config/typeMapping.xml）
		String filePath = sce.getServletContext().getInitParameter(BusinessShuntHandler.FILE_PATH_XML);

		InputStream in = getClass().getResourceAsStream(filePath);
		if (in == null)
		{
			logger.error("hoa业务分流：加载业务分流配置文件异常！");
		} else
		{
			// 解析业务分流xml
			InputStreamReader isr = null;
			try
			{
				isr = new InputStreamReader(in, "UTF-8");
			} catch (UnsupportedEncodingException e)
			{
				logger.error("hoa业务分流：字节流转换成字符流失败", e);
			}
			BusinessShuntHandler.reverseHandler(isr);
		}
		HttpUtil.init();
	}

	public void contextDestroyed(ServletContextEvent sce)
	{
		HttpUtil.shutdown();
	}

}
