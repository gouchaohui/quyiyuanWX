package com.quyiyuan.weichat.modules.accesstoken.dao;

import com.kyee.nextframework.core.base.dao.internal.IJdbcBaseDao;
import com.quyiyuan.weichat.modules.accesstoken.domain.WeiChatToken;


public interface IAccessTokenDao extends IJdbcBaseDao<WeiChatToken, String> {
	
	WeiChatToken findTokenByAppId(String appId);
	void updateTokenByAppId(String appId , long getTime , String accessToken);
	void updateFlagByAppId(String appId);

}
