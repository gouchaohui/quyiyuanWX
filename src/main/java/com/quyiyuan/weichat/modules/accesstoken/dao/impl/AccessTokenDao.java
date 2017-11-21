package com.quyiyuan.weichat.modules.accesstoken.dao.impl;
import org.springframework.stereotype.Repository;

import com.kyee.nextframework.core.base.dao.helper.NamedParamsBuilder;
import com.kyee.nextframework.core.base.dao.internal.impl.JdbcBaseDao;
import com.quyiyuan.weichat.modules.accesstoken.dao.IAccessTokenDao;
import com.quyiyuan.weichat.modules.accesstoken.domain.WeiChatToken;


@Repository("accessTokenDao")
public class AccessTokenDao extends JdbcBaseDao<WeiChatToken, String>  implements IAccessTokenDao{
		
	@Override 
	public WeiChatToken findTokenByAppId(String appId){
		NamedParamsBuilder paramBuilder = NamedParamsBuilder.getInstance();
		paramBuilder.put("APP_ID",appId);
		String sqlStr = "SELECT APP_SECRET, ACCESS_TOKEN,GET_TIME,FLAG FROM WEIXIN_ACCESSTOKEN where APP_ID=:APP_ID";	
		WeiChatToken weiChatToken = findOneByNamedParamsSqlForObjectValue(
				sqlStr.toString(), paramBuilder, WeiChatToken.class);
		return weiChatToken;
	}

	@Override
	public void updateTokenByAppId(String appId , long getTime ,String accessToken){
		NamedParamsBuilder paramBuilder = NamedParamsBuilder.getInstance();
		paramBuilder.put("APP_ID",appId);
		paramBuilder.put("GET_TIME",getTime);
		paramBuilder.put("ACCESS_TOKEN",accessToken);
		String sqlStr = 
				"UPDATE WEIXIN_ACCESSTOKEN  SET ACCESS_TOKEN=:ACCESS_TOKEN ,GET_TIME=:GET_TIME,FLAG='0' where APP_ID=:APP_ID";	
		 updateByNamedParamsSql(
				sqlStr.toString(), paramBuilder);
	}

	@Override
	public void updateFlagByAppId(String appId) {
		NamedParamsBuilder paramBuilder = NamedParamsBuilder.getInstance();
		paramBuilder.put("APP_ID",appId);
		String sqlStr = 
				"UPDATE WEIXIN_ACCESSTOKEN  SET FLAG='1' where APP_ID=:APP_ID";	
		 updateByNamedParamsSql(
				sqlStr.toString(), paramBuilder);
		
	}
}
