package com.quyiyuan.weichat.modules.templatemessage.protocol;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import com.kyee.nextframework.core.extension.protocol.ProtocolIn;

public class TemplateMessageProtocolIn extends ProtocolIn {
	@NotNull(message="020001:openId不能为空")
	private String openId;
	@NotNull(message="020002:msgType不能为空")
	private String msgType;
	@NotNull(message="020003:data不能为空")
	private String data;
	
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}


}
