/**
 * 
 */
package com.cmcciot.platform.flume.exception;

/**
 * flume日志处理异常
 * @author tanyao
 *
 */
public class FlowLogException extends RuntimeException{

	private static final long serialVersionUID = -2535964656976222730L;
	
	public FlowLogException(String message) {
		super(message);
	}
	
	public FlowLogException(Throwable cause) {
		super(cause);
	}
	
	public FlowLogException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
