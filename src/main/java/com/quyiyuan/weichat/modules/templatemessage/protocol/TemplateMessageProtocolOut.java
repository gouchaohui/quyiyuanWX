package com.quyiyuan.weichat.modules.templatemessage.protocol;

import com.kyee.nextframework.core.extension.protocol.ProtocolOut;

public class TemplateMessageProtocolOut extends ProtocolOut {
	private String respMsg;

	public TemplateMessageProtocolOut(){
	}

	public TemplateMessageProtocolOut(String respMsg) {
		this.respMsg = respMsg;
	}

	public String getRespMsg() {
		return respMsg;
	}

	public void setRespMsg(String respMsg) {
		this.respMsg = respMsg;
	}
	

}
