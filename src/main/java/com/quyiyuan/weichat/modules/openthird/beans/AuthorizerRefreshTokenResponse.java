package com.quyiyuan.weichat.modules.openthird.beans;

/**
 * Created by gouchaohui on 2017年11月13日11:49:09.
 */

public class AuthorizerRefreshTokenResponse extends WeChatBaseResponse{
    /**
     * authorizer_access_token
     */
    private String authorizer_access_token;

    /**
     * expires_in
     */
    private String expires_in;

    /**
     * authorizer_refresh_token
     */
    private String authorizer_refresh_token;

    public String getAuthorizer_access_token() {
        return authorizer_access_token;
    }

    public void setAuthorizer_access_token(String authorizer_access_token) {
        this.authorizer_access_token = authorizer_access_token;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }

    public String getAuthorizer_refresh_token() {
        return authorizer_refresh_token;
    }

    public void setAuthorizer_refresh_token(String authorizer_refresh_token) {
        this.authorizer_refresh_token = authorizer_refresh_token;
    }
}
