package com.quyiyuan.weichat.modules.openthird.util;

import com.quyiyuan.weichat.modules.openthird.aes.AesException;
import com.quyiyuan.weichat.modules.openthird.aes.WXBizMsgCrypt;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息处理工具类
 *
 * @author Administrator
 * @create 2017-11-03 16:04
 **/
public class MessageUtil {
    protected Logger logger = Logger.getLogger(getClass());
    /**
     * @param request
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        Map<String, String> map = new HashMap<String, String>();

        InputStream inputStream = request.getInputStream();
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();
        List<Element> elementList = root.elements();

        for (Element e : elementList)
            map.put(e.getName(), e.getText());

        inputStream.close();

        return map;
    }
    /**
     *
     * @param xmlStr
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> parseXml(String xmlStr) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
       /* InputStream inputStream = new ByteArrayInputStream(xmlStr.getBytes());
        SAXReader reader = new SAXReader();
        reader.setEncoding("UTF8");
        Document document = reader.read(inputStream);*/
        Document document = DocumentHelper.parseText(xmlStr);
        document.setXMLEncoding("UTF-8");
        Element root = document.getRootElement();
        List<Element> elementList = root.elements();
        for (Element e : elementList)
            map.put(e.getName(), e.getText());
       // inputStream.close();
        return map;
    }
    /**
     * 工具类：回复微信服务器"文本消息"
     * @param response
     * @param returnvaleue
     */
    public static void output(HttpServletResponse response, String returnvaleue){
        try {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            PrintWriter pw = response.getWriter();
            pw.write(returnvaleue);
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String createEncryptTextMsg(String fromUserName, String toUserName, String content, String nonce, WXBizMsgCrypt pc) {
        final Long createTime = Calendar.getInstance().getTimeInMillis() / 1000;
        final String textMsg = "<xml>" +
                "<ToUserName><![CDATA[" + fromUserName + "]]></ToUserName>" +
                "<FromUserName><![CDATA[" + toUserName + "]]></FromUserName>" +
                "<CreateTime>" + createTime + "</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[" + content + "]]></Content>" +
                "</xml>";
        String encryptMsg = "";
        try {
            encryptMsg = pc.encryptMsg(textMsg, createTime.toString(), nonce);
        } catch (AesException e) {
            e.printStackTrace();
        }
        return encryptMsg;
    }
}
