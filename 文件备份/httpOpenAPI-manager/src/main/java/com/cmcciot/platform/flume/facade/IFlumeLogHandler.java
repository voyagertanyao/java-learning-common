package com.cmcciot.platform.flume.facade;

public interface IFlumeLogHandler {

	/**
	 * flume日志处理逻辑
	 * @param request
	 * @param response
	 */
	void handle(String requestBody);
	
}
