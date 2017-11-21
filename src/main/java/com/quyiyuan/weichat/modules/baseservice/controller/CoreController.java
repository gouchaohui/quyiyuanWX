package com.quyiyuan.weichat.modules.baseservice.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import com.kyee.nextframework.core.common.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kyee.nextframework.core.base.resultmodel.ResultModel;
import com.kyee.nextframework.core.base.resultmodel.internal.text.impl.StandardResultModel;
import com.quyiyuan.weichat.comm.controller.WxBaseController;
import com.quyiyuan.weichat.modules.baseservice.service.ICoreService;


@Controller
public class CoreController extends WxBaseController{
	@Autowired
	@Qualifier("coreService")
	ICoreService coreService;
	
	@RequestMapping(value="/wechat",method=GET)
	public void checkSigToWx(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String token="";
		Properties properties =new Properties();
		InputStream path = this.getClass().getClassLoader().getResourceAsStream("system-config.properties");
		try {
			properties.load(path);
			token = properties.getProperty("token");
		} catch (IOException e) {
			logger.info("加载system-config配置文件出错");
			e.printStackTrace();
		}
		String signature = request.getParameter("signature");
		String timestamp = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String echostr = request.getParameter("echostr");
		response.setHeader("Content-type", "text/html;charset=UTF-8");//指定消息头以UTF-8码表读数据  
		//加密校验
		PrintWriter out = response.getWriter();  
	    // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败  
        if (coreService.checkSignature(signature, timestamp, nonce,token)) {  
        	logger.info(echostr);
            out.print(echostr);  
        }  
        out.close();
        out = null;
	}
	
	@RequestMapping(value="/wechat",method=POST)
	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException{
		 request.setCharacterEncoding("UTF-8");  
	        response.setCharacterEncoding("UTF-8");  
	        logger.info(request);
	        // 调用核心业务类接收消息、处理消息  
	        String respMessage = coreService.processRequest(request);  
	        logger.info(respMessage);
	        // 响应消息  
	        PrintWriter out = response.getWriter();  
	        out.print(respMessage);  
	        out.close();
	}
	

	@RequestMapping("/createMenu")
	public ResultModel creatMenu(HttpServletRequest request){
		StandardResultModel menuResult = new StandardResultModel();
		String testFlagStr = request.getParameter("testFlag");	//测试环境创建菜单 传入 ?testFlag=1
		Boolean testFlag = "1".equals(testFlagStr) ? true : false;
		String respMsg = coreService.doCreateMenu(testFlag);
		menuResult.setData(respMsg);
		return menuResult;
	}
}
