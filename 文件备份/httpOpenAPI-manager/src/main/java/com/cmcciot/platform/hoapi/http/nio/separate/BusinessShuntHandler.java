package com.cmcciot.platform.hoapi.http.nio.separate;

import java.io.Reader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.cmcciot.platform.common.utils.ListTools;
import com.cmcciot.platform.common.utils.StringUtil;
import com.cmcciot.platform.common.utils.XmlTools;
import com.cmcciot.platform.hoapi.http.nio.HttpKeyConstant;
import com.cmcciot.platform.hoapi.http.nio.bean.HandledServiceBean;
import com.cmcciot.platform.hoapi.http.nio.separate.bean.MappingBean;
import com.cmcciot.platform.hoapi.http.nio.separate.bean.PropertyBean;
import com.cmcciot.platform.hoapi.http.nio.separate.bean.TypeMappBean;

/**
 * 业务分流处理类
 * 本类的主要功能是类加载到内存中后便立即解析xml文件中的数据成key-vlue形式保存内存中，通过静态方法getUrlByMsgType(url)获取对应的匹配值
 */
public class BusinessShuntHandler {

	private static Logger logger = Logger.getLogger(BusinessShuntHandler.class);
	/**
	 * 配置文件在web.xml的初始化值
	 */
	public static final String FILE_PATH_XML = "typeMapping";
	//模糊匹配标识
	private static final String BLURRY_MATCH = "blurryMatch";
	//全匹配标识
	private static final String STRICT_MATCH = "strictMatch";
	
	
	//模糊匹配集合
	private static Map<String,String> blurryMap = new HashMap<String,String>();
	
	//保存有序的模糊匹配的key（规则：按字符串的字典顺序倒序排列，这要在映射模糊匹配的map时，匹配的越仔细的就放在前面）
	private static TreeSet<String> keySet = new TreeSet<String>(new Comparator<String>() {
		@Override
		public int compare(String str1, String str2) {
			return str2.compareTo(str1);
		}
		
	});
	
	//全匹配集合
	private static Map<String,String> strictMap = new HashMap<String,String>();
	
	/**
	 * 根据key获取url
	 * @param mesgType
	 * @return
	 */
	public static String getUrlByMsgType(String mesgType){
		String url = strictMap.get(mesgType);
		if(!StringUtil.isEmpty(url)){
			//优先进行完全匹配
			return url;
		}
		
		Iterator<String> it = keySet.iterator();
		while(it.hasNext()){
			String blurryKey = it.next();
			//进行模糊匹配
			if(mesgType.startsWith(blurryKey)){
				url = blurryMap.get(blurryKey);
				break ;
			}
		}
		return url;
	}
	
	/**
	 * 解析xml文件，把解析后的值存入reqMap中
	 * @param in 文件输入流
	 */
	public static void reverseHandler(Reader in)
	{
		Map<String,Map<String, String>> reqMap = new HashMap<String,Map<String, String>>();
    	try {  
    		
    		String stream = XmlTools.readInputStream(in);
            TypeMappBean xmlBean = XmlTools.converyToJavaBean(stream, TypeMappBean.class);
            
            if(xmlBean == null)
            {
            	logger.error("hoa业务分流：xml转实体Mappings节点解析出错");
            	return ;
            }
            
            List<MappingBean> mappingList = xmlBean.getMappingList();
            if(ListTools.isEmpty(mappingList)) 
            {
            	logger.error("hoa业务分流：xml转实体Mapping节点解析出错");
            	return ;
            }
            
            for (MappingBean mappingBean : mappingList) {
        		List<PropertyBean> propertyList = mappingBean.getPropertyList();
        		if(!ListTools.isEmpty(propertyList)){
        			Map<String,String> inMap = new HashMap<String,String>();
        			for (PropertyBean propertyBean : propertyList) {
        				String key = propertyBean.getKey();
        				String value = propertyBean.getValue();
        				String maxtpm = propertyBean.getMaxtpm();
        				
        				//限流接口初始化
        				if(!StringUtil.isEmpty(maxtpm))
        				{
        					HandledServiceBean handledServiceBean = new HandledServiceBean();
            				handledServiceBean.setMsgType(key);
            				handledServiceBean.setCount(new AtomicInteger(0));
            				handledServiceBean.setMaxAcount(Integer.valueOf(maxtpm));
            				HttpKeyConstant.LIMITEFLOWSERVICECOUNT.put(handledServiceBean.getMsgType(), handledServiceBean);
        				}
        				        				
        				if(BLURRY_MATCH.equals(mappingBean.getId())) {
        					//去掉模糊匹配中的末尾的星号
							key = key.replace("*", "");
						}
        				inMap.put(key, value);
					}
        			reqMap.put(mappingBean.getId(), inMap);
        		}
			}
            
            //把解析出的路由分配到相应的集合中去
        	Map<String,String> sMap = reqMap.get(STRICT_MATCH);
        	if(sMap!=null && sMap.size()>0)
        	{
        		strictMap.putAll(sMap);
        	}
        	Map<String,String> bMap = reqMap.get(BLURRY_MATCH);
        	if(bMap!=null && bMap.size()>0)
        	{
        		blurryMap.putAll(bMap);
        		keySet.addAll(blurryMap.keySet());
        	}
        	
        } catch (Exception e) {  
        	logger.error("hoa业务分流：解析typeMapping.xml出错",e);
        } 
    	    	
    	logger.info("hoa业务分流：成功读取配置,全匹配映射==>"+strictMap);
    	logger.info("hoa业务分流：成功读取配置,模糊匹配映射==>"+blurryMap);
	}
	
}
