/*
 * 文 件 名:  DigestAuthUtils.java
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
import com.cmcciot.platform.common.utils.StringUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <一句话功能简述> <功能详细描述>
 * 
 * @author Administrator
 * @version [版本号, 2014年4月9日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class DigestAuthUtils
{

	

	/**
	 * response生成算法
	 * KD（H（A1），unq（nonce-value）“:”nonce-value“:”unq(cnonce-value)“:”unq(qop-value)“:”H（A2））
	 * 
	 * 	A1=unq(username-value)”:”unq(real-value)”:”passwd
	 	如果qop等于auth，A2=Method”:”digest-uri-value
		其中：
		H(data)=MD5(data)
		KD(secret，data)=H(concat(secret,”:”data))
		unq(X)代表取消X前后的引号
		Method=GET或者POST
	 * @param da
	 * @param method
	 * @param user
	 * @param cn
	 * @return [参数说明]
	 * 
	 * @return String [返回类型说明]
	 * @exception throws [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static String generateResponse(DigestData da, String method)
	{
		MessageDigest digest = null;
		try
		{
			digest = MessageDigest.getInstance("MD5");
		} 
		catch (NoSuchAlgorithmException e)
		{
			throw new IllegalStateException("No MD5 algorithm available!");
		}
				
		//A1=unq(username-value)”:”unq(real-value)”:”passwd
		String a1 = StringUtil.trimQuot(da.getUsername()) + ":"
				+ StringUtil.trimQuot(da.getRealm()) + ":" + da.getPassword();
		//第一个参数为h_a1
		String h_a1 = new String(Hex.encode(digest.digest(a1.getBytes())));
		
		//A2=Method”:”digest-uri-value
		String a2 = method+":"+da.getUri();
		String h_a2 = new String(Hex.encode(digest.digest(a2.getBytes())));
		
		//第二个参数为 unq（nonce-value）“:”nonce-value“:”unq(cnonce-value)“:”unq(qop-value)“:”H（A2）
		String data = StringUtil.trimQuot(da.getNonce())+":"
				+ da.getNonce()+":"+StringUtil.trimQuot(da.getCnonce())+":"
				+ StringUtil.trimQuot(da.getQop())+":"+h_a2;
		
		String kd_s_d = h_a1+":"+data;
		String result = new String(Hex.encode(digest.digest(kd_s_d.getBytes())));
		return result;
	}
		
	public static void main(String[] args)throws Exception
	{
		String s = "sadfsdfs";
		MessageDigest digest = null;
		try
		{
			digest = MessageDigest.getInstance("MD5");
		} 
		catch (NoSuchAlgorithmException e)
		{
			throw new IllegalStateException("No MD5 algorithm available!");
		}
		System.out.println(new String(Hex.encode(digest.digest(s.getBytes()))));
		System.out.println(new String(digest.digest(s.getBytes("UTF-8"))));
	}
}
