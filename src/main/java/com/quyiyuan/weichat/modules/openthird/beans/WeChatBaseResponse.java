package com.quyiyuan.weichat.modules.openthird.beans;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by gouchaohui on 2017/11/7.
 */

public class WeChatBaseResponse {

    protected Logger logger = Logger.getLogger(getClass());
    /**
     * 错误码
     */
    private String errcode;

    /**
     * 错误信息
     */
    private String errmsg;

    public String getErrcode() {
        return errcode;
    }

    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    /**
     * 此response是否有效
     *
     * @return
     */
    public boolean isValid() {
        logger.info("errCode:"+errcode+", errMessage:"+errmsg);
        return errcode == null || StringUtils.equals(errcode, ErrCodeEnum.SUCCESS.getCode());
    }
}
