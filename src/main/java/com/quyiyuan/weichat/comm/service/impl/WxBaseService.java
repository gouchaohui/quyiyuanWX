package com.quyiyuan.weichat.comm.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.kyee.nextframework.core.base.domain.BaseDomain;
import com.kyee.nextframework.core.base.service.impl.BaseService;
import com.quyiyuan.weichat.comm.service.IWxBaseService;
import com.quyiyuan.weichat.modules.accesstoken.dao.IAccessTokenDao;
/**
 * “微信后台”基础服务基类
 */
public class WxBaseService<T extends BaseDomain, PK extends Serializable> extends BaseService<T, PK>
		implements IWxBaseService<T, PK> {
	@Autowired
	@Qualifier("accessTokenDao")
	protected IAccessTokenDao accessTokenDao;
	
	protected String appId;
	protected String appSecret;
	private Properties properties;
	
	@PostConstruct
	public void init(){
		properties=new Properties();
		InputStream path = this.getClass().getClassLoader().getResourceAsStream("system-config.properties");
		try {
			properties.load(path);
			appId = properties.getProperty("app_id");
			appSecret = accessTokenDao.findTokenByAppId(appId).getAppSecret();
		} catch (IOException e) {
			logger.error("加载system-config配置文件出错",e);
		}
	}
}
