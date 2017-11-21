package com.quyiyuan.weichat.modules.openthird.beans;

/**
 * Created by gouchaohui on 2017年11月13日11:49:09.
 */

public class QRcodeTicketResponse extends WeChatBaseResponse{
    private String ticket;
    private String expire_seconds;
    private String url;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getExpire_seconds() {
        return expire_seconds;
    }

    public void setExpire_seconds(String expire_seconds) {
        this.expire_seconds = expire_seconds;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
