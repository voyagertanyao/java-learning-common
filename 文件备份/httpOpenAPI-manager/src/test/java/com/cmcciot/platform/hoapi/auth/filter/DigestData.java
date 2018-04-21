/*
 * 文 件 名:  DigestData.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月10日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.auth.filter;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * 
 * @author  Administrator
 * @version  [版本号, 2014年4月10日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class DigestData
{
	private String username = "test";
	private String password = "123456";
	private String realm;
	private String qop;
	private String nonce;
	private String opaque;
	
	private String uri;
	private String cnonce;

	/** 
	 * <默认构造函数>
	 */
	public DigestData()
	{
		
	}
	
	public DigestData(String realm, String qop, String nonce, String opaque)
	{
		this.realm = realm;
		this.qop = qop;
		this.nonce = nonce;
		this.opaque = opaque;
	}

	/**
	 * @return 返回 username
	 */
	public String getUsername()
	{
		return username;
	}

	/**
	 * @param 对username进行赋值
	 */
	public void setUsername(String username)
	{
		this.username = username;
	}

	/**
	 * @return 返回 password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param 对password进行赋值
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @return 返回 realm
	 */
	public String getRealm()
	{
		return realm;
	}

	/**
	 * @param 对realm进行赋值
	 */
	public void setRealm(String realm)
	{
		this.realm = realm;
	}

	/**
	 * @return 返回 qop
	 */
	public String getQop()
	{
		return qop;
	}

	/**
	 * @param 对qop进行赋值
	 */
	public void setQop(String qop)
	{
		this.qop = qop;
	}

	/**
	 * @return 返回 nonce
	 */
	public String getNonce()
	{
		return nonce;
	}

	/**
	 * @param 对nonce进行赋值
	 */
	public void setNonce(String nonce)
	{
		this.nonce = nonce;
	}

	/**
	 * @return 返回 opaque
	 */
	public String getOpaque()
	{
		return opaque;
	}

	/**
	 * @param 对opaque进行赋值
	 */
	public void setOpaque(String opaque)
	{
		this.opaque = opaque;
	}

	/**
	 * @return 返回 uri
	 */
	public String getUri()
	{
		return uri;
	}

	/**
	 * @param 对uri进行赋值
	 */
	public void setUri(String uri)
	{
		this.uri = uri;
	}

	/**
	 * @return 返回 cnonce
	 */
	public String getCnonce()
	{
		return cnonce;
	}

	/**
	 * @param 对cnonce进行赋值
	 */
	public void setCnonce(String cnonce)
	{
		this.cnonce = cnonce;
	}

}
