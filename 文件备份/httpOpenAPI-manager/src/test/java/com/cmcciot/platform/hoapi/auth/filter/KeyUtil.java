/*
 * 文 件 名:  NonceUtil.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月9日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.auth.filter;

import com.cmcciot.platform.common.utils.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * <一句话功能简述> <功能详细描述>
 * 
 * @author Administrator
 * @version [版本号, 2014年4月9日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class KeyUtil
{
	public static void main(String[] args)
	{
		System.out.println(generateCnonce());
	}

	/**
	 * 生成cnonce
	 * <功能详细描述>
	 * @return [参数说明]
	 * 
	 * @return String [返回类型说明]
	 * @exception throws [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static String generateCnonce()
	{
		Random rand = new Random();
		String source = System.currentTimeMillis() + "-" + rand.nextInt(10000);

		MessageDigest digest;
		try
		{
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e)
		{
			throw new IllegalStateException("No MD5 algorithm available!");
		}
		
		String s = new String(Hex.encode(digest.digest(source.getBytes())));
		return s.substring(8,24);
	}
	
		

}
