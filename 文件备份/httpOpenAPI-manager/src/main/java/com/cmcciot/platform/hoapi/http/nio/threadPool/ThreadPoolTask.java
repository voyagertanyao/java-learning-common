package com.cmcciot.platform.hoapi.http.nio.threadPool;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cmcciot.platform.common.http.client.HttpUtil;
import com.cmcciot.platform.common.utils.JacksonUtil;
import com.cmcciot.platform.common.utils.PropertyUtil;
import com.cmcciot.platform.common.utils.StringUtil;
import com.cmcciot.platform.hoapi.http.nio.separate.BusinessShuntHandler;

public class ThreadPoolTask implements Runnable, Serializable {

    /**
     * JDK1.5中，每个实现Serializable接口的类都推荐声明这样的一个ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 日志记录
     */
    private Logger logger = Logger.getLogger(this.getClass());

    /**
     * 消息内容
     */
    private String postMsg;
    
    private String msgType;

    /**
     * 头部内容
     */
    private String headerMsg;

    public ThreadPoolTask(String postMsg, String headerMsg,String msgType) {
        this.postMsg = postMsg;
        this.headerMsg = headerMsg;
        this.msgType = msgType;
    }

    // 每个任务的执行过程，现在是什么都没做，除了print和sleep)
    @Override
    public void run() {
    	//要分配到业务机的地址
        String businessUrl = PropertyUtil.getValue("restful.url.auth.nio");
        
    	//获取报文体中的信息
    	try{
    		Map<String, Object> postMap = JacksonUtil.jsonToMap(postMsg);
            String msgType = (String) postMap.get("msgType");
            
            //获取请求路由
            if(!StringUtil.isEmpty(msgType)){
            	String url = BusinessShuntHandler.getUrlByMsgType(msgType);
            	if(!StringUtil.isEmpty(url)){
            		businessUrl = url;
            	}
            }
    	}catch(Exception e){
    		logger.error("hoa业务分流：json字符串转为map不成功，跳转到osh");
    	}
               
        Map<String,String> headerMap = new HashMap<String,String>();
        headerMap.put("HOA_auth", headerMsg);
        String resp = HttpUtil.postHttp(businessUrl, postMsg, headerMap, "UTF-8");
        logger.debug("子线程响应地址：" + businessUrl  + "，子线程响应结果：" + resp);
    }
}
