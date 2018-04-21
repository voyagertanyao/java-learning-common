/**
 * 
 */
package com.cmcciot.platform.flume.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @author tanyao
 *
 */
public class FlumeResponseWrapper extends HttpServletResponseWrapper {

	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private PrintWriter pw;
	private HttpServletResponse response;
	public FlumeResponseWrapper(HttpServletResponse response) {
		super(response);
		this.response = response;
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return new FlumeServletOutputStream(out);
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(new OutputStreamWriter(out,this.response.getCharacterEncoding()));
	}
	
	public byte[] getByteArray(){
        try{
            if(pw!=null){
                pw.close();
            }
            if(out!=null){
                out.flush();
                return out.toByteArray();
            }
            return null;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
