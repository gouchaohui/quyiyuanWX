package com.quyiyuan.weichat.modules.openthird.service;

/**
 * 统一给微信发送请求
 * @author gouchaohui
 * @create 2017-11-09 9:42
 **/
public interface ISendRequestToWXService {
    String postSend(String url,String param);
}
