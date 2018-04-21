package com.cmcciot.platform.hoapi.http.nio.listener;

import com.cmcciot.platform.common.utils.DateUtil;
import com.cmcciot.platform.common.utils.StringUtil;
import com.cmcciot.platform.hoapi.http.nio.HttpKeyConstant;
import com.cmcciot.platform.hoapi.http.nio.threadPool.TemporaryObject;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * 每隔三秒钟轮询一次，判断请求是否已过十秒钟
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author Administrator
 * @version [版本号, 2014年4月28日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class TimingListener implements ServletContextListener {
    /**
     * 日志记录
     */
    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void contextDestroyed(ServletContextEvent e) {
        //关闭监听线程
        HttpKeyConstant.THREADFLAG = false;
        //关闭线程
        HttpKeyConstant.PRODUCERPOOL.shutdownNow();
    }

    /**
     * 每隔三秒钟轮询一次，判断请求是否已过十秒钟
     *
     * @param e
     */
    @Override
    public void contextInitialized(ServletContextEvent e) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (HttpKeyConstant.THREADFLAG) {
                    if (HttpKeyConstant.MEMORYVALUE != null && HttpKeyConstant.MEMORYVALUE.size() > 0) {
                        logger.info("当前总请求有："
                                + HttpKeyConstant.MEMORYVALUE.size());
                        for (Map.Entry<String, Object> map : HttpKeyConstant.MEMORYVALUE.entrySet()) {
                            TemporaryObject t = (TemporaryObject) map.getValue();
                            try {
                                Long currentTime = new Date().getTime();
                                if (!StringUtil.isEmpty(t.getWaitTime() + "")
                                        && (currentTime - t.getWaitTime()) > HttpKeyConstant.HTTP_TIMEOUT) {
                                    synchronized (t) {
                                        try {
                                            synchronized (HttpKeyConstant.TIMEEOUTCOUNT) {
                                                int i = Integer.parseInt(HttpKeyConstant.TIMEEOUTCOUNT.toString()) + 1;
                                                //替换新的字符串
                                                HttpKeyConstant.TIMEEOUTCOUNT.replace(0,
                                                        HttpKeyConstant.TIMEEOUTCOUNT.length(),
                                                        i + "");
                                                logger.info("超时请求总个数:"
                                                        + HttpKeyConstant.TIMEEOUTCOUNT.toString());
                                            }
                                            logger.debug("msgSeq="
                                                    + map.getKey()
                                                    + "已超时。请求时间："
                                                    + DateUtil.getDateTime(new Date(
                                                    t.getWaitTime())));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        } finally {
                                            t.notifyAll();
                                        }
                                    }
                                }
                            } catch (NullPointerException e) {
                                logger.error("msgSeq=" + map.getKey()
                                        + ";已被消息寄存器删除！");
                            }
                        }
                    }

                    try {//休眠3秒钟
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        //启动线程
        new Thread(runnable).start();
    }
}
