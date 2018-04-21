package com.cmcciot.platform.hoapi.http.nio.separate.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)  
@XmlType(propOrder = { "propertyList", "name", "id" })  
public class MappingBean {

	@XmlElements(value = { @XmlElement(name = "property", type = PropertyBean.class) })
	private List<PropertyBean> propertyList;
	
	@XmlAttribute(name="name")
	private String name;
	
	@XmlAttribute(name="id")
	private String id;

	public List<PropertyBean> getPropertyList() {
		return propertyList;
	}

	public void setPropertyList(List<PropertyBean> propertyList) {
		this.propertyList = propertyList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "MappingBean [propertyList=" + propertyList + ", name=" + name
				+ ", id=" + id + "]";
	}
	
}

