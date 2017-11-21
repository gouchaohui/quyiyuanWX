package com.quyiyuan.weichat.modules.templatemessage.beans;

public class BaseValue {
	private String value;
	private String color;
	public BaseValue(){
	}
	public BaseValue(String value){
		this.value =value;
		color = "#333333";
	}
	public String getValue(){
		return value;
	}
	public void setValue(String value){
		this.value =value;
	}
	public String getColor(){
		return color;
	}
	public void setColor(String color){
		this.color =color;
	}
	

}
