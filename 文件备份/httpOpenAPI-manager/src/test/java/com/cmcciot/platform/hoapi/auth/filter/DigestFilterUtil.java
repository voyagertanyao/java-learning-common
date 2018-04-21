package com.cmcciot.platform.hoapi.auth.filter;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 *
 * Created by Ginda.Tseng on 2016/2/14.
 */
public class DigestFilterUtil {

    /**
     * 发送鉴权工具请求
     * @param url 请求地址
     * @param message 请求报文
     * @return 响应报文
     */
    public static String sendMessage(String url, final String message) {
        //首次挑战
        HttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager());
        HttpPost method = new HttpPost(url);

        DigestData data = new DigestData();
        StringBuilder firstHead = new StringBuilder();
//            data.setUsername();
//            data.setPassword(password);
        firstHead.append("");
        firstHead.append("Digest ");
        firstHead.append("username=\"" + data.getUsername() + "\"");
        firstHead.append(", uri=\"uri\"");
        firstHead.append(", algorithm=\"MD5\"");
        method.setHeader("Authorization", firstHead.toString());

        //发送消息
        HttpResponse httpResponse = null;
        try {
            httpResponse = client.execute(method);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("第一次挑战结果:" + httpResponse.getStatusLine().toString());
        Header[] herders = httpResponse.getAllHeaders();
        for (Header h : herders)
        {
            System.out.println(h.getName() + ":" + h.getValue());
        }

        int times = 1;

        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED)
        {
            //获取挑战后的鉴权信息
            Header header = httpResponse.getHeaders("WWW-Authenticate")[0];
            HeaderElement[] eles = header.getElements();
            times = 1;
            for (int i = 0; i < eles.length; i++)
            {
                HeaderElement ele = eles[i];
                String name = null;
                if (i == 0)
                {
                    if (!ele.getName().startsWith("Digest "))
                    {
                        throw new RuntimeException("not digest auth");
                    }
                    name = ele.getName().substring(7);
                }
                else
                {
                    name = ele.getName();
                }
                if ("realm".equals(name))
                {
                    data.setRealm(ele.getValue());
                }
                else if ("nonce".equals(name))
                {
                    data.setNonce(ele.getValue());
                }
                else if ("qop".equals(name))
                {
                    data.setQop(ele.getValue());
                }
                else if ("opaque".equals(name))
                {
                    data.setOpaque(ele.getValue());
                }
            }
        }

        //生成请求鉴权头，第二次发起请求
//        data.setUri(uri);
        data.setCnonce(KeyUtil.generateCnonce());
        String responseStr = DigestAuthUtils.generateResponse(data, "POST");
        StringBuilder hv = new StringBuilder();
        hv.append("Digest ");
        hv.append("username=\"" + data.getUsername() + "\"");
        hv.append(", realm=\"" + data.getRealm() + "\"");
        hv.append(", nonce=\"" + data.getNonce() + "\"");
        hv.append(", uri=\"" + data.getUri() + "\"");
        hv.append(", algorithm=\"MD5\"");
        hv.append(", response=\"" + responseStr + "\"");
        hv.append(", opaque=\"" + data.getOpaque() + "\"");
        hv.append(", qop=\"" + data.getQop() + "\"");
        hv.append(", cnonce=\"" + data.getCnonce() + "\"");
        hv.append(", nc=\"" + String.format("%08d", times) + "\"");

        method.setHeader("Authorization", hv.toString());
        ContentProducer cp = new ContentProducer()
        {
            @Override
            public void writeTo(OutputStream outstream)
                    throws IOException
            {
                Writer writer = new OutputStreamWriter(outstream,
                        "UTF-8");
                writer.write(message);
                writer.flush();
            }
        };
        method.setEntity(new EntityTemplate(cp));
        try {
            httpResponse = client.execute(method);
        } catch (IOException e) {
            e.printStackTrace();
        }
        times++;
        System.out.println("第" + times + "返回code:"
                + httpResponse.getStatusLine().toString());
        herders = httpResponse.getAllHeaders();
        for (Header h : herders)
        {
            System.out.println(h.getName() + ":" + h.getValue());
        }

        String result = null;
        if (String.valueOf(httpResponse.getStatusLine().getStatusCode()).startsWith("20"))
        {
            try {
                result = EntityUtils.toString(httpResponse.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            result = httpResponse.toString();
        }

        return result;
    }
}