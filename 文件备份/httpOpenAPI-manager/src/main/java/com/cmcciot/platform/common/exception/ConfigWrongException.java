package com.cmcciot.platform.common.exception;


/**
 * 配置错误异常
 *@author:wangxw
 *@date：2011-10-31 上午04:37:17
 */

public class ConfigWrongException extends RuntimeException
{
	private static final long serialVersionUID = -6261883848536821867L;

	public ConfigWrongException(String message)
	{
		super(message);
	}

	public ConfigWrongException(Exception e)
	{
		super(e);
	}

	public ConfigWrongException(String message, Throwable cause)
	{
		super(message, cause);
	}

	
}
