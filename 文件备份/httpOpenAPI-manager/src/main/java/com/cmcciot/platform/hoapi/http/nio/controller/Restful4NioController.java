package com.cmcciot.platform.hoapi.http.nio.controller;

import com.cmcciot.common.cache.redis.FailoverJedisPool;
import com.cmcciot.platform.common.constants.RedisKeyPrefixConstant;
import com.cmcciot.platform.common.utils.*;
import com.cmcciot.platform.hoapi.auth.bean.User;
import com.cmcciot.platform.hoapi.auth.cache.UserInfoCache;
import com.cmcciot.platform.hoapi.auth.service.AuthUserService;
import com.cmcciot.platform.hoapi.auth.util.DigestAuthUtils;
import com.cmcciot.platform.hoapi.http.nio.HttpKeyConstant;
import com.cmcciot.platform.hoapi.http.nio.bean.HandledServiceBean;
import com.cmcciot.platform.hoapi.http.nio.bean.LockUserInfo;
import com.cmcciot.platform.hoapi.http.nio.bean.QueryLockUserRespons;
import com.cmcciot.platform.hoapi.http.nio.service.Restful4NioService;
import com.cmcciot.platform.hoapi.http.nio.threadPool.LockMsgSeq;
import com.cmcciot.platform.hoapi.http.nio.threadPool.TemporaryObject;
import com.cmcciot.platform.hoapi.http.nio.threadPool.ThreadPoolTask;

