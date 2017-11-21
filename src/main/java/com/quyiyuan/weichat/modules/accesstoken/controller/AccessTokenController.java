package com.quyiyuan.weichat.modules.accesstoken.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kyee.nextframework.core.base.controller.BaseController;
import com.kyee.nextframework.core.base.resultmodel.ResultModel;
import com.kyee.nextframework.core.base.resultmodel.internal.text.impl.HtmlResultModel;
import com.kyee.nextframework.core.common.utils.CommonUtils.SpringUtil;
import com.kyee.nextframework.core.support.messager.impl.TextMessager;
import com.quyiyuan.weichat.modules.accesstoken.protocol.AccessTokenProtocolOut;
import com.quyiyuan.weichat.modules.accesstoken.service.IAccessTokenService;


@Controller
public class AccessTokenController extends BaseController {
	@Autowired
	@Qualifier("accessTokenService")
	private IAccessTokenService accessTokenService;
		
	@RequestMapping("accessToken")
	public ResultModel getToken(){
		AccessTokenProtocolOut accessTokenProtocolOut = new AccessTokenProtocolOut();
		try {
			accessTokenProtocolOut = accessTokenService.doGetAccessToken(); 
			return new HtmlResultModel(accessTokenProtocolOut.getAccessToken());
		} catch (Exception e) {
			return protocolBuilder.buildError("010001", "获取accessToken失败");
		}    
	}

}
