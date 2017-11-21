package com.quyiyuan.weichat.modules.openthird.controller;

import com.quyiyuan.weichat.comm.controller.WxBaseController;
import com.quyiyuan.weichat.modules.openthird.aes.AesException;
import com.quyiyuan.weichat.modules.openthird.service.IOpenThirdService;
import com.quyiyuan.weichat.modules.openthird.util.MessageUtil;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Controller
@RequestMapping("/openThirdwx")
public class OpenThirdController extends WxBaseController {

	@Autowired
	@Qualifier("openThirdService")
	private IOpenThirdService openThirdService;

	@RequestMapping(value = "/event/authorize",method = RequestMethod.POST)
	public void acceptAuthorizeEvent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//获取微信每10分钟推送的ticket
		String mes=openThirdService.updateAuthorizeEvent(request, response);
		MessageUtil.output(response,mes);
	}
	/**
	 * 获取预授权码
	 * @return
	 */
	@RequestMapping(value = "/get/preAuthCode",method = RequestMethod.GET)
	public String getPreAuthCode() {
		return openThirdService.doGetPreAuthCode();
	}

	@RequestMapping(value = "/goAuthor",method = RequestMethod.GET)
	public void goAuthor(HttpServletRequest request, HttpServletResponse response) throws IOException, AesException, DocumentException {
		openThirdService.doGoAuthor(request,response);
	}

	@RequestMapping(value = "/authorCallback",method = RequestMethod.GET)
	public void authorCallback(HttpServletRequest request, HttpServletResponse response) throws IOException, AesException, DocumentException {
		openThirdService.execAuthorCallback(request,response);
	}

	@RequestMapping(value = "/{appid}/callback", method = RequestMethod.POST)
	public void messageEventCallback(@PathVariable("appid") String appid, HttpServletRequest request, HttpServletResponse response) throws Exception {
		openThirdService.doMessageEventCallback(appid,request, response);
	}

	@RequestMapping(value = "/createCode")
	public void createCode(HttpServletRequest request, HttpServletResponse response) throws Exception {
		openThirdService.docreateCode(request, response);
	}
}


