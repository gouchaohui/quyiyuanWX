package com.quyiyuan.weichat.modules.templatemessage.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.kyee.nextframework.core.base.domain.BaseDomain;


@Entity
@Table(name="weixin_templateId")
public class TemplateId extends BaseDomain {
	private String templateName;
	private String templateId;
	
	public TemplateId(){
	}
	
	public TemplateId(String templateName, String templateID){
		this.templateName = templateName;
		this.templateId = templateID;
	}
	
	@Column(name="TEMPLATE_NAME")
	public String getTemplateName(){
		return templateName;
	}
	public void setTemplateName(String templateName){
		this.templateName = templateName;
	}
	
	@Column(name="TEMPLATE_ID")
	public String getTemplateId(){
		return templateId;
	}
	public void setTemplateId(String templateId){
		this.templateId = templateId;
	}

}
