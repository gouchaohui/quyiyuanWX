package com.quyiyuan.weichat.modules.openthird.service.impl;

import com.quyiyuan.weichat.comm.HttpProxy;
import com.quyiyuan.weichat.modules.openthird.service.ISendRequestToWXService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;


/**
 * 发送微信请求统一入口
 *
 * @author gouchaohui
 * @create 2017-11-09 9:43
 **/
@Service("sendRequestToWXService")
public class SendRequestToWXService implements ISendRequestToWXService {
    protected Logger logger = Logger.getLogger(getClass());
    @Override
    public String postSend(String url, String param) {
        String jsonStr = "";
        logger.info("Url: "+url+" param: "+param);
        try {
             jsonStr = HttpProxy.httpPost(url, param);
        } catch (Exception e) {
            logger.error("请求微信失败",e);
            return null;
        }
        return jsonStr;
    }
}
