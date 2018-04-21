package com.cmcciot.platform.hoapi.http.nio.separate.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


@XmlAccessorType(XmlAccessType.FIELD)  
@XmlType(propOrder = { "value", "maxtpm","key"})  
public class PropertyBean {

	@XmlValue
	private String value;
	
	@XmlAttribute(name="maxp")
	private String maxtpm;
	
	@XmlAttribute(name="name")
	private String key;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMaxtpm() {
		return maxtpm;
	}

	public void setMaxtpm(String maxtpm) {
		this.maxtpm = maxtpm;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
