package com.quyiyuan.weichat.comm;

import com.kyee.nextframework.core.common.utils.base.http.ssl_strategy.BaseSSLStrategy;

public class WeiChatSSL extends BaseSSLStrategy{

	@Override
	public String decide(String url, Object params) {
		// TODO Auto-generated method stub
		return SKIP;
	}
}
