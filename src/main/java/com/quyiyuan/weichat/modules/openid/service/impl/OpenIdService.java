package com.quyiyuan.weichat.modules.openid.service.impl;

import java.io.IOException;
import java.io.Serializable;

import org.springframework.stereotype.Service;

import com.kyee.nextframework.core.base.domain.internal.EmptyDomain;
import com.kyee.nextframework.core.common.utils.CommonUtils.JsonUtil;
import com.quyiyuan.weichat.comm.HttpProxy;
import com.quyiyuan.weichat.comm.service.impl.WxBaseService;
import com.quyiyuan.weichat.modules.openid.service.IOpenIdService;

@Service("openIdService")
public class OpenIdService extends WxBaseService<EmptyDomain, Serializable> implements IOpenIdService {
	/**
	 * 从微信获取openId
	 * @param code 由微信传来的加密字符串用于校验
	 * @return String
	 */
	@Override
	public String getOpenId(String code) {
		String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId + "&secret=" + appSecret
				+ "&code=" + code + "&grant_type=authorization_code";
		String openId = "";
		try {
			String resp = HttpProxy.httpPost(url, "");
			logger.info("从微信返回的获取OpenID信息为： "+resp);
			openId = JsonUtil.getValue(resp, "openid", String.class);
			if (openId==null) {
				throw new IOException();
			}
			return openId;
		}  catch (IOException e) {
			logger.error("获取openId时code有误");
			openId = "error";
			return openId;
		}
	}

}
