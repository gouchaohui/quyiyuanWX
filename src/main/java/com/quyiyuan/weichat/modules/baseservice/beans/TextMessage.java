package com.quyiyuan.weichat.modules.baseservice.beans;

import com.quyiyuan.weichat.comm.MessageUtil;

public class TextMessage extends BaseMessage {
	/*
	 * 回复的消息内容
	 */
	private String Content;

	public String getContent() {
		return Content;
	}

	public void setContent(String content) {
		Content = content;
	}
	
	public TextMessage(){
		
	}
	
	public TextMessage(String fromUserName, String toUserName, long createTime, String content, int funcFlag){
		this.FromUserName = fromUserName;
		this.ToUserName = toUserName;
		this.CreateTime = createTime;
		this.Content = content;
		this.FuncFlag = funcFlag;
		this.MsgType = MessageUtil.RESP_MESSAGE_TYPE_TEXT;
	}

}
