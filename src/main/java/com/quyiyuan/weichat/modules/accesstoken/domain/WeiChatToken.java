package com.quyiyuan.weichat.modules.accesstoken.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.kyee.nextframework.core.base.domain.BaseDomain;

@Entity
@Table(name="weixin_accessToken")

public class WeiChatToken extends BaseDomain {
    private String appId;
    private String appSecret;
    private String accessToken;
    private long getTime;
    
    public WeiChatToken(){
    	
    }
    
    public WeiChatToken(String appId, String appSecret, String accessToken, long getTime){
    	this.appId = appId;
    	this.appSecret = appSecret;
    	this.accessToken = accessToken;
    	this.getTime = getTime;	
    }
    
    @Column(name="APP_ID")
    public String getAppId(){
    	return appId;
    }
    
    @Column(name="APP_SECRET")
    public String getAppSecret(){
    	return appSecret;
    }
   public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}
    
    @Column(name="ACCESS_TOKEN")
    public String getAccessToken(){
    	return accessToken;
    }
    public void setAccessToken(String accessToken){
    	this.accessToken = accessToken;
    }
    
    @Column(name="GET_TIME")
    public long getGetTime(){
    	return getTime;
    }
    public void setGetTime(long getTime){
    	this.getTime = getTime;
    }
}
