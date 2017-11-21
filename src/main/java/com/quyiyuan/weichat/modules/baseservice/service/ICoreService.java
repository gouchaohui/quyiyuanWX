package com.quyiyuan.weichat.modules.baseservice.service;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;

import com.kyee.nextframework.core.base.domain.internal.EmptyDomain;
import com.quyiyuan.weichat.comm.service.IWxBaseService;

public interface ICoreService extends IWxBaseService<EmptyDomain, Serializable> {
	boolean checkSignature(String signature, String timestamp, String nonce, String Token);
	String processRequest(HttpServletRequest request);
	String doCreateMenu(Boolean testFlag);
	
	String doGetJsApiTicket() throws ClientProtocolException, IOException;
	String getNonceStr();
	String getTimeStamp();
	String getSignatureInfo(String noncestr, String jsapiTicket, String timestamp, String url);
}