package com.quyiyuan.weichat.modules.openid.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kyee.nextframework.core.base.resultmodel.ResultModel;
import com.kyee.nextframework.core.base.resultmodel.internal.EmptyResultModel;
import com.quyiyuan.weichat.comm.controller.WxBaseController;
import com.quyiyuan.weichat.modules.openid.service.IOpenIdService;

@Controller
public class OpenIdServletController extends WxBaseController {
	@Autowired
	@Qualifier("openIdService")
	private IOpenIdService openIdService;
	private Properties properties;
	private String appWebUrl;
	private String index;
	private String consultDoctorList;
	private String queue;
	private String patientCard;
	private String insurance;
	private String triageMain;
	private String nearby_hospital;
	private String report_multiple;
	private String medicalGuide;
	private String clinic_payment;
	private String inpatient_general;
	private String health_education;
	private String consultOrder;
	private String cWebUrl;
	private String c_consultDoctorList;
	private String c_consultOrder;
	private String c_appointment;
	private String c_clinicPayment;
	private String c_inpatienPayment;
	private String c_report;
	private String c_patientCard;
	private String s_index;
	private String s_appointment;
	private String s_clinicPayment;
	private String s_inpatienPayment;
	private String s_report;
	private String s_patientCard;
	@PostConstruct
	public void init() {
		properties = new Properties();
		InputStream path = this.getClass().getClassLoader().getResourceAsStream("system-config.properties");
		try {
			properties.load(path);
			// 从配置文件获取app网页版地址
			appWebUrl = properties.getProperty("app_web_url");
			index = properties.getProperty("index");
			consultDoctorList = properties.getProperty("consultDoctorList");
			queue = properties.getProperty("queue");
			triageMain = properties.getProperty("triageMain");
			report_multiple = properties.getProperty("reportMultiple");
			medicalGuide = properties.getProperty("medicalGuide");
			patientCard = properties.getProperty("patientCard");
			clinic_payment = properties.getProperty("clinic_payment");
			inpatient_general = properties.getProperty("inpatient_general");
			insurance = properties.getProperty("insurance");
			nearby_hospital = properties.getProperty("nearby_hospital");
			health_education = properties.getProperty("health_education");
			consultOrder = properties.getProperty("consultOrder");
			cWebUrl = properties.getProperty("c_web_url");
			c_consultDoctorList = properties.getProperty("c_consultDoctorList");
			c_consultOrder = properties.getProperty("c_consultOrder");
			c_appointment = properties.getProperty("c_appointment");
			c_clinicPayment = properties.getProperty("c_clinicPayment");
			c_inpatienPayment = properties.getProperty("c_inpatienPayment");
			c_report = properties.getProperty("c_report");
			c_patientCard = properties.getProperty("c_patientCard");

			s_index = properties.getProperty("s_index");
			s_appointment = properties.getProperty("s_appointment");
			s_clinicPayment = properties.getProperty("s_clinicPayment");
			s_inpatienPayment = properties.getProperty("s_inpatienPayment");
			s_report = properties.getProperty("s_report");
			s_patientCard = properties.getProperty("s_patientCard");

		} catch (IOException e) {
			logger.info("加载system-config配置文件出错");
			e.printStackTrace();
		}
	}

	@RequestMapping("/login")
	public ResultModel getOpenId(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String code = request.getParameter("code");
		String state = request.getParameter("state");
		String pst = request.getParameter("pst");
		logger.info("code:" + code);
		logger.info("state:" + state);
		if (code != null && state != null) {
			// 根据code获取openid
			String openid = openIdService.getOpenId(code);
			logger.info("OpenId is: " + openid);
			if(openid.equals("error")){
				logger.error("获取openId出错");
				return new EmptyResultModel();
			}
			if (openid != null) {
				String url = "";
				
				if("index".equals(state)){
					// 医院主页
					url = appWebUrl + index;
				}else if("consultDoctorList".equals(state)){ 
					// 咨询医生
					url = appWebUrl + consultDoctorList;
				}else if("queue".equals(state)){
					//叫号查询
					url = appWebUrl + queue;
				}else if("triageMain".equals(state)){
					// 症状自查
					url = appWebUrl + triageMain;
				}else if("medicalGuide".equals(state)){
					// 预约记录
					url = appWebUrl + medicalGuide;
				}else if ("report_multiple".equals(state)) {
					// 报告单查询
					url = appWebUrl + report_multiple;
				}else if("patient_card_recharge".equals(state)){
					// 就诊卡费用
					url = appWebUrl + patientCard;
				}else if ("clinic_payment_revise".equals(state)) {
					// 门诊费用
					url = appWebUrl + clinic_payment;
				}else if ("inpatient_general".equals(state)) {
					// 住院费用
					url = appWebUrl + inpatient_general;
				}else if ("insurance".equals(state)) {
					// 健康保险
					url = appWebUrl + insurance;
				}else if("health_education".equals(state)) { 
					// 健康教育
					long timeStamp = new Date().getTime();
					url = health_education + "" + openid + "&timeStamp=" + String.valueOf(timeStamp);
				}else if("consult_order".equals(state)){
					//订单咨询
					url = appWebUrl + consultOrder;
				}
				
				url = url.replace("OPEN-ID", openid);
				//如果传入pst参数，则替换掉趣医公众号的pst
				if(pst != null && pst.length() > 0){
					url = url.replace("020000", pst);
				}

				response.sendRedirect(url);

			}
		} else {
			response.sendRedirect("test.html");
		}
		return new EmptyResultModel();
	}

