package com.quyiyuan.weichat.modules.openthird.beans;

/**
 * Created by gouchaohui on 2017/9/13.
 */

public class AuthInfoResponse extends WeChatBaseResponse {

    /**
     * authorization_info
     */
    AuthInfoPO authorization_info;

    public AuthInfoPO getAuthorization_info() {
        return authorization_info;
    }

    public void setAuthorization_info(AuthInfoPO authorization_info) {
        this.authorization_info = authorization_info;
    }
}
