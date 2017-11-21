package com.quyiyuan.weichat.modules.openthird.service;

import com.kyee.nextframework.core.base.domain.internal.EmptyDomain;
import com.quyiyuan.weichat.comm.service.IWxBaseService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface IOpenThirdService extends IWxBaseService<EmptyDomain, Serializable> {
    /**
     * 获取推送的ticket
     * @param request
     * @param response
     * @throws Exception
     */
    String updateAuthorizeEvent(HttpServletRequest request, HttpServletResponse response) throws Exception;
    /**
     * 获取第三方平台component_access_token
     * @return
     */
    String updateOrGetComponentToken();

    /**
     * 获取预授权码
     * @return
     */
    String doGetPreAuthCode();
    /**
     * 一键授权功能
     * @param request
     * @param response
     */
    void doGoAuthor(HttpServletRequest request, HttpServletResponse response);
    /**
     * 授权后回调方法
     * @param request
     * @param response
     */
    void execAuthorCallback(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取（刷新）授权公众号或小程序的接口调用凭据（令牌）
     * 如果数据库有且没过期，从数据库拿，过期则刷新AuthorizerAccessToken
     * 如果没找报出该公众号还没有授权
     */
    String doGetAuthorizerAccessTokenSingle(String authorizerAppId);

    List<Map<String, Object>> doGetAuthorizerAccessTokenAll();
    /**
     * 用于第三方消息事件回调
     * @param request
     * @param response
     * @return
     */
    void doMessageEventCallback(String appid,HttpServletRequest request, HttpServletResponse response)throws Exception;

    /**
     * 通过appid创建带参数二维码
     * @param request
     * @param response
     */
    void docreateCode(HttpServletRequest request, HttpServletResponse response);
}
