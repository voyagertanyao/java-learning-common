/**
 * 
 */
package com.cmcciot.platform.flume.util;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


import com.cmcciot.platform.common.utils.PropertyUtil;
import com.csvreader.CsvReader;

/**
 * Describe: 获取IP具体信息工具
 *
 * @author : ChenQuangu
 * @mail : chenquangu@cmiot.chinamobile.com
 * @date : 11:48 2018/4/9
 */
public class IPUtils {
	private static String ipInfoFilePath;
	
	private final static String IP_EVALUATE_ERROR_RESULT = ",,,,,,,,,,,,";
	
    private static byte[] data;

    private static long indexSize;

    private static final Pattern IPV4_PATTERN = Pattern.compile("^(([1-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){1}(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){2}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
    
    // 读取ipInfos.datx文件目录
    static {
    	String ipInfoProperty = PropertyUtil.getValue("ipInfo.filePath");
    	try {
        	ipInfoFilePath = new File(IPUtils.class.getResource(ipInfoProperty).getPath()).getPath();
        	loadResource(ipInfoFilePath);
    	}catch(Exception e) {
    		loadResource("");
    	}
    }
    
    // 加载资源文件
    private static void loadResource(String name) {
        try {
            Path path = Paths.get(name);
			data = Files.readAllBytes(path);
			indexSize = bytesToLong(data[0], data[1], data[2], data[3]);
		} catch (Exception e) {
			data = null;
			indexSize = 0L;
		}
        
    }
    
    // 释放资源
    private static void releaseResource() {
    	if(data != null) {
    		data = null;
    	}
    }
    
    // 检查是否加载资源文件
    private static void reloadResource() {
    	if(data == null) {
    		loadResource(ipInfoFilePath);
    	}
    }
    
    // 校验ip格式
    private static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }
    
    private static long int2long(int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }

    private static long bytesToLong(byte a, byte b, byte c, byte d) {
        return int2long((((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff)));
    }

    private static long ip2long(String ip) {
        String[] ss = ip.split("\\.");
        int a, b, c, d;
        a = Integer.parseInt(ss[0]);
        b = Integer.parseInt(ss[1]);
        c = Integer.parseInt(ss[2]);
        d = Integer.parseInt(ss[3]);
        int i = (a << 24) | (b << 16) | (c << 8) | d;
        return int2long(i);
    }

    private static String[] find(String ips) throws Exception {
        if (!isIPv4Address(ips)) {
            throw new Exception();
        }
        long val = ip2long(ips);
        int start = 262148;
        int low = 0;
        int mid = 0;
        int high = new Long((indexSize - 262144 - 262148) / 9).intValue() - 1;
        int pos = 0;
        while (low <= high) {
            mid = new Double((low + high) / 2).intValue();
            pos = mid * 9;
            long s = 0;
            if (mid > 0) {
                int pos1 = (mid - 1) * 9;
                s = bytesToLong(data[start + pos1], data[start + pos1 + 1], data[start + pos1 + 2], data[start + pos1 + 3]);
            }
            long end = bytesToLong(data[start + pos], data[start + pos + 1], data[start + pos + 2], data[start + pos + 3]);
            if (val > end) {
                low = mid + 1;
            } else if (val < s) {
                high = mid - 1;
            } else {
                byte b = 0;
                long off = bytesToLong(b, data[start + pos + 6], data[start + pos + 5], data[start + pos + 4]);
                long len = bytesToLong(b, b, data[start + pos + 7], data[start + pos + 8]);
                int offset = new Long(off - 262144 + indexSize).intValue();
                byte[] loc = Arrays.copyOfRange(data, offset, offset + new Long(len).intValue());
                return new String(loc, Charset.forName("UTF-8")).split("\t", -1);
            }
        }
        return null;
    }
    
    // 单个ip地址解析
    public static String evaluate(String ip) {
    	reloadResource();
        try {
            String res = Arrays.toString(IPUtils.find(ip)).replace(" ", "");
            return res.substring(1, res.length() - 1);
        } catch (Exception ex) {
            return IP_EVALUATE_ERROR_RESULT;
        } finally {
        	releaseResource();
        }
    }
    
    // 批量ip地址解析
    public static Map<String, String> evaluateBatch(List<String> ips) {
    	reloadResource();
    	Map<String, String> map = new HashMap<String, String>();
    	try {
			Iterator<String> iterator = ips.iterator();
			while(iterator.hasNext()) {
				String ip = iterator.next();
				try {
					String[] result = IPUtils.find(ip);
					if(result != null) {
						String res = Arrays.toString(result).replace(" ", "");
						map.put(ip, res.substring(1, res.length()-1));
						continue;
					}
					map.put(ip, IP_EVALUATE_ERROR_RESULT);
				}catch(Exception e) {
					map.put(ip, IP_EVALUATE_ERROR_RESULT);
				}
			}
		} finally {
        	releaseResource();
        }
    	return map;
    }
    
    public static void main(String[] args) throws Exception {

    	long start = System.currentTimeMillis();
    	List<String> ips = new ArrayList<String>();
    	CsvReader csv = new CsvReader("D:\\tanyao\\developfiles\\ip_demo_1000.csv");
    	csv.readHeaders();
    	while(csv.readRecord()) {
    		ips.add(csv.getRawRecord());
    	}
    	
    	Map<String, String> map = IPUtils.evaluateBatch(ips);
    	map = IPUtils.evaluateBatch(ips);
/*    	Map<String, String> map = new HashMap<String, String>();
    	ips.forEach(ip -> {
    		map.put(ip, evaluate(ip));
    	});*/
    	
    	long end = System.currentTimeMillis();
/*    	for(String ip : ips) {
    		System.out.println(ip + "," + map.get(ip));
    	}*/
    	System.out.println("------耗时：" + (end-start) + "ms");
    }
}
