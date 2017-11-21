package com.quyiyuan.weichat.modules.openthird.enums;

/**
 * Created by hanchonglin on 2017/9/18.
 */
public enum MsgTypeEnum {
    /**
     * 返回消息类型：文本
     */
    RESP_MESSAGE_TYPE_TEXT("text"),

    /**
     * 返回消息类型：音乐
     */
    RESP_MESSAGE_TYPE_MUSIC("music"),

    /**
     * 返回消息类型：图文
     */
    RESP_MESSAGE_TYPE_NEWS("news"),

    /**
     * 请求消息类型：文本
     */
    REQ_MESSAGE_TYPE_TEXT("text"),

    /**
     * 请求消息类型：图片
     */
    REQ_MESSAGE_TYPE_IMAGE("image"),

    /**
     * 请求消息类型：链接
     */
    REQ_MESSAGE_TYPE_LINK("link"),

    /**
     * 请求消息类型：地理位置
     */
    REQ_MESSAGE_TYPE_LOCATION("location"),

    /**
     * 请求消息类型：音频
     */
    REQ_MESSAGE_TYPE_VOICE("voice"),

    /**
     * 请求消息类型：推送
     */
    REQ_MESSAGE_TYPE_EVENT("event"),

    /**
     * 事件类型：subscribe(订阅)
     */
    EVENT_TYPE_SUBSCRIBE("subscribe"),

    /**
     * 事件类型：unsubscribe(取消订阅)
     */
    EVENT_TYPE_UNSUBSCRIBE("unsubscribe"),

    /**
     * 事件类型：CLICK(自定义菜单点击事件)
     */
    EVENT_TYPE_CLICK("CLICK");


    private String code;


    MsgTypeEnum(String code) {
        this.code = code;
    }

    /**
     * name -> code
     */
    public String getCode() {
        return this.code;
    }
}