	//个性化医院跳转
	@RequestMapping("/clogin")
	public ResultModel getOpenIdC(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String code = request.getParameter("code");
		String state = request.getParameter("state");
		String pst = request.getParameter("pst");
		String hospitalId = request.getParameter("hospitalId");
		logger.info("code:" + code);
		logger.info("state:" + state);
		if (code != null && state != null) {
			// 根据code获取openid
			String openid = openIdService.getOpenId(code);
			logger.info("OpenId is: " + openid);
			if(openid.equals("error")){
				logger.error("获取openId出错");
				return new EmptyResultModel();
			}
			if (openid != null) {
				String url = "";
				if("consultDoctorList".equals(state)){
					// 咨询医生
					url = cWebUrl + c_consultDoctorList;
				} else if("consultOrder".equals(state)){
					//我的订单
					url = cWebUrl + c_consultOrder;
				} else if("appointment".equals(state)){
					//预约挂号-选择科室页面
					url = cWebUrl + c_appointment;
				} else if("clinic_payment_revise".equals(state)){
					//门诊缴费
					url = cWebUrl + c_clinicPayment;
				} else if("inpatient_general".equals(state)){
					//住院缴费
					url = cWebUrl + c_inpatienPayment;
				} else if("report_multiple".equals(state)){
					//报告单查询
					url = cWebUrl + c_report;
				} else if("patient_card_recharge".equals(state)){
					//就诊卡充值
					url = cWebUrl + c_patientCard;
				} else if("index".equals(state)){
					//首页
					url = cWebUrl + "?userSource=0&PublicServiceType=:PST&hospitalID=:HOSPITAL_ID&openid=:OPEN_ID#/";
				} else if("appointment_regist_list".equals(state)){
					//预约记录列表
					url = cWebUrl + "?wx_forward=appointment_regist_list&userSource=0&PublicServiceType=:PST&hospitalID=:HOSPITAL_ID&openid=:OPEN_ID#/";
				} else {
					logger.error("clogin接口:错误的state,state作为wx_forward直接跳转");
					url = cWebUrl + "?wx_forward=" + state + "&userSource=0&PublicServiceType=:PST&hospitalID=:HOSPITAL_ID&openid=:OPEN_ID#/";
				}

				url = url.replace(":OPEN_ID", openid);
				//如果传入pst参数，则替换公众号的pst
				if(pst != null && pst.length() > 0){
					url = url.replace(":PST", pst);
				}
				//如果传入hospitalId参数,则替换掉hospitalId
				if(hospitalId != null && hospitalId.length() > 0){
					url = url.replace(":HOSPITAL_ID", hospitalId);
				}
				logger.info("重定向至：" + url);
				response.sendRedirect(url);
			}
		} else {
			response.sendRedirect("test.html");
		}
		return new EmptyResultModel();
	}

	//定制医院公众号跳转
	@RequestMapping("/slogin")
	public ResultModel getOpenIdS(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String code = request.getParameter("code");
		String state = request.getParameter("state");
		String pst = request.getParameter("pst");
		String hospitalId = request.getParameter("hospitalId");
		logger.info("code:" + code);
		logger.info("state:" + state);
		if (code != null && state != null) {
			// 根据code获取openid
			String openid = openIdService.getOpenId(code);
			logger.info("OpenId is: " + openid);
			if(openid.equals("error")){
				logger.error("获取openId出错");
				return new EmptyResultModel();
			}
			if (openid != null) {
				String url = "";

				if("index".equals(state)){
					//首页
					url = appWebUrl + s_index;
				} else if("appointment".equals(state)){
					//预约挂号-选择科室页面
					url = appWebUrl + s_appointment;
				} else if("clinic_payment_revise".equals(state)){
					//门诊缴费
					url = appWebUrl + s_clinicPayment;
				} else if("inpatient_general".equals(state)){
					//住院缴费
					url = appWebUrl + s_inpatienPayment;
				} else if("report_multiple".equals(state)){
					//报告单查询
					url = appWebUrl + s_report;
				} else if("patient_card_recharge".equals(state)){
					//就诊卡充值
					url = appWebUrl + s_patientCard;
				} else {
					logger.error("slogin接口:错误的state,state作为wx_forward进行跳转");
					url = appWebUrl + "?wx_forward=" + state + "&userSource=0&PublicServiceType=:PST&hospitalID=:HOSPITAL_ID&hospitalFilterEnable=0&openid=:OPEN_ID#/";
				}

				url = url.replace(":OPEN_ID", openid);
				//如果传入pst参数，则替换公众号的pst
				if(pst != null && pst.length() > 0){
					url = url.replace(":PST", pst);
				}
				//如果传入hospitalId参数,则替换掉hospitalId
				if(hospitalId != null && hospitalId.length() > 0){
					url = url.replace(":HOSPITAL_ID", hospitalId);
				}
				logger.info("重定向至：" + url);
				response.sendRedirect(url);
			}
		} else {
			response.sendRedirect("test.html");
		}
		return new EmptyResultModel();
	}

	@RequestMapping("revise")
	public void reviseMenu(HttpServletRequest request,HttpServletResponse response) throws IOException{
		logger.info("附近医院菜单跳转临时修改为www-new用于测试");
		nearby_hospital = properties.getProperty("test");
		response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(request.getParameter("callback")+"(\"附近医院菜单跳转临时修改为www-new用于测试\")");
        out.flush();
        out.close();
		
	}
	
	@RequestMapping("restore")
	public void restoreMenu(HttpServletRequest request,HttpServletResponse response) throws IOException{
		logger.info("测试完毕，附近医院菜单还原为原地址");
		nearby_hospital = properties.getProperty("nearby_hospital");
		response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(request.getParameter("callback")+"(\"测试完毕，附近医院菜单还原为原地址\")");
        out.flush();
        out.close();
	}

}
