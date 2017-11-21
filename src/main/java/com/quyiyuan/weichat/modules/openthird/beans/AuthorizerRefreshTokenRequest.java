package com.quyiyuan.weichat.modules.openthird.beans;

/**
 * Created by gouchaohui on 2017年11月13日11:49:09.
 */

public class AuthorizerRefreshTokenRequest {
    /**
     * component_appid
     */
    private String component_appid;

    /**
     * authorizer_appid
     */
    private String authorizer_appid;

    /**
     * authorizer_refresh_token
     */
    private String authorizer_refresh_token;

    public String getComponent_appid() {
        return component_appid;
    }

    public void setComponent_appid(String component_appid) {
        this.component_appid = component_appid;
    }

    public String getAuthorizer_appid() {
        return authorizer_appid;
    }

    public void setAuthorizer_appid(String authorizer_appid) {
        this.authorizer_appid = authorizer_appid;
    }

    public String getAuthorizer_refresh_token() {
        return authorizer_refresh_token;
    }

    public void setAuthorizer_refresh_token(String authorizer_refresh_token) {
        this.authorizer_refresh_token = authorizer_refresh_token;
    }
}
