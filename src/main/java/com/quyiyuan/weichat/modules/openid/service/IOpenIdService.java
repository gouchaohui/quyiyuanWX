package com.quyiyuan.weichat.modules.openid.service;

import java.io.Serializable;

import com.kyee.nextframework.core.base.domain.internal.EmptyDomain;
import com.quyiyuan.weichat.comm.service.IWxBaseService;

public interface IOpenIdService extends IWxBaseService<EmptyDomain, Serializable> {
	String getOpenId(String code);

}
