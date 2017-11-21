package com.quyiyuan.weichat.modules.baseservice.beans;

public class BaseMessage {
	/*
	 * 接收方账号(收到的openId)
	 */
	protected String ToUserName;
	/*
	 * 开发者微信号
	 */
	protected String FromUserName;
	/*
	 * 消息创建时间
	 */
	protected long CreateTime;
	/*
	 * 消息类型(text/music/news)
	 */
	protected String MsgType;
	/*
	 * 位0x0001被标识时，星标刚收到的消息
	 */
	protected int FuncFlag;
	public String getToUserName() {
		return ToUserName;
	}
	public void setToUserName(String toUserName) {
		ToUserName = toUserName;
	}
	public String getFromUserName() {
		return FromUserName;
	}
	public void setFromUserName(String fromUserName) {
		FromUserName = fromUserName;
	}
	public long getCreateTime() {
		return CreateTime;
	}
	public void setCreateTime(long createTime) {
		CreateTime = createTime;
	}
	public String getMsgType() {
		return MsgType;
	}
	public void setMsgType(String msgType) {
		MsgType = msgType;
	}
	public int getFuncFlag() {
		return FuncFlag;
	}
	public void setFuncFlag(int funcFlag) {
		FuncFlag = funcFlag;
	}
}