import net.sf.ehcache.CacheManager;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 迭代二
 * <功能详细描述>
 *
 * @author Administrator
 * @version [版本号, 2014年5月29日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Controller("restful4Nio")
@RequestMapping
public class Restful4NioController {
    /**
     * 用于存放序列号,未与服务ID组装(默认从0开始)
     */
    private static LockMsgSeq lockMsgSeq = new LockMsgSeq();
    /**
     * 日志记录
     */
    private Logger logger = Logger.getLogger(this.getClass());
    @Resource
    private AuthUserService AuthUserService;

    @Resource
    private CacheManager ehCacheManager;

    @Resource
    private Restful4NioService restful4NioService;

    @Autowired
    private FailoverJedisPool jedisPool;

    /**
     * 鉴权接口
     * <功能详细描述>
     *
     * @param request
     * @param response [参数说明]
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @RequestMapping(method = RequestMethod.POST, value = "/auth/nio/serviceManager")
    public void authServiceManagerNio(HttpServletRequest request,
                                      HttpServletResponse response) {
        //1.按照规定格式组装后的msgSeq(不能用内存中的msgSeq,因为传递时，值可能会变)
        String _msgSeq = "";
        //2.2.根据消息队列获取路由器返回的值,用于回执给APP
        String responseVal = "";
        String postStr = "";//请求参数
        //数据临时存放地，用于唤醒对象
        TemporaryObject temporaryObject = new TemporaryObject();
        try {
            //1.获取参数值
            StringBuffer buffer = new StringBuffer();
            InputStream in = request.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(in);
            byte[] bt = new byte[1024];
            int iRead;
            while ((iRead = bis.read(bt)) != -1) {
                buffer.append(new String(bt, 0, iRead, "UTF-8"));
            }
            //接口参数
            postStr = buffer.toString();
            logger.debug("远程请求地址：" + request.getRemoteAddr() + "，请求参数："
                    + postStr);
            //请求处理开始的时间
            Long startTimeSpan = System.currentTimeMillis();
            //2.1.生成全站唯一消息序列(线程安全+序列号)
            synchronized (lockMsgSeq) {
                //序列号+1
                Integer nMsgSeq = lockMsgSeq.getnMsgSeq() + 1;

                //配置文件ID + 消息序列,不足6位，前面以0补充
                final String STR_FORMAT = "000000";
                DecimalFormat df = new DecimalFormat(STR_FORMAT);
                _msgSeq = PropertyUtil.getValue("http.service.id") + "-" + DateUtil.getNowDate("yyyyMMddHHmmss") + "-" + df.format(nMsgSeq);

                //如果大于6位数
                if (nMsgSeq >= 999999) {//从新归零计数
                    lockMsgSeq.setnMsgSeq(0);
                } else {
                    lockMsgSeq.setnMsgSeq(nMsgSeq);
                }
            }

            //3.放置header信息
            StringBuilder headerMsg = new StringBuilder();
            boolean bAuth = response.containsHeader("Authorization-Info");

            //设置AUTH
            if (bAuth) {
                headerMsg.append("isAuth=\"true\"");

                //根据请求，取出header中的userID的值
                String[] headerEntries = DigestAuthUtils.splitIgnoringQuotes(request.getHeader("Authorization"), ',');
                Map<String, String> headerMap = DigestAuthUtils.splitEachArrayElementAndCreateMap(headerEntries, "=", "\"");
                String username = headerMap.get("Digest username");
                User user = UserInfoCache.getUserByUsername(username);
                if (user != null) {
                    headerMsg.append(",userID=\"" + user.getUserid() + "\"");
                }
            } else {
                headerMsg.append("isAuth=\"false\"");
            }
            //设置KEY
            headerMsg.append(",key=\""
                    + KeyUtil.makeMD5(PropertyUtil.getValue("http.service.id")
                    + PropertyUtil.getValue("http.service.password"))
                    + "\"");
            //设置serverID
            headerMsg.append(",serverID=\""
                    + PropertyUtil.getValue("http.service.id") + "\"");
            //设置SEQ(不能用内存中的msgSeq,因为传递时，值可能会变)
            headerMsg.append(",msgSeq=\"" + _msgSeq + "\"");
            Map<String, Object> postMap = JacksonUtil.jsonToMap(postStr);
            String msgType = (String) postMap.get("msgType");
            //6.2.向app返回值
            response.setContentType("text/html;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            //限流接口记录
            if(!StringUtil.isEmpty(msgType)){
            	if(HttpKeyConstant.LIMITEFLOWSERVICECOUNT.containsKey(msgType)){
            		HandledServiceBean hsb = (HandledServiceBean) HttpKeyConstant.LIMITEFLOWSERVICECOUNT.get(msgType);
            		if(hsb.getCount().get()>=hsb.getMaxAcount()){
            			 String timeoutResponse = "{\"version\":\"16\",\"msgType\":\""
                                 + msgType
                                 + "\",\"msgSeq\":\""
                                 + _msgSeq
                                 + "\",\"errorCode\":\"1\",\"description\":\"服务器正忙\"}";
                         response.getWriter().write(timeoutResponse);
                         logger.debug("[optTime:" + (System.currentTimeMillis() - startTimeSpan) + "ms] 限流接口: " +msgType+ "接口达到限流最大限制，已拒绝请求。返回结果的远程地址：" + request.getRemoteAddr()
                                 + "返回结果的时间：" + DateUtil.now() + "请求内容：" + timeoutResponse);
            			return;
            		}
            		hsb.getCount().addAndGet(1);
            		logger.debug("限流接口: "+msgType+ "次数加1，当前正在操作处理中次数为："+hsb.getCount().get()+"" );
            	}
            }
            
            //4.建立子线程，用于向服务管理传递参数
            HttpKeyConstant.PRODUCERPOOL.execute(new ThreadPoolTask(postStr,
                    headerMsg.toString(),msgType));

            //当前时间毫秒数
            Long currentTime = new Date().getTime();
            temporaryObject.setWaitTime(currentTime);
            //根据序列号放置结果
            HttpKeyConstant.MEMORYVALUE.put(_msgSeq, temporaryObject);

            //5.锁定消息序列号对应的临时对象（该对象处理服务管理所主动传递的值）
            synchronized (temporaryObject) {
                try {
                    temporaryObject.wait();
                } catch (InterruptedException e) {
                    logger.error("远程请求地址：" + request.getRemoteAddr() + "，错误内容："
                            + e);
                }
            }
            responseVal = temporaryObject.getResponseVal();

            //6.1.删除内存中的去掉的数据
            HttpKeyConstant.MEMORYVALUE.remove(_msgSeq);
          
            try {
                if (StringUtil.isEmpty(responseVal)) {
                    //获取报文体中的信息
                    String msgSeq = (String) postMap.get("msgSeq");
                    //超时返回报文
                    String timeoutResponse = "{\"version\":\"16\",\"msgType\":\""
                            + msgType
                            + "\",\"msgSeq\":\""
                            + msgSeq
                            + "\",\"errorCode\":\"1\",\"description\":\"请求超时\"}";

                    response.getWriter().write(timeoutResponse);

                    String dateNow = DateUtil.now();
                    logger.debug("[optTime:" + (System.currentTimeMillis() - startTimeSpan) + "ms] " + "请求超时。返回结果的远程地址：" + request.getRemoteAddr()
                            + "返回结果的时间：" + dateNow + "请求内容：" + postStr);
                } else {//因为服务管理把报文体中的msgSeq替换了，所以，在此处替换回来
                    Map<String, Object> reqMap = JacksonUtil.jsonToMap(postStr);
                    Map<String, Object> respMap = JacksonUtil.jsonToMap(responseVal);
                    respMap.put("msgSeq", reqMap.get("msgSeq"));
                    responseVal = JacksonUtil.mapToJson(respMap);
                    
                    /**
                     *  服务管理成果处理结果后，将msgType,errorCode封装到response的header中，方便flumeLogFilter使用
                     */
                    response.addHeader("msgType", (String)reqMap.get("msgType"));
                    response.addHeader("errorCode", (String)respMap.get("errorCode"));
                    
                    response.getWriter().write(responseVal);

                    String dateNow = DateUtil.now();
                    logger.debug("[optTime:" + (System.currentTimeMillis() - startTimeSpan) + "ms] " + "返回结果的远程地址：" + request.getRemoteAddr()
                            + "返回结果的时间：" + dateNow + "返回结果内容：" + responseVal);
                }
                //限流接口记录
                if(!StringUtil.isEmpty(msgType)){
                	if(HttpKeyConstant.LIMITEFLOWSERVICECOUNT.containsKey(msgType)){
                		HandledServiceBean hsb = (HandledServiceBean) HttpKeyConstant.LIMITEFLOWSERVICECOUNT.get(msgType);
                		hsb.getCount().addAndGet(-1);
                		logger.debug("限流接口: " +msgType+ "次数减-1，当前正在操作处理中次数为："+hsb.getCount().get()+"" );
                	}
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("远程请求地址：" + request.getRemoteAddr() + "，返回时结果时错误:"
                        + e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("远程请求地址：" + request.getRemoteAddr() + "，错误内容：" + e);
            //获取报文体中的信息
            Map<String, Object> postMap = JacksonUtil.jsonToMap(postStr);
            String msgType = (String) postMap.get("msgType");
            String msgSeq = (String) postMap.get("msgSeq");
            //统一错误
            String errorResponse = "{\"version\":\"16\",\"msgType\":\""
                    + msgType + "\",\"msgSeq\":\"" + msgSeq
                    + "\"\"errorCode\":\"1\",\"description\":\"服务器内部错误\"}";
            try {
                response.getWriter().write(errorResponse);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 非鉴权接口
     * <功能详细描述>
     *
     * @param request
     * @param response [参数说明]
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @RequestMapping(method = RequestMethod.POST, value = "/noauth/nio/serviceManager")
    public void noAuthServiceManagerNio(HttpServletRequest request,
                                        HttpServletResponse response) {
        //1.按照规定格式组装后的msgSeq(不能用内存中的msgSeq,因为传递时，值可能会变)
        String _msgSeq = "";
        //2.2.根据消息队列获取路由器返回的值,用于回执给APP
        String responseVal = "";
        //数据临时存放地，用于唤醒对象
        TemporaryObject temporaryObject = new TemporaryObject();
        String postStr = "";//请求参数
        try {
        	
            //1.获取参数值
/*            StringBuffer buffer = new StringBuffer();
            InputStream in = request.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(in);
            byte[] bt = new byte[1024];
            int iRead;
            while ((iRead = bis.read(bt)) != -1) {
                buffer.append(new String(bt, 0, iRead, "UTF-8"));
            }
            //接口参数
            postStr = buffer.toString();*/
        	
        	postStr = (String)MDC.get("postStr");
            logger.debug("远程请求地址：" + request.getRemoteAddr() + "，请求参数："
                    + postStr);
            Long startTimeSpan = System.currentTimeMillis();
            //2.1.生成全站唯一消息序列(线程安全+序列号)
            synchronized (lockMsgSeq) {
                //序列号+1
                Integer nMsgSeq = lockMsgSeq.getnMsgSeq() + 1;

                //配置文件ID + 消息序列,不足6位，前面以0补充
                final String STR_FORMAT = "000000";
                DecimalFormat df = new DecimalFormat(STR_FORMAT);
                _msgSeq = PropertyUtil.getValue("http.service.id") + "-" + DateUtil.getNowDate("yyyyMMddHHmmss") + "-" + df.format(nMsgSeq);

                //如果大于6位数
                if (nMsgSeq >= 999999) {//从新归零计数
                    lockMsgSeq.setnMsgSeq(0);
                } else {
                    lockMsgSeq.setnMsgSeq(nMsgSeq);
                }
            }
            //当前时间毫秒数
            Long currentTime = new Date().getTime();
            temporaryObject.setWaitTime(currentTime);
            //根据序列号放置结果
            HttpKeyConstant.MEMORYVALUE.put(_msgSeq, temporaryObject);

            //3.放置header信息
            StringBuilder headerMsg = new StringBuilder("isAuth=\"false\"");
            //设置KEY
            headerMsg.append(",key=\""
                    + KeyUtil.makeMD5(PropertyUtil.getValue("http.service.id")
                    + PropertyUtil.getValue("http.service.password"))
                    + "\"");
            //设置serverID
            headerMsg.append(",serverID=\""
                    + PropertyUtil.getValue("http.service.id") + "\"");
            //设置SEQ(不能用内存中的msgSeq,因为传递时，值可能会变)
            headerMsg.append(",msgSeq=\"" + _msgSeq + "\"");
            Map<String, Object> postMap = JacksonUtil.jsonToMap(postStr);
            String msgType = (String) postMap.get("msgType");

            //限流接口记录
            if(!StringUtil.isEmpty(msgType)){
            	if(HttpKeyConstant.LIMITEFLOWSERVICECOUNT.containsKey(msgType)){
            		HandledServiceBean hsb = (HandledServiceBean) HttpKeyConstant.LIMITEFLOWSERVICECOUNT.get(msgType);
            		if(hsb.getCount().get()>=hsb.getMaxAcount()){
            			 String timeoutResponse = "{\"version\":\"16\",\"msgType\":\""
                                 + msgType
                                 + "\",\"msgSeq\":\""
                                 + _msgSeq
                                 + "\",\"errorCode\":\"1\",\"description\":\"服务器正忙\"}";
                         response.getWriter().write(timeoutResponse);
                         logger.debug("[optTime:" + (System.currentTimeMillis() - startTimeSpan) + "ms] 限流接口: " +msgType+ "接口达到限流最大限制，已拒绝请求。返回结果的远程地址：" + request.getRemoteAddr()
                                 + "返回结果的时间：" + DateUtil.now() + "请求内容：" + timeoutResponse);
            			return;
            		}
            		hsb.getCount().addAndGet(1);
            		logger.debug("限流接口: " +msgType+ "次数加1，当前正在操作处理中次数为："+hsb.getCount().get()+"" );
            	}
            }
            
            //4.建立子线程，用于向服务管理传递参数
            HttpKeyConstant.PRODUCERPOOL.execute(new ThreadPoolTask(postStr,
                    headerMsg.toString(),msgType));

            //5.锁定消息序列号对应的临时对象（该对象处理服务管理所主动传递的值）
            synchronized (temporaryObject) {
                try {
                    temporaryObject.wait();
                } catch (InterruptedException e) {
                    logger.debug("远程请求地址：" + request.getRemoteAddr() + "，错误内容："
                            + e);
                }
            }
            responseVal = temporaryObject.getResponseVal();

            //6.1.删除内存中的去掉的数据
            HttpKeyConstant.MEMORYVALUE.remove(_msgSeq);
            //6.2.向app返回值
            response.setContentType("text/html;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            try {
                if (StringUtil.isEmpty(responseVal)) {
                    String msgSeq = (String) postMap.get("msgSeq");
                    //超时返回报文
                    String timeoutResponse = "{\"version\":\"16\",\"msgType\":\""
                            + msgType
                            + "\",\"msgSeq\":\""
                            + msgSeq
                            + "\",\"errorCode\":\"1\",\"description\":\"请求超时\"}";

                    response.getWriter().write(timeoutResponse);

                    String dateNow = DateUtil.now();
                    logger.debug("[optTime:" + (System.currentTimeMillis() - startTimeSpan) + "ms] " + "请求超时。返回结果的远程地址：" + request.getRemoteAddr()
                            + "返回结果的时间：" + dateNow + "请求内容：" + timeoutResponse);
                } else {//因为服务管理把报文体中的msgSeq替换了，所以，在此处替换回来
                    Map<String, Object> reqMap = JacksonUtil.jsonToMap(postStr);
                    Map<String, Object> respMap = JacksonUtil.jsonToMap(responseVal);
                    respMap.put("msgSeq", reqMap.get("msgSeq"));
                    responseVal = JacksonUtil.mapToJson(respMap);
                    
                    /**
                     *  服务管理成果处理结果后，将msgType,errorCode封装到response的header中，方便flumeLogFilter使用
                     */
                    response.addHeader("msgType", (String)reqMap.get("msgType"));
                    response.addHeader("errorCode", (String)respMap.get("errorCode"));
                    
                    response.getWriter().write(responseVal);

                    String dateNow = DateUtil.now();
                    logger.debug("[optTime:" + (System.currentTimeMillis() - startTimeSpan) + "ms] " + "返回结果的远程地址：" + request.getRemoteAddr()
                            + "返回结果的时间：" + dateNow + "返回结果内容：" + responseVal);
                }
              //限流接口记录
                if(!StringUtil.isEmpty(msgType)){
                	if(HttpKeyConstant.LIMITEFLOWSERVICECOUNT.containsKey(msgType)){
                		HandledServiceBean hsb = (HandledServiceBean) HttpKeyConstant.LIMITEFLOWSERVICECOUNT.get(msgType);
                		hsb.getCount().addAndGet(-1);
                		logger.debug("限流接口: " +msgType+ "次数减-1，当前正在操作处理中次数为："+hsb.getCount().get()+"" );
                	}
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.debug("远程请求地址：" + request.getRemoteAddr() + "，返回时结果时错误:"
                        + e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("远程请求地址：" + request.getRemoteAddr() + "，错误内容：" + e);

            //获取报文体中的信息
            Map<String, Object> postMap = JacksonUtil.jsonToMap(postStr);
            String msgType = (String) postMap.get("msgType");
            String msgSeq = (String) postMap.get("msgSeq");
            //统一错误
            String errorResponse = "{\"version\":\"16\",\"msgType\":\""
                    + msgType + "\",\"msgSeq\":\"" + msgSeq
                    + "\"\"errorCode\":\"1\",\"description\":\"服务器内部错误\"}";
            try {
                response.getWriter().write(errorResponse);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 服务管理主动调用的北向接口
     * <功能详细描述>
     *
     * @param request
     * @param response [参数说明]
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @RequestMapping(method = RequestMethod.POST, value = "/nio/response/serviceManager")
    public void responseResult(HttpServletRequest request,
                               HttpServletResponse response) {
        //1.获取参数值
        StringBuffer buffer = new StringBuffer();
        InputStream in;
        try {
            in = request.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(in);
            byte[] bt = new byte[1024];
            int iRead;
            while ((iRead = bis.read(bt)) != -1) {
                buffer.append(new String(bt, 0, iRead, "ISO-8859-1"));
            }
            //接口参数
            String postStr = new String(buffer.toString()
                    .getBytes("ISO-8859-1"), "UTF-8");
            //根据请求，取出header中的msgSeq的值
            String[] headerEntries = DigestAuthUtils.splitIgnoringQuotes(request.getHeader("HOA_auth"),
                    ',');
            Map<String, String> headerMap = DigestAuthUtils.splitEachArrayElementAndCreateMap(headerEntries,
                    "=",
                    "\"");
            //头部序列号
            String msgSeq = headerMap.get("msgSeq");
            response.getWriter()
                    .write("{\"errorCode\":\"0\",\"description\":\"已收到消息\"}");
            //根据msgSeq的值，获得内存中的对象，唤醒锁定的对象,并往对象中赋予结果值
            TemporaryObject temporaryObject = (TemporaryObject) HttpKeyConstant.MEMORYVALUE.get(msgSeq);
            if (temporaryObject != null) {//判断为空，则可能是因为已经超时，消息寄存器给删掉了。
                temporaryObject.setResponseVal(postStr);

                //因为存放对象的map是hashMap，非线程安全，所以，不用担心线程死锁问题。又而msgSeq的key是唯一的，不涉及到安全问题
                synchronized (temporaryObject) {//唤醒对象
                    temporaryObject.notifyAll();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空鉴权的 用户信息缓存
     * 服务管理主动调用北向接口，修改密码成功后调用此接口清空缓存
     *
     * @param request
     * @param response [参数说明]
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @RequestMapping(method = RequestMethod.POST, value = "/nio/userCache/clear/{userName}")
    public void clearCacheResult(HttpServletRequest request,
                                 HttpServletResponse response, @PathVariable("userName")
                                 String userName) {
        try {
            String ipWhites = PropertyUtil.getValue("http.ip.white");
            String[] ipArr = ipWhites.split(",");
            List<String> ipList = Arrays.asList(ipArr);
            String sourceIp = request.getRemoteHost();
            if (!ipList.contains(sourceIp)) {
                logger.error("非法来源地址：" + sourceIp);
                return;
            }
            
            /*  if (!StringUtil.isEmpty(userName))
              {
                  Cache cache = this.ehCacheManager.getCache("userInfoCache");
                  cache.remove("[" + userName + "]");
                  logger.debug("清除 openapi用户" + userName + "的缓存信息.");
                  return;
              }*/
            this.ehCacheManager.getCache("userInfoCache").flush();
            logger.debug("刷新 openapi用户信息缓存成功.");
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * 查询锁定用户
     * <功能详细描述>
     *
     * @param request  请求对象
     * @param response 返回对象
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @RequestMapping(method = RequestMethod.POST, value = "/nio/lockUser/queryLockUser")
    public void queryLockUser(HttpServletRequest request,
                              HttpServletResponse response) {
        try {
            QueryLockUserRespons queryLockuserRespons = new QueryLockUserRespons();
            //ip校验
            String ipWhites = PropertyUtil.getValue("http.ip.white");
            String[] ipArr = ipWhites.split(",");
            List<String> ipList = Arrays.asList(ipArr);
            String sourceIp = request.getRemoteHost();
            if (!ipList.contains(sourceIp)) {
                logger.error("armp请求的ip属于非法来源地址：" + sourceIp);
                queryLockuserRespons.setErroCode("4");
                queryLockuserRespons.setDescription("ip属于非法来源");
                response.getWriter()
                        .write(JacksonUtil.objToJson(queryLockuserRespons));
                return;
            }
            //1.获取参数值
            StringBuffer buffer = new StringBuffer();
            InputStream in;
            in = request.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(in);
            byte[] bt = new byte[1024];
            int iRead;
            while ((iRead = bis.read(bt)) != -1) {
                buffer.append(new String(bt, 0, iRead, "UTF-8"));
            }
            //接口参数
            String postStr = buffer.toString();
            Map<String, Object> userNameMap = JacksonUtil.jsonToMap(postStr);

            //获取要查询的用户名
            String userName = (String) userNameMap.get("userName");
            List<LockUserInfo> listLockUserInfo = null;
            if (StringUtil.isEmpty(userName)) {
                Set<String> keys = jedisPool.getJedis().keys(RedisKeyPrefixConstant.REDIS_KEY_PREFIX_LOCK_USER + "*");
                if (keys.size() > 0) {
                    listLockUserInfo = new ArrayList<LockUserInfo>();
                    LockUserInfo lockUserInfo = null;
                    for (String key : keys) {
                        if (listLockUserInfo.size() < 500) {
                            String name = key.replace(RedisKeyPrefixConstant.REDIS_KEY_PREFIX_LOCK_USER, "");
                            lockUserInfo = restful4NioService.buildLockMap(name);
                            if (null != lockUserInfo) {
                                listLockUserInfo.add(lockUserInfo);
                            }
                        } else {
                            break;
                        }
                    }
                    queryLockuserRespons.setLockUsers(listLockUserInfo);
                }
                queryLockuserRespons.setErroCode("0");
                queryLockuserRespons.setDescription("成功");
            } else {

                LockUserInfo lockUserInfo = restful4NioService.buildLockMap(userName);
                if (null != lockUserInfo) {
                    listLockUserInfo = new ArrayList<LockUserInfo>();
                    listLockUserInfo.add(lockUserInfo);
                    queryLockuserRespons.setLockUsers(listLockUserInfo);
                }

                queryLockuserRespons.setErroCode("0");
                queryLockuserRespons.setDescription("成功");
            }
            //返回消息
            response.getWriter()
                    .write(JacksonUtil.objToJson(queryLockuserRespons));
        } catch (IOException e) {
            logger.error(e);
        }
    }

    /**
     * 解锁用户
     * <功能详细描述>
     *
     * @param request  请求对象
     * @param response [参数说明]
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @RequestMapping(method = RequestMethod.POST, value = "/nio/lockUser/unLockUser")
    public void unLockUser(HttpServletRequest request,
                           HttpServletResponse response) {
        try {
            //1.获取参数值
            StringBuffer buffer = new StringBuffer();
            InputStream in;
            in = request.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(in);
            byte[] bt = new byte[1024];
            int iRead;
            while ((iRead = bis.read(bt)) != -1) {
                buffer.append(new String(bt, 0, iRead, "UTF-8"));
            }
            //接口参数
            String postStr = buffer.toString();
            Map<String, Object> userNameMap = JacksonUtil.jsonToMap(postStr);

            //解锁账号
            String userName = (String) userNameMap.get("userName");

            //ip校验
            String ipWhites = PropertyUtil.getValue("http.ip.white");
            String[] ipArr = ipWhites.split(",");
            List<String> ipList = Arrays.asList(ipArr);
            String sourceIp = request.getRemoteHost();
            if (!ipList.contains(sourceIp)) {
                logger.error("armp请求的ip属于非法来源地址：" + sourceIp);
                response.getWriter()
                        .write("{\"errorCode\":\"4\",\"description\":\"ip属于非法来源\"}");
                return;
            }

            if (StringUtil.isEmpty(userName)) {
                Set<String> keys = jedisPool.getJedis().keys(RedisKeyPrefixConstant.REDIS_KEY_PREFIX_LOCK_USER + "*");
                for (String key : keys) {
                    jedisPool.getJedis().del(key);
                }
            } else {
                jedisPool.getJedis().del(getUserFailLoginKey(userName));
            }
            response.getWriter()
                    .write("{\"errorCode\":\"0\",\"description\":\"解锁成功\"}");
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private String getUserFailLoginKey(String userName) {
        return RedisKeyPrefixConstant.REDIS_KEY_PREFIX_LOCK_USER + userName;
    }
}
