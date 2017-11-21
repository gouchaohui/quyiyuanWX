package com.quyiyuan.weichat.modules.openthird.beans;

/**
 * Created by gouchaohui on 2017/11/7.
 */
public enum ErrCodeEnum {
    /**
     * 错误
     */
    BUSY("-1"),

    /**
     * 正常
     */
    SUCCESS("0"),

    ;

    private String code;


    ErrCodeEnum(String code) {
        this.code = code;
    }

    /**
     * name -> code
     */
    public String getCode() {
        return this.code;
    }
}
