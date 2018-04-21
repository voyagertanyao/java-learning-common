/**
 * 
 */
package com.cmcciot.platform.flume.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * ServletOutputStream继承类，用于flume日志
 * @author tanyao
 *
 */
public class FlumeServletOutputStream extends ServletOutputStream{

	private ByteArrayOutputStream out;
	
	public FlumeServletOutputStream(ByteArrayOutputStream out) {
		this.out = out;
	}
	
	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(int b) throws IOException {
		this.out.write(b);		
	}

}
