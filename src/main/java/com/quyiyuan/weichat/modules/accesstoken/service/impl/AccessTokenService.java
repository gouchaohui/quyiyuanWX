package com.quyiyuan.weichat.modules.accesstoken.service.impl;

import java.io.IOException;
import org.springframework.stereotype.Service;

import com.kyee.nextframework.core.common.utils.CommonUtils.JsonUtil;
import com.kyee.nextframework.core.support.cache.normal.impl.bean.CacheNode;
import com.quyiyuan.weichat.comm.HttpProxy;
import com.quyiyuan.weichat.comm.service.impl.WxBaseService;
import com.quyiyuan.weichat.modules.accesstoken.domain.WeiChatToken;
import com.quyiyuan.weichat.modules.accesstoken.protocol.AccessTokenProtocolOut;
import com.quyiyuan.weichat.modules.accesstoken.service.IAccessTokenService;

import net.sf.ehcache.config.CacheConfiguration;

@Service("accessTokenService")
public class AccessTokenService extends WxBaseService<WeiChatToken, String> implements IAccessTokenService{
	private static final long EXPIRES_IN = 7200L;
	private static final String CACHE_NAME = "accessTokenCash";
	private WeiChatToken weiChatToken ;
	/**
	 * 获取accessToken主方法，依次从缓存、数据库、微信接口中获取accessToken
	 * @return AccessTokenProtocolOut
	 */
	@Override
	public AccessTokenProtocolOut doGetAccessToken() {
		AccessTokenProtocolOut accessTokenProtocolOut = new AccessTokenProtocolOut();
		String accessToken= "";
		long getTime = 0;
		// 缓存不存在时，配置缓存服务,并从数据库中读AccessToken，如果Token有效返回给用户,并且将数据写入缓存。
		if (!localCacheService.containsCache(CACHE_NAME)) {
			logger.info("正在建立服务器缓存");
			CacheConfiguration cfg = new CacheConfiguration();
			cfg.setName(CACHE_NAME);
			//服务器缓存有效期为1小时
			cfg.setTimeToLiveSeconds(60*60);
			cfg.setMaxElementsInMemory(20);
			localCacheService.addCache(cfg);
			synchronized (appId) {
			logger.info("正在从数据库中读取Token");
			    //读库前先将库里的FLAG字段置为1，防止两台服务器同时请求Token导致失效问题
			    accessTokenDao.updateFlagByAppId(appId);
				weiChatToken = getTokenFromDatabase(appId);
				if (weiChatToken != null) {
					logger.info("数据库中Token有效，返回给用户");
					accessToken = weiChatToken.getAccessToken();
					accessTokenProtocolOut.setAccessToken(accessToken);
					return accessTokenProtocolOut;
				} else {
					logger.info("数据库Token失效，正在从微信接口获取Token");
					weiChatToken = getTokenFromWX(appId, appSecret);
					if (weiChatToken != null) {
						logger.info("从微信获取的Token有效，返回给用户");
						accessToken = weiChatToken.getAccessToken();
						accessTokenProtocolOut.setAccessToken(accessToken);
						return accessTokenProtocolOut;
					}
					return null;
				}
			}
		} else {
			logger.info("正在从服务器缓存中读取Token");
			long now = System.currentTimeMillis();
			try {
				accessToken = (String) localCacheService.getNode(CACHE_NAME, "accessToken").getObjectValue();
				getTime = (long) localCacheService.getNode(CACHE_NAME, "getTime").getObjectValue();
			} catch (Exception e) {
				logger.error("服务器缓存节点失效,正在重新建立节点");
			}
			if (checkToken(accessToken, getTime, now)) {
				logger.info("服务器Token缓存有效");
				weiChatToken.setAccessToken(accessToken);
				weiChatToken.setGetTime(getTime);
				accessTokenProtocolOut.setAccessToken(accessToken);
				return accessTokenProtocolOut;
			} else{
				synchronized (appId) {
					logger.info("服务器缓存失效，现在从数据库中获取Token");
					accessTokenDao.updateFlagByAppId(appId);
					weiChatToken = getTokenFromDatabase(appId);
					if (weiChatToken != null) {
						logger.info("数据库中Token有效，返回给用户");
						accessToken = weiChatToken.getAccessToken();
						accessTokenProtocolOut.setAccessToken(accessToken);
						return accessTokenProtocolOut;
					} else {
						logger.info("数据库Token失效，正在从微信接口获取Token");
						weiChatToken = getTokenFromWX(appId, appSecret);
						if (weiChatToken != null) {
							logger.info("从微信获取的Token有效，返回给用户");
							accessToken = weiChatToken.getAccessToken();
							accessTokenProtocolOut.setAccessToken(accessToken);
							return accessTokenProtocolOut;
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 检测获取的accessToken是否有效，accessToken的有效期默认为2小时
	 * @param accessToken
	 * @param getTime 数据库中存accessToken的时间
	 * @param now 系统时间
	 * @return Boolean
	 */
	@Override
	public Boolean checkToken(String accessToken,long getTime,long now){
		if((accessToken == null)||(accessToken.equals(""))||(now - getTime) / 1000 >= (EXPIRES_IN - 2)){
			return false;
		}
		else{
			return true;
		}		
	}

	/**
	 * 当服务器缓存失效时，从数据库中读accessToken并检验有效性，若失效返回null
	 * @param appId
	 * @return WeiChatToken
	 */
	@Override
	public WeiChatToken getTokenFromDatabase(String appId) {
		weiChatToken = accessTokenDao.findTokenByAppId(appId);
	    long getTime = weiChatToken.getGetTime();
		String accessToken = weiChatToken.getAccessToken();
		long now = System.currentTimeMillis();
		if(checkToken(accessToken, getTime, now)){
			localCacheService.putOrUpdateNode(CACHE_NAME, new CacheNode("accessToken", accessToken));
			localCacheService.putOrUpdateNode(CACHE_NAME, new CacheNode("getTime", getTime));
			return weiChatToken;
		}
		return null;
	}

	/**
	 * 从微信接口获取Token并更新数据库和缓存
	 * @param appId
	 * @param appSecret
	 * @return WeiChatToken
	 */
	@Override
	public WeiChatToken getTokenFromWX(String appId ,String appSecret) {
		String accessToken = "";
		long getTime = 0;
		WeiChatToken weiChatToken = new WeiChatToken();
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
				+ appId + "&secret=" + appSecret;
		logger.info("Url:"+url);
		try {
			String jsonStr = HttpProxy.httpPost(url, "");
		    accessToken = JsonUtil.getValue(jsonStr, "access_token", String.class);	
			getTime = System.currentTimeMillis();
		} catch (IOException e) {
			logger.error("请求Token失败",e);
			return null;
		}
		weiChatToken.setAccessToken(accessToken);
		weiChatToken.setGetTime(getTime);
		localCacheService.putOrUpdateNode(CACHE_NAME, new CacheNode("accessToken", accessToken));
		localCacheService.putOrUpdateNode(CACHE_NAME, new CacheNode("getTime", getTime));
		accessTokenDao.updateTokenByAppId(appId, getTime, accessToken);
		return weiChatToken;	
	}
}

