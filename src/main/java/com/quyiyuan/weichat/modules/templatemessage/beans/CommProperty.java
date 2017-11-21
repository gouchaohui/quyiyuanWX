package com.quyiyuan.weichat.modules.templatemessage.beans;

public class CommProperty {
	private String touser;
	private String template_id;
	private String url;
	private Data data;
	public CommProperty(){
	}
	public CommProperty(String touser, String template_id, String url){
		this.touser = touser;
		this.template_id =template_id;
		this.url = url;
	}
	public String getTouser(){
		return touser;
	}
	public void setTouser(String touser){
		this.touser = touser;
	}
	public String getTemplateId(){
		return template_id;
	}
	public void setTemplateId(String template_id){
		this.template_id = template_id;
	}
	public String getUrl(){
		return url;
	}
	public void setUrl(String url){
		this.url = url;
	}
	public Data getData(){
		return data;
	}
	public void setData(Data data){
		this.data = data;
	}

}
