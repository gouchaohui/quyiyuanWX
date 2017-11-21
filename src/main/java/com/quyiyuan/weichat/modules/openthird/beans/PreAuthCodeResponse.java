package com.quyiyuan.weichat.modules.openthird.beans;

/**
 * Created by gouchaohui on 2017/11/08.
 */

public class PreAuthCodeResponse extends WeChatBaseResponse {

    /**
     * PreAuthCode
     */
    private String pre_auth_code;

    /**
     * expiresIn
     */
    private String expires_in;

    public String getPre_auth_code() {
        return pre_auth_code;
    }

    public void setPre_auth_code(String pre_auth_code) {
        this.pre_auth_code = pre_auth_code;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }
}
