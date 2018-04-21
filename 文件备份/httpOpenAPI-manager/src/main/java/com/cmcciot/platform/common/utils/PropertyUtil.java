/*
 * 文 件 名:  PropertyUtil.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月11日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 读取配置文件/config/config.properties的工具类
 * <p/>
 * 此工具类会动态读取配置文件，配置文件修改后会重新加载配置项。
 *
 * @author Administrator
 * @version [版本号, 2014年4月11日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class PropertyUtil {
    private static Properties props = new Properties();

    //配置文件
    private static File configFile;

    //配置文件的最后修改时间
    private static long fileLastModify = 0L;

    static {
        try {
            configFile = new File(
                    PropertyUtil.class.getResource("/config/config.properties")
                            .getPath());
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据配置文件中的key,查找值
     *
     * @param name
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String getValue(String name) {
        long lm = configFile.lastModified();
        if (lm != fileLastModify) {
            load();
        }
        return props.getProperty(name);
    }

    /**
     * 根据,返回多组数据
     * <功能详细描述>
     *
     * @param name
     * @return String[] [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String[] getValue4Array(String name) {
        long lm = configFile.lastModified();
        if (lm != fileLastModify) {
            load();
        }
        String perperties = props.getProperty(name);
        String[] valueArray = null;
        if (!StringUtil.isEmpty(perperties)) {
            valueArray = perperties.split(",");
        }
        return valueArray;
    }

    /**
     * 重新加载配置文件配置项
     *
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    private static void load() {
        try {
            InputStream is = new FileInputStream(configFile);
            props.load(is);
            fileLastModify = configFile.lastModified();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(getValue4Array("http.ip.white").length);

        String[] ipArray = PropertyUtil.getValue4Array("http.ip.white");
        List<String> listArray = Arrays.asList(ipArray);
        System.out.println(listArray.contains("10.189.24.155"));
    }

}
