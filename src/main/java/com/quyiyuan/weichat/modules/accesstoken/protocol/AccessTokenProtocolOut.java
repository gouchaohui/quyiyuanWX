package com.quyiyuan.weichat.modules.accesstoken.protocol;

import com.kyee.nextframework.core.extension.protocol.ProtocolOut;

public class AccessTokenProtocolOut extends ProtocolOut {
	private String accessToken;
	public AccessTokenProtocolOut(){	
	}
	public AccessTokenProtocolOut(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
}
