package com.cmcciot.platform.common.exception;

public class InternetException extends RuntimeException
{
	private static final long serialVersionUID = -1256131313740923643L;

	public InternetException()
	{
		super();
	}

	public InternetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InternetException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InternetException(String message)
	{
		super(message);
	}

	public InternetException(Throwable cause)
	{
		super(cause);
	}

}
