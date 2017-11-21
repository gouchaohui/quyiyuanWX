package com.quyiyuan.weichat.modules.openthird.beans;


/**
 * Created by gouchaohui on 2017/11/9.
 */
public class AuthorizerTokenRequest {
    /**
     * component_appid
     */
    private String component_appid;

    /**
     * authorization_code
     */
    private String authorization_code;

    public String getComponent_appid() {
        return component_appid;
    }

    public void setComponent_appid(String component_appid) {
        this.component_appid = component_appid;
    }

    public String getAuthorization_code() {
        return authorization_code;
    }

    public void setAuthorization_code(String authorization_code) {
        this.authorization_code = authorization_code;
    }
}
