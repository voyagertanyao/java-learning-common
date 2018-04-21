/**
 * 
 */
package com.cmcciot.platform.flume.surport;

/**
 * flume日志处理相关配置参数
 * @author tanyao
 *
 */
public abstract class FlumeLogConstant {
	
	/**
	 * handler名称后缀
	 */
	public final static String FLUME_HANDLER_NAME_SUFFIX = "Handler";
	

	/**
	 *  flume日志处理线程池配置：核心线程数
	 */
	public final static int EXECUTORPOOL_CORE_SIZE = 50;
	
	/**
	 *  flume日志处理线程池配置：最大线程数
	 */
	public final static int EXECUTOR_MAX_SIZE = 100;
	
	/**
	 *  flume日志处理线程池配置：结束空闲线程时间
	 */
	public final static long EXECUTOR_KEEP_ALIVE_SECONDS = 180;
	
	/**
	 *  flume日志处理线程池配置：等待任务队列大小
	 */
	public final static int EXECUTOR_TASK_QUEUE_SIZE = 100;
}
