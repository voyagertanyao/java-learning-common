/**
 * 
 */
package com.cmcciot.platform.flume.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmcciot.platform.flume.exception.FlowLogException;
import com.cmcciot.platform.flume.surport.FlumeLogConstant;


/**
 * flume日志线程池
 * 需要同步到大数据平台的日志信息，通过该线程池运行
 * 可参考WorkerThreadPool.java的实现方式
 * @author tanyao
 *
 */
public class FlumeLogThreadPool {

	private static Logger logger = LoggerFactory.getLogger(FlumeLogThreadPool.class);
	
	private static ThreadPoolExecutor executor = null;
	
	private static FlumeLogThreadPool instance = new FlumeLogThreadPool();
	
	private FlumeLogThreadPool()
	{
		
	}
	
	public static FlumeLogThreadPool getInstance() {
		return instance;
	}
	
	/**
	 * 提交任务到线程池执行
	 * @param task
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Future submitTask(Runnable task) {
		if(executor == null) {
			init();
		}
		try {
			Future future = executor.submit(task);
			return future;
		} catch(Exception e) {
			logger.error("提交线程池失败:",e);
			throw new FlowLogException(e);
		}

	}
	
	/**
	 * 创建线程池
	 */
	public void init() {
		BlockingQueue<Runnable> bBueue = new LinkedBlockingQueue<Runnable>(
				FlumeLogConstant.EXECUTOR_TASK_QUEUE_SIZE);
		executor = new ThreadPoolExecutor(FlumeLogConstant.EXECUTORPOOL_CORE_SIZE,
				FlumeLogConstant.EXECUTOR_MAX_SIZE,
				FlumeLogConstant.EXECUTOR_KEEP_ALIVE_SECONDS,
				TimeUnit.SECONDS,
				bBueue,
				new ThreadPoolExecutor.CallerRunsPolicy());
		logger.debug("flume日志线程池启动.");
	}
	
	/**
	 * 关闭线程池
	 */
	public void shutdown() {
		if(executor != null) {
			executor.shutdown();
			logger.debug("flume日志线程池停止.");
		}
	}
	
}
