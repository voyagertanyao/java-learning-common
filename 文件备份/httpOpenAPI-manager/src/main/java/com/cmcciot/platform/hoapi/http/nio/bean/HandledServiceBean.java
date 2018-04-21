package com.cmcciot.platform.hoapi.http.nio.bean;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * osh正在处理中的接口
 * @author ldd
 *
 */
public class HandledServiceBean {
	
	private String msgType;//接口名称

	private int maxAcount;  //允许接口正在处理中的最大数量
	
	private AtomicInteger  count;//正在处理中的接口数量

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public int getMaxAcount() {
		return maxAcount;
	}

	public void setMaxAcount(int maxAcount) {
		this.maxAcount = maxAcount;
	}

	public AtomicInteger getCount() {
		return count;
	}

	public void setCount(AtomicInteger count) {
		this.count = count;
	}
	
	public static void main(String[] args) {
		HandledServiceBean hsb = new HandledServiceBean();
		hsb.setCount(new AtomicInteger(0));
		System.out.println(hsb.getMaxAcount());
		hsb.getCount().addAndGet(1);
		System.out.println(hsb.getCount());
	}
}
