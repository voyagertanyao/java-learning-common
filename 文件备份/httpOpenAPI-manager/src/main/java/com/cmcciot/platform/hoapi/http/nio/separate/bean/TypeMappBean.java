package com.cmcciot.platform.hoapi.http.nio.separate.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)  
@XmlRootElement(name = "Mappings")
@XmlType(propOrder = {"mappingList"})
public class TypeMappBean {

	@XmlElements(value = { @XmlElement(name = "Mapping", type = MappingBean.class) })
	private List<MappingBean> mappingList;

	public List<MappingBean> getMappingList() {
		return mappingList;
	}

	public void setMappingList(List<MappingBean> mappingList) {
		this.mappingList = mappingList;
	}

	@Override
	public String toString() {
		return "TypeMappBean [mappingList=" + mappingList + "]";
	}

}
