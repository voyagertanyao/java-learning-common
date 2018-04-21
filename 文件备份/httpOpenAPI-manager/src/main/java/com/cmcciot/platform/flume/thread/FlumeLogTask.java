/**
 * 
 */
package com.cmcciot.platform.flume.thread;

import com.alibaba.fastjson.JSONObject;
import com.cmcciot.platform.flume.facade.IFlumeLogHandler;

/**
 * flume日志处理任务
 * @author tanyao
 *
 */
public class FlumeLogTask implements Runnable{

	private IFlumeLogHandler handler;
	private String requestBody;
	
	@Override
	public void run() {
		handler.handle(requestBody);
	}
	
	public FlumeLogTask(IFlumeLogHandler handler, String requestBody) {
		this.handler = handler;
		this.requestBody = requestBody;
	}

}
