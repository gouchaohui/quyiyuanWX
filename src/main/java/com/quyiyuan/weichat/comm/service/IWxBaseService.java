package com.quyiyuan.weichat.comm.service;

import java.io.Serializable;

import com.kyee.nextframework.core.base.domain.BaseDomain;
import com.kyee.nextframework.core.base.service.IBaseService;
/**
 * “微信后台”基础服务接口
 */
public interface IWxBaseService<T extends BaseDomain, PK extends Serializable> extends IBaseService<T, PK> {

}
