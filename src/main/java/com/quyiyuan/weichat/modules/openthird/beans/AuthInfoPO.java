package com.quyiyuan.weichat.modules.openthird.beans;


import java.util.List;

/**
 * Created by gouchaohui on 2017/11/9.
 */

public class AuthInfoPO {
    /**
     * authorizer_appid
     */
    private String authorizer_appid;

    /**
     *authorizer_access_token
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

    List<FuncscopeCategory> func_info;

    public String getAuthorizer_appid() {
        return authorizer_appid;
    }

    public void setAuthorizer_appid(String authorizer_appid) {
        this.authorizer_appid = authorizer_appid;
    }

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

    public List<FuncscopeCategory> getFunc_info() {
        return func_info;
    }

    public void setFunc_info(List<FuncscopeCategory> func_info) {
        this.func_info = func_info;
    }

    private static class FuncscopeCategory {
        /**
         * id
         */
        private Integer id;
    }
}
