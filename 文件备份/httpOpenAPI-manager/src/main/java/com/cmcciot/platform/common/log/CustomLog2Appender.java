package com.cmcciot.platform.common.log;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.cmcciot.platform.common.utils.DateTools;



/**
 * 自定义Appender，继承 AbstractAppender 只需要覆盖自已想要的方法即可<br>
 * 类上面的注解是用来设置配置文件中的标签。
 */
@Plugin(name = "TextArea", category = "Core", elementType = "appender", printObject = true)
public class CustomLog2Appender extends AbstractAppender {
	
	private long nextCheck = 0 ; 
	
	private static final long serialVersionUID = -830237775522429777L;
	
	private String fileName;

	private String datePattern = "yyyy-MM-dd";
	 /**
	   * Determines the size of IO buffer be. Default is 8K. 
	   */
	protected int bufferSize = 8*1024;
	/**
    Do we do bufferedIO? */
	protected boolean bufferedIO = false;
    /**
     * @fields serialVersionUID
     */
   
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();

  //电话号码正则表达式
  	static Pattern p = Pattern.compile("((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0-9]))\\d{8}");
    //需要实现的构造方法，直接使用父类就行
    protected CustomLog2Appender(final String name, final Filter filter, final Layout<? extends Serializable> layout,
            final boolean ignoreExceptions,String fileName) {
        super(name, filter, layout, ignoreExceptions);
        this.fileName = fileName;
        this.nextCheck = this.getLogUpdateTime();
    }

    @Override
    public void append(LogEvent event) {
        readLock.lock();
        try {
            final byte[] bytes = getLayout().toByteArray(event);//日志二进制文件，输出到指定位置就行
            //下面这个是要实现的自定义逻辑
            String mesg = new String(bytes);
            mesg = replacePhone(mesg);
            //写入日志文件
            if(System.currentTimeMillis()>nextCheck){
            	//如果日期到了新的一天则记录新的一天日志，将osh.log转换成日期形式保存。
            	this.rollOver();
            	//更新时间
            	nextCheck = this.getLogUpdateTime();
            	//创建新的日志文件
            	writerFileCreate(mesg.getBytes());
            	
            }else{
            	writerFileAppend(mesg.getBytes());
            }
            
            
            
        } catch (Exception ex) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(ex);
            }
        } finally {
            readLock.unlock();
        }
    }

    // 下面这个方法可以接收配置文件中的参数信息
    @PluginFactory
    public static CustomLog2Appender createAppender(@PluginAttribute("name") String name,
    		@PluginAttribute("fileName") String fileName,
            @PluginElement("Filter") final Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
        if (name == null) {
            LOGGER.error("No name provided for MyCustomAppenderImpl");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new CustomLog2Appender(name, filter, layout, ignoreExceptions,fileName);
    }
    /**
     * 写入日志到文件追加
     * @param log
     */
    private void writerFileAppend(byte[] log) {
        try {        	
            Files.write(Paths.get(fileName), log, StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.error("write file exception", e);
        }
    }
    /**
     * 写入日志到文件覆盖
     * @param log
     */
    private void writerFileCreate(byte[] log) {
        try {
            Files.write(Paths.get(fileName), log, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("write file exception", e);
        }
    }
    /**
     * 模糊日志中的电话号码
     * @param message
     * @return
     */
    private static String replacePhone(String message)
	{
		Matcher m = p.matcher(message);
		StringBuffer sb = new StringBuffer();
		sb.append(message);
		while (m.find())
		{
			sb.replace(m.start() + 3, m.start() + 7, "****");
		}
		return sb.toString();
	}
    /**
     * 获取初始日志文件变更时间
     * @return
     */
	private long getLogUpdateTime(){
		Calendar c = Calendar.getInstance();
	    c.setTime(new Date());
	    c.add(Calendar.DATE,1);
	    Date date = DateTools.parse(DateTools.format( c.getTime(), datePattern), datePattern);
		return  date.getTime();
	}
	
	/**
	 * 日志按日期处理
	 * @throws IOException
	 */
	private void  rollOver() throws IOException
	{
		//获取昨天日期格式日志文件名
		String datedFilename = fileName +"."+ DateTools.format(new Date(nextCheck-1), datePattern);
		File target = new File(datedFilename);
		if (target.exists())
		{
			target.delete();
		}
		try{
			Files.copy(Paths.get(fileName), Paths.get(datedFilename), StandardCopyOption.REPLACE_EXISTING);
			//boolean result = file.renameTo(target);
		}catch(Exception e){
		}
			
	}
    public static void main(String[] args) {
    	/*Calendar c = Calendar.getInstance();
	    c.setTime(new Date());
	    c.add(Calendar.DATE,1);
	    Date date = DateTools.parse(DateTools.format( c.getTime(), "yyyy-MM-dd"), "yyyy-MM-dd");*/
    	String datedFilename =  "E:/osh2.log" +"."+ DateTools.format(new Date(), "yyyy-MM-dd");
		File target = new File(datedFilename);
		if (target.exists())
		{
			target.delete();
		}
		try{
			Files.copy(Paths.get( "E:/osh2.log"), Paths.get(datedFilename), StandardCopyOption.REPLACE_EXISTING);
			//boolean result = file.renameTo(target);
		}catch(Exception e){
		}
    	
    	
    	/*String datedFilename = "E:/osh2.log" +"."+ DateTools.format(new Date(), "yyyy-MM-dd");
		File target = new File(datedFilename);
		if (target.exists())
		{
			target.delete();
		}

		File file = new File("E:/osh2.log");
		boolean result = file.renameTo(target);
		System.out.println(result);*/
	}
}