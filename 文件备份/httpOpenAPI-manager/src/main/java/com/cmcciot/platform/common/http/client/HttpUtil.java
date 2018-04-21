package com.cmcciot.platform.common.http.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmcciot.platform.common.exception.InternetException;
import com.cmcciot.platform.common.utils.PropertyUtil;
import com.cmcciot.platform.common.utils.StringUtil;

public class HttpUtil
{
	public static int MAX_TIMEOUT = 10000;  
    
	public static int MAX_CONN = 2000;
	public static int MAX_CONN_PERROUTE = 200;
	private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	
    private static RequestConfig requestConfig;  

    //所有http请求共用一个builder
	private static HttpClientBuilder httpClientBuilder;
    
    //每个https域名占用一个builder
    private static Map<String,HttpClientBuilder> httpsBuilderMap = new ConcurrentHashMap<String,HttpClientBuilder>();
  
    private static PoolingHttpClientConnectionManager httpCm;
  	static 
    {    
        RequestConfig.Builder configBuilder = RequestConfig.custom();  
        // 设置连接超时  
        configBuilder.setConnectTimeout(MAX_TIMEOUT/2);  
        // 设置读取超时  
        configBuilder.setSocketTimeout(MAX_TIMEOUT);  
        // 设置从连接池获取连接实例的超时  
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);  
        // 在提交请求之前 测试连接是否可用  
        configBuilder.setStaleConnectionCheckEnabled(true);  
        requestConfig = configBuilder.build();  
        httpClientBuilder = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);
        httpCm = new PoolingHttpClientConnectionManager();
        httpCm.setMaxTotal(MAX_CONN);
        httpCm.setDefaultMaxPerRoute(MAX_CONN_PERROUTE);
        httpClientBuilder.setConnectionManager(httpCm);
    }
    
	/**
	 * 以 http body的方式post提交请求
	 * 
	 * @param url
	 * @param content
	 * @param charset
	 * @return
	 */
	public static String postHttp(String url, String content, String charset)
	{
		return post(getHttpClientBuilder(),url,content,null,charset);
	}
	
	public static String postHttp(String url, String content,Map<String,String> headerMap, String charset)
	{
		return post(getHttpClientBuilder(),url,content,headerMap,charset);
	}

	public static String postHttps(String url, String content, final String charset)
	{
		if(StringUtil.isEmpty(url))
		{
			throw new InternetException("url为空");
		}
		if(!url.startsWith("https://"))
		{
			throw new InternetException("url["+url+"]不是合法的https地址");
		}
		
		Pattern ptn = Pattern.compile("^https://[A-Za-z0-9.:\\-]+");
		Matcher matcher = ptn.matcher(url);
		if(!matcher.find())
		{
			throw new InternetException("url["+url+"]不是合法的https地址");
		}
		String domain = matcher.group();
		
		HttpClientBuilder builder = getHttpsClientBuilder(domain);
		return post(builder,url,content,null,charset);
	}
	
	private static String post(HttpClientBuilder builder, String url, final String content,Map<String,String> headerMap, final String charset)
	{
        //HttpClient  
		CloseableHttpClient client = builder.build();
		HttpPost method = new HttpPost(url);

		String result = null;
		CloseableHttpResponse httpResponse = null;
		logger.debug("请求地址:" + url);
		logger.debug("请求报文:" + content);
		try
		{
			if(headerMap!=null && headerMap.size()>0)
			{
				Set<String> keys = headerMap.keySet();
				for(String key : keys)
				{
					method.addHeader(key, headerMap.get(key));
				}
			}
			ContentProducer cp = new ContentProducer()
			{
				@Override
				public void writeTo(OutputStream outstream) throws IOException
				{
					Writer writer = new OutputStreamWriter(outstream, charset);
					writer.write(content);
					writer.flush();
				}
			};
			method.setEntity(new EntityTemplate(cp));
			httpResponse = client.execute(method);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			// 请求发送成功，并得到响应
			if (statusCode == 200)
			{
				try
				{
					// 读取服务器返回过来的字符串数据
					result = EntityUtils.toString(httpResponse.getEntity(),charset);
				} catch (Exception e)
				{
					logger.error("读取返回字符串数据失败:" + url, e);
				}
			}
			else if(statusCode == HttpStatus.SC_MOVED_TEMPORARILY  
                    || statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
			{
				Header[] headers = httpResponse.getHeaders("Location");
				if (headers != null && headers.length > 0)
				{
					String redirectUrl = headers[0].getValue();
					logger.debug("返回发生了重定向，URL:" + redirectUrl);
					redirectUrl = redirectUrl.replace(" ", "%20");
					result = get(redirectUrl, charset);
				}
			}
			else
			{
				logger.error("接口返回响应失败：response="+httpResponse.getStatusLine());
			}
		}
		catch (Exception e)
		{
			logger.error("post请求提交失败", e);
			throw new InternetException(e);
		}
		finally
		{
			try
			{
				if(httpResponse!=null)
				{
					httpResponse.close();
				}
			} catch (Exception e)
			{
			}
		}
		logger.debug("响应报文:" + result);
		return result;
	
	}
	
	/**
	 * 以表单的形式 post提交http请求
	 * @param url
	 * @param formMap
	 * @param charset
	 * @return
	 */
	public static String postHttpForm(String url, Map<String,String> formMap, Map<String,String> headerMap,String charset)
	{
        //HttpClient  
        CloseableHttpClient client = getHttpClientBuilder().build();  
		HttpPost method = new HttpPost(url);

		String result = null;
		CloseableHttpResponse httpResponse = null;
		logger.debug("请求地址:" + url);
		logger.debug("请求内容:" + formMap);
		try
		{
			if(headerMap!=null && headerMap.size()>0)
			{
				Set<String> keys = headerMap.keySet();
				for(String key : keys)
				{
					method.addHeader(key, headerMap.get(key));
				}
			}
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			if(formMap!=null && formMap.size()>0)
			{				
				Set<String> keys = formMap.keySet();
				for(String key : keys)
				{
					nvps.add(new BasicNameValuePair(key, formMap.get(key)));
				}
				
			}
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvps, charset);			
			method.setEntity(entity);
			httpResponse = client.execute(method);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			// 请求发送成功，并得到响应
			if (statusCode == 200)
			{
				try
				{
					// 读取服务器返回过来的字符串数据
					result = EntityUtils.toString(httpResponse.getEntity(),charset);
				} catch (Exception e)
				{
					logger.error("读取返回字符串数据失败:" + url, e);
				}
			}
			else if(statusCode == HttpStatus.SC_MOVED_TEMPORARILY  
                    || statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
			{
				Header[] headers = httpResponse.getHeaders("Location");
				if (headers != null && headers.length > 0)
				{
					String redirectUrl = headers[0].getValue();
					logger.debug("返回发生了重定向，URL:" + redirectUrl);
					redirectUrl = redirectUrl.replace(" ", "%20");
					result = get(redirectUrl, charset);
				}
			}
			else
			{
				logger.error("接口返回响应失败：response="+httpResponse.getStatusLine());
			}
		}
		catch (Exception e)
		{
			logger.error("post请求提交失败", e);
			throw new InternetException(e);
		}
		finally
		{
			try
			{
				if(httpResponse!=null)
				{
					httpResponse.close();
				}
			} catch (Exception e)
			{
			}
		}
		logger.debug("响应报文:" + result);
		return result;
	
	}
	
	private static String get(String url,String charset)
	{
		CloseableHttpClient client = getHttpClientBuilder().build();
		HttpGet method = new HttpGet(url);
		String result = null;
		HttpResponse httpResponse = null;
		logger.debug("请求地址:" + url);
		
		try
		{
			httpResponse = client.execute(method);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			// 请求发送成功，并得到响应
			if (statusCode == 200)
			{
				try
				{
					// 读取服务器返回过来的字符串数据
					result = EntityUtils.toString(httpResponse.getEntity(), charset);
				} catch (Exception e)
				{
					logger.error("读取返回字符串数据失败:" + url, e);
				}
			}
			else if(statusCode == HttpStatus.SC_MOVED_TEMPORARILY  
                    || statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
			{
				Header[] headers = httpResponse.getHeaders("Location");
				if (headers != null && headers.length > 0)
				{
					String redirectUrl = headers[0].getValue();
					logger.debug("返回发生了重定向，URL:" + redirectUrl);
					redirectUrl = redirectUrl.replace(" ", "%20");
					result = get(url,charset);
				}
			}
		}
		catch (Exception e)
		{
			logger.error("get请求提交失败", e);
			throw new InternetException(e);
		}
		finally
		{
			try
			{
				client.close();
			} catch (IOException e)
			{
			}
		}
		logger.debug("响应报文:" + result);
		return result;
	}
	
	private static SSLConnectionSocketFactory createSSLConnSocketFactory() 
	{
		SSLConnectionSocketFactory sslsf = null;
		try
		{
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy()
			{
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException
				{
					return true;
				}
			}).build();
			sslsf = new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		} 
		catch (GeneralSecurityException e)
		{
			logger.error("https构造SSLConnectionSocketFactory出错",e);
		}
		return sslsf;
	}  
	
	 
    public synchronized static HttpClientBuilder getHttpsClientBuilder(String domain)
    {
    	HttpClientBuilder builder = httpsBuilderMap.get(domain);
    	if(builder==null)
		{
    		builder = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);
    		SSLConnectionSocketFactory sslCSFactory = createSSLConnSocketFactory();
    		builder.setSSLSocketFactory(sslCSFactory);
    		httpsBuilderMap.put(domain, builder);
		}
    	return builder;
    }
	
	private static HttpClientBuilder getHttpClientBuilder()
    {
    	lastCreateTime = System.currentTimeMillis();
    	startup();
    	return httpClientBuilder;
    }	

    private static volatile boolean running = false;
    private static long lastCreateTime = 0;
    private static void startup()
    {
    	if(running)
    	{
    		return;
    	}
    	running = true;
    	Thread th = new Thread("超期httpclient清理线程")
    	{
    		@Override
    		public void run()
    		{
    			try
    			{
    				logger.info(this.getName()+"启动===");
    				while(running)
    				{
    					httpCm.closeExpiredConnections();
    					httpCm.closeIdleConnections(MAX_TIMEOUT, TimeUnit.MILLISECONDS);
    					long now = System.currentTimeMillis();
    					if(now - lastCreateTime > 3*MAX_TIMEOUT)
    					{
    						break;
    					}
    					Thread.sleep(5000);    					
    				}
    			}
    			catch(Exception e)
    			{
    				logger.error(this.getName()+"清除超时和空闲httpclient出错",e);
    			}
    			running = false;
    			logger.info(this.getName()+"暂时退出===");
    		}
    	};
    	th.start();
    }

    public static void shutdown()
    {
    	running = false;
    }
    
    public static void init()
    {
    	String mc = PropertyUtil.getValue("http.client.maxconn");
    	String mcp = PropertyUtil.getValue("http.client.maxconn.perroute");
    	String mct = PropertyUtil.getValue("http.client.maxtimeout");
    	try
    	{
    		int maxconn = Integer.parseInt(mc);
    		int maxconnRoute = Integer.parseInt(mcp);
    		int maxtimeout = Integer.parseInt(mct);
    		MAX_CONN = maxconn;
    		MAX_CONN_PERROUTE = maxconnRoute;
    		MAX_TIMEOUT = maxtimeout;
    	}
    	catch(Exception e)
    	{
    		logger.warn("httpClient配置加载出错：http.client.maxconn="+mc+",http.client.maxconn.perroute="+mcp+",http.client.maxtimeout="+mct);
    	}
    }
	
	public static void main(String[] args)
	{
		String url = "http://openapi.stg.closeli.com.cn/andmu/v1/service/batch";
		Map<String,String> fromMap = new HashMap<String,String>();
		fromMap.put("jsonObject", "{\"msgSeq\":\"123\",\"version\":\"11\",\"msgType\":\"MSG_P_SEND_LOCALPLAYINFO_REQ\",\"dev\":{\"devID\":\"d11000000001\"}}");
		fromMap.put("accessKey", "910fb531-4d0");	
		String resp = postHttpForm(url, fromMap,null, "UTF-8");
		System.out.println(resp);
		
	}
	
	
}
