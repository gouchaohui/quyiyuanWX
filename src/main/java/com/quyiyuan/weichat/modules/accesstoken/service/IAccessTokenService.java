package com.quyiyuan.weichat.modules.accesstoken.service;

import com.kyee.nextframework.core.base.service.IBaseService;
import com.quyiyuan.weichat.comm.service.IWxBaseService;
import com.quyiyuan.weichat.modules.accesstoken.domain.WeiChatToken;
import com.quyiyuan.weichat.modules.accesstoken.protocol.AccessTokenProtocolOut;

public interface IAccessTokenService extends IWxBaseService<WeiChatToken, String> {
	AccessTokenProtocolOut doGetAccessToken();
	Boolean checkToken(String accessToken,long getTime,long now);
	WeiChatToken getTokenFromDatabase(String appId);
	WeiChatToken getTokenFromWX(String appId ,String appSecret);

}
