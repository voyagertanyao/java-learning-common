/**
 * 
 */
package com.cmcciot.platform.flume.handler;

import org.springframework.beans.BeansException;

import com.cmcciot.common.cache.redis.util.SpringContextutil;
import com.cmcciot.platform.common.utils.StringUtil;
import com.cmcciot.platform.flume.facade.IFlumeLogHandler;

/**
 * 获取flume日志处理类
 * @author tanyao
 *
 */
public class FlumeLogHandlerFactory {

	public static IFlumeLogHandler getFlumeLogHandler(String name) {
		IFlumeLogHandler handler = null;
		if(!StringUtil.isEmpty(name)) {
			try {
				handler = (IFlumeLogHandler) SpringContextutil.getBean(name);
			} catch(BeansException e) {
				return null;
			}
		}
		return handler;
	}
	
}
