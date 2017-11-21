package com.quyiyuan.weichat.modules.openthird.beans;

/**
 * Created by gouchaohui on 2017/11/7.
 */
public class ComponentAccessTokenResponse extends WeChatBaseResponse{

    /**
     * component_access_token
     */
    private String component_access_token;

    /**
     * expires_in
     */
    private String expires_in;

    public String getComponent_access_token() {
        return component_access_token;
    }

    public void setComponent_access_token(String component_access_token) {
        this.component_access_token = component_access_token;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }
}
