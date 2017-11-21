package com.quyiyuan.weichat.modules.baseservice.beans;

/**
 * Created by czh on 2017/8/15.
 */
public class MiniProgram extends CommProperty{
    private String appid;
    private String pagepath;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getPagepath() {
        return pagepath;
    }

    public void setPagepath(String pagepath) {
        this.pagepath = pagepath;
    }
}