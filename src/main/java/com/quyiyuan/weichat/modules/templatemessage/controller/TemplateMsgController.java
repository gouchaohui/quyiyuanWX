package com.quyiyuan.weichat.modules.templatemessage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.kyee.nextframework.core.base.controller.BaseController;
import com.kyee.nextframework.core.base.resultmodel.ResultModel;
import com.quyiyuan.weichat.comm.HttpProxy;
import com.quyiyuan.weichat.modules.templatemessage.protocol.TemplateMessageProtocolIn;
import com.quyiyuan.weichat.modules.templatemessage.protocol.TemplateMessageProtocolOut;
import com.quyiyuan.weichat.modules.templatemessage.service.ITemplateMsgService;


@Controller
public class TemplateMsgController extends BaseController {
	@Autowired
	@Qualifier("templateMsgService")
	private ITemplateMsgService templateMsgService;
	
	@RequestMapping(value="/templateMessage")
	public ResultModel doSendTemplateMsg(@Valid TemplateMessageProtocolIn in, HttpServletRequest request){
		TemplateMessageProtocolOut templateMessageProtocolOut = new TemplateMessageProtocolOut();
		Properties ipProperties = new Properties();
		String ipAddress = HttpProxy.getIPAddr(request);
		String ipList;
		String turnOff;
		String[] arr;
		int tryTime = 0;
		try {
			InputStream path = this.getClass().getClassLoader().getResourceAsStream("system-config.properties");
			ipProperties.load(path);
			ipList = ipProperties.getProperty("ipAddeess");
			turnOff = ipProperties.getProperty("turnOff");
			arr = ipList.split("\\|\\|");
		} catch (Exception e) {
			logger.info("Failed to load System-config.properties",e);
			return protocolBuilder.buildError("020004", "加载System-config.properties配置文件失败");
		}
		if (turnOff.equals("1")) {
			logger.info("模板消息功能已关闭");
			return protocolBuilder.buildError("020005", "模板消息功能已关闭");
		} else {
			// 如果配置文件中ipFlag为1则白名单功能生效，如果为0则关闭白名单，所有请求都可推送模板消息
			String ipFlag = ipProperties.getProperty("ipFlag");
			if (ipFlag.equals("1")) {
				logger.info("白名单生效");
				logger.info("发送方的IP地址为:" + ipAddress);		
				for(String i : arr){
					if (ipAddress.matches(i.trim())) {
						logger.info("IP合法");
						templateMessageProtocolOut = templateMsgService.doChooseTemplateMsg(in.getOpenId(),in.getMsgType(),in.getData());
						return protocolBuilder.build(templateMessageProtocolOut,"模板消息推送成功！");
					}
					tryTime = tryTime+1;
				}
				if(tryTime>=arr.length){
					logger.info("发送方Ip非法，不予推送");
					return protocolBuilder.buildError("020006", "发送方Ip非法，不予推送");
				}	
			} else {
				logger.info("白名单关闭，所有请求都可推送模板消息");
				logger.info("发送方的IP地址为:" + ipAddress);
				templateMessageProtocolOut = templateMsgService.doChooseTemplateMsg(in.getOpenId(),in.getMsgType(),in.getData());
				if(templateMessageProtocolOut==null){
					return protocolBuilder.buildError("020007", "入参不正确或网络异常，推送失败");
				}
				return protocolBuilder.build(templateMessageProtocolOut,"模板消息推送成功！");
			}
		}
		return protocolBuilder.buildError("020007", "入参不正确或网络异常，推送失败");
	}
}
