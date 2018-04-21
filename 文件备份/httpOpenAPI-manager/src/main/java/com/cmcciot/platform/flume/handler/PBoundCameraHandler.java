/**
 * 
 */
package com.cmcciot.platform.flume.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.cmcciot.platform.flume.facade.IFlumeLogHandler;
import com.cmcciot.platform.flume.util.IPUtils;


/**
 * 第三方绑定摄像机，向flume发送摄像机mac，ip等信息
 * @author tanyao
 *
 */
@Service("pBoundCameraHandler")
public class PBoundCameraHandler implements IFlumeLogHandler {
	
	private final Logger logger = LoggerFactory.getLogger(PBoundCameraHandler.class);

	@Override
	public void handle(String requestBody) {

		// 解析requestBody
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		
		// 获取devID,devIP
		if(requestJson != null) {
			String devId = requestJson.getString("devID");
			String devIp = requestJson.getString("devIP");
			// 解析devIP
			String carrier = IPUtils.evaluate(devIp);
			
			logger.info("{},{},{}", devId , devIp ,carrier);
		}

	}

}
