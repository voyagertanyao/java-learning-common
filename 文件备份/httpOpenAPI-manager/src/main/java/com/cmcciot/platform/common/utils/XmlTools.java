package com.cmcciot.platform.common.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class XmlTools {
    // 日志
    private static Logger logger = Logger.getLogger(XmlTools.class);

    /**
     * doc2String 将xml文档内容转为String
     *
     * @param document
     * @return 字符串
     */
    public static String doc2String(Document document) {
        String str = "";
        try {
            // 使用输出流来进行转化
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // 使用utf-8编码
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("utf-8");
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
            str = out.toString();
        } catch (Exception ex) {
            logger.error(ex);
        }
        return str;
    }

    /**
     * string2Document 将字符串转为Document
     *
     * @param s
     * @return doc
     */
    public static Document string2Document(String s) {
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(s);
        } catch (Exception ex) {
            logger.error(ex);
        }
        return doc;
    }

    /**
     * 将xml字符串转成bean
     * <功能详细描述>
     *
     * @param str [参数说明]
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static <T> T xmlToBean(T bean, String str) throws Exception {
        Serializer serializer = new Persister();
        try {
            serializer.read(bean, str, false);
        } catch (Exception e) {
            throw new Exception("xml转换成bean异常",e);
        }
        return bean;
    }

    
    public static void main(String[] args) throws Exception {
	}
    /**
     * 将bean转成xml字符串
     * <功能详细描述>
     *
     * @param bean
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static <T> String beanToXmlString(T bean) {
        String result = null;
        try {
            //定义序列化对象
            Serializer serializer = new Persister();
            OutputStream out = new ByteArrayOutputStream();
            serializer.write(bean, out);
            result = out.toString().replaceAll("\\n", "").replaceAll("\\s", "");
        } catch (Exception e) {
            logger.error(e);
        }
        return result;
    }
    
    public static <T> String beanToXmlString(T bean, String charset) {
        String result = null;
        try {
            //定义序列化对象
        	Persister serializer = new Persister();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            serializer.write(bean, out,charset);
            result = out.toString(charset);
        } catch (Exception e) {
            logger.error(e);
        }
        return result;
    }


    /**
     * 将xml转换为Map
     *
     * @param xml
     * @return
     * @throws Exception
     */
    public static Map<String, Object> xml2Map(String xml) throws Exception {
        return xmlDoc2Map(DocumentHelper.parseText(xml));
    }

    /**
     * 将xml文件转成Map
     *
     * @param xmlDoc
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Map<String, Object> xmlDoc2Map(Document xmlDoc) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (xmlDoc == null) {
            return map;
        }
        Element root = xmlDoc.getRootElement();
        for (Iterator iterator = root.elementIterator(); iterator.hasNext(); ) {
            Element e = (Element) iterator.next();
            List list = e.elements();
            if (list.size() > 0) {
                map.put(e.getName(), Dom2Map(e, map));
            } else {
                map.put(e.getName(), e.getText());
            }
        }
        return map;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map Dom2Map(Element e, Map map) {
        List list = e.elements();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Element iter = (Element) list.get(i);
                List mapList = new ArrayList();
                if (iter.elements().size() > 0) {
                    Map m = Dom2Map(iter, map);
                    if (map.get(iter.getName()) != null) {
                        Object obj = map.get(iter.getName());
                        if (!obj.getClass().getName().equals("java.util.ArrayList")) {
                            mapList = new ArrayList();
                            mapList.add(obj);
                            mapList.add(m);
                        }
                        if (obj.getClass().getName().equals("java.util.ArrayList")) {
                            mapList = (List) obj;
                            mapList.add(m);
                        }
                        map.put(iter.getName(), mapList);
                    } else {
                        map.putAll(m);
                    }
                } else {
                    if (map.get(iter.getName()) != null) {
                        Object obj = map.get(iter.getName());
                        if (!obj.getClass().getName().equals("java.util.ArrayList")) {
                            mapList = new ArrayList();
                            mapList.add(obj);
                            mapList.add(iter.getText());
                        }
                        if (obj.getClass().getName().equals("java.util.ArrayList")) {
                            mapList = (List) obj;
                            mapList.add(iter.getText());
                        }
                        map.put(iter.getName(), mapList);
                    } else {
                        map.put(iter.getName(), iter.getText());
                    }
                }
            }
        } else {
            map.put(e.getName(), e.getText());
        }
        return map;
    }
    
    
    /** 
     * 把输入流的内容转化成字符串 
     * @param is 
     * @return 
     */  
    public static String readInputStream(Reader in){  
    	try {  
        	BufferedReader bfr = new BufferedReader(in);
        	CharArrayWriter caw = new CharArrayWriter(1024);
        	BufferedWriter bfw = new BufferedWriter(caw);
            String readLine = null;
            while((readLine = bfr.readLine()) != null){  
                bfw.write(readLine);
            }  
            bfr.close();
            bfw.close();
            return caw.toString();  
        } catch (Exception e) {  
            e.printStackTrace();  
            return "获取失败";  
        }  
    }  
    
    /** 
     * xml转换成JavaBean 
     * @param xml 
     * @param c 
     * @return 
     */  
    @SuppressWarnings("unchecked")  
    public static <T> T converyToJavaBean(String xml, Class<T> c) {  
        T t = null;  
        try {  
            JAXBContext context = JAXBContext.newInstance(c);  
            Unmarshaller unmarshaller = context.createUnmarshaller();  
            t = (T) unmarshaller.unmarshal(new StringReader(xml));  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
  
        return t;  
    }  
    
}
