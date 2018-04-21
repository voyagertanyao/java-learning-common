package com.cmcciot.platform.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * json工具类
 *
 * @author
 * @version [版本号, 2014年5月12日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class JacksonUtil {
    /**
     * 往json字串里添加属性
     *
     * @param jsonStr
     * @param params
     * @param values
     * @return [参数说明]
     * @return String [返回类型说明]
     * @exception throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    static Logger logger = Logger.getLogger(JacksonUtil.class);
    private static ObjectMapper objectMapper = null;

    /**
     * json转obj
     * <一句话功能简述>
     * <功能详细描述>
     *
     * @param json
     * @param cls
     * @return Object [返回类型说明]
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException            [参数说明]
     * @throws throws                 [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object jsonToObj(String json, Class cls)
            throws InstantiationException, IllegalAccessException,
            JsonParseException, JsonMappingException, IOException {
        objectMapper = new ObjectMapper();
        Object obj = objectMapper.readValue(json, cls);
        return obj;
    }

    /**
     * obj转json
     * <一句话功能简述>
     * <功能详细描述>
     *
     * @param obj
     * @return String [返回类型说明]
     * @throws IOException [参数说明]
     * @throws throws      [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @SuppressWarnings("deprecation")
    public static String objToJson(Object obj) throws IOException {
        objectMapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gener;
        gener = new JsonFactory().createJsonGenerator(sw);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.writeValue(gener, obj);
        gener.close();
        String json = sw.toString();
        return json;
    }

    /**
     * map转化为json
     * <功能详细描述>
     *
     * @param map
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @SuppressWarnings({"deprecation", "rawtypes"})
    public static String mapToJson(Map map) {
        String json = "{\"errorCode\":1,\"description\":\"服务器内部异常\"}";
        objectMapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gener;
        try {
            gener = new JsonFactory().createJsonGenerator(sw);
            objectMapper.writeValue(gener, map);
            gener.close();
            json = sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * json转化为map
     *
     * @param json
     * @return Map<String,Object> [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @SuppressWarnings({"unchecked"})
    public static Map<String, Object> jsonToMap(String json) {
        Map<String, Object> map = new HashMap<String, Object>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(json, Map.class);

            for (String s : map.keySet()) {
                String value = map.get(s) + "";
                if (!value.equals("null")) {
                    String type = map.get(s).getClass().getSimpleName();
                    //将integer类型的转化为String
                    if (type.equals("Integer")) {
                        map.put(s, ((Integer) map.get(s)).toString());
                    }
                    //将long类型的转化为String
                    if (type.equals("Long")) {
                        map.put(s, ((Long) map.get(s)).toString());
                    }
                }
            }
        } catch (Exception e) {
            map = new HashMap<String, Object>();
            map.put("jsonError", "消息格式错误");
        }
        return map;
    }

    public static String listToJson(List<?> list) {
        objectMapper = new ObjectMapper();
        String backstring = "";
        try {
            backstring = objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return backstring;
    }
}
