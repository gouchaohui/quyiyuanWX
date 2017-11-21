package com.quyiyuan.weichat.modules.baseservice.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.kyee.nextframework.core.base.domain.internal.EmptyDomain;
import com.kyee.nextframework.core.common.utils.CommonUtils.HttpUtil;
import com.kyee.nextframework.core.common.utils.CommonUtils.JsonUtil;
import com.kyee.nextframework.core.common.utils.CommonUtils.TextUtil;
import com.quyiyuan.weichat.comm.JsonObject;
import com.quyiyuan.weichat.comm.service.IWxBaseService;
import com.quyiyuan.weichat.comm.service.impl.WxBaseService;
import com.quyiyuan.weichat.modules.baseservice.beans.Article;

@Service("QRcodeService")
public class QRcodeService extends WxBaseService<EmptyDomain, Serializable> implements IWxBaseService<EmptyDomain, Serializable> {
	
	private Properties properties;
	private String appBackEndServer;
	private String appWebUrl;

	@PostConstruct
	public void init(){
		properties = new Properties();
		InputStream path = this.getClass().getClassLoader().getResourceAsStream("system-config.properties");
		try {
			properties.load(path);
			// 从配置文件获取app云后台地址
			appBackEndServer = properties.getProperty("app_back_end_server");
			// 从配置文件获取app网页版地址
			appWebUrl = properties.getProperty("app_web_url");
		} catch (IOException e) {
			logger.error("加载system-config配置文件出错", e);
		}
	}
	
	/*
	 * 处理扫描的带参二维码，生成图文消息
	 * @param businessType  二维码中携带的码类型 1  医患互动； 2 检查检验单  6 医患互动标准二维码  9 科室随访二维码
	 * @param uuid  二维码中携带的码参数
	 * @param openId
	 */	
	public List<Article> handleQRcodeWithArg(String eventType, String businessType, String uuid, String openId){
		List<Article> list = new ArrayList<Article>();
		// 医患互动二维码
		if(businessType.equals("1")){
			list = handleDoctorQRcode(uuid, openId);
		}else if(businessType.equals("6")){
			list = handleHosDoctorQRcode(uuid, openId,businessType);
		}else if(businessType.equals("9")){
			String[] parmArr = uuid.split("&");
			if(parmArr.length != 4){
				list = handleDeptQRcode(parmArr, openId, businessType);
			}else{
				String hospitalId = parmArr[0];
				String deptCode = parmArr[1];
				String hospitalName = parmArr[2];
				String deptName = parmArr[3];
				list = handleDeptQRcode(hospitalId,deptCode,hospitalName,deptName,openId,businessType);
			}
		}else{
			list = buildQRcodeArticle(businessType, uuid, openId);
		}

		return list;
	}
	
	private List<Article> buildQRcodeArticle(String businessType, String hospitalID, String openId){
		// 初始化消息内容
		String articleTitle = "";
		String articlePictureUrl = "";
		String articleDescription = "";
		String articleUrl = appWebUrl
				+ "?wx_forward=qrcode_skip_controller&userSource=0&PublicServiceType=020000"
				+ "&businessType=" + businessType
				+ "&openId="  + openId
				+ "&hospitalID=" + hospitalID + "&hospitalId=" + hospitalID + "&HOSPITAL_ID=" + hospitalID
				+ "#/qrcode_skip_controller";
		
		if(businessType.equals("2")){
			// 报告单二维码
			articleTitle = "一键查询报告单";
			articlePictureUrl = "http://md-read.oss-cn-shanghai.aliyuncs.com/Images/report_weixin.jpg";
			articleDescription = "点击查看报告单详情";
		}else{
			// TODO
		}
		
		List<Article> list = new ArrayList<Article>();
		Map articleInfo = getQRcodeArticleInfo(businessType, hospitalID, openId);
		
		try {
			articleTitle = (String) articleInfo.get("title");
			articlePictureUrl = (String) articleInfo.get("pictureUrl");
			articleDescription = (String) articleInfo.get("description");

			if(!articleTitle.isEmpty()){
				Article article = new Article();
				article.setDescription(articleDescription);
				article.setPicUrl(articlePictureUrl);
				article.setTitle(articleTitle);
				article.setUrl(articleUrl);
				list.add(article); // 这里发送的是单图文，如果需要发送多图文则在这里 list
									// 中加入多个
									// Article 即可！
			}
		} catch (Exception e) {
			logger.error("从App取得的数据有误, " +  e);
		}

		return list;
	}
	
	/*
	 * 向app后台请求二维码扫描后要发送的图文信息的内容
	 */	
	private Map getQRcodeArticleInfo(String businessType, String hospitalID, String openId){
		String respFromApp = "";
		try {
			String getQRcodeArticleInfo = appBackEndServer
					+ "user/action/QRCodeBusinessActionC.jspx?"
					+ "op=getWXBusinessInfo"
					+ "&businessType=" + businessType
					+ "&hospitalID=" + hospitalID + "&hospitalId=" + hospitalID + "&HOSPITAL_ID=" + hospitalID
					+ "&openId=" + openId
					+ "&forceSatification=YES";		//医院维护框架不进行请求拦截

			respFromApp = HttpUtil.Sync.get(getQRcodeArticleInfo);
			JsonObject jsonObject = JsonUtil.json2Object(respFromApp, JsonObject.class);
			Map articleInfo = jsonObject.getData();
			logger.info("获取图文消息内容成功" + respFromApp);
			return articleInfo;
		} catch (Exception e) {
			logger.error("获取图文消息内容失败" + respFromApp);
			return null;
		}
	}

	/*
	 * 向app后台请求二维码扫描后要发送的图文信息的内容
	 * 携带统计参数 deptCode和doctorCode
	 */
	private Map getQRcodeArticleInfo(String businessType, String hospitalID, String openId, String deptCode, String doctorCode){
		String respFromApp = "";
		try {
			String getQRcodeArticleInfo = appBackEndServer
					+ "user/action/QRCodeBusinessActionC.jspx?"
					+ "op=getWXBusinessInfo"
					+ "&businessType=" + businessType
					+ "&hospitalID=" + hospitalID + "&hospitalId=" + hospitalID + "&HOSPITAL_ID=" + hospitalID
					+ "&openId=" + openId
					+ (TextUtil.isNotEmpty(doctorCode) ? ("&doctorCode=" + doctorCode) : "")
					+ "&deptCode=" + deptCode
					+ "&forceSatification=YES";		//医院维护框架不进行请求拦截

			respFromApp = HttpUtil.Sync.get(getQRcodeArticleInfo);
			JsonObject jsonObject = JsonUtil.json2Object(respFromApp, JsonObject.class);
			Map articleInfo = jsonObject.getData();
			logger.info("获取图文消息内容成功" + respFromApp);
			return articleInfo;
		} catch (Exception e) {
			logger.error("获取图文消息内容失败" + respFromApp);
			return null;
		}
	}
	
	// 处理医患互动二维码
	private List<Article> handleDoctorQRcode(String uuid, String openId){
		
		String articleTitle = "感谢关注";
		String articlePictureUrl = "http://md-read.oss-cn-shanghai.aliyuncs.com/Images/weiChat1.jpg";
		String articleDescription = "感谢关注趣医网，暂未为您匹配到医生，小趣还有很多便利服务，快去体验吧。";
		String articleUrl = appWebUrl + "#/home-%3EMAIN_TAB";
		String doctorName = "";
		String hospitalId = "";
		String deptCode = "";
		String doctorCode = "";
		String deptName = "";
		String doctorTitle="";
		
		// 向APP云查询医生、医院等详细信息
		try {
			Map doctorInfoMap = getDoctorInfoFromApp(uuid);
			doctorName = (String) doctorInfoMap.get("DOCTOR_NAME");
			hospitalId = (String) doctorInfoMap.get("HOSPITAL_ID");
			deptCode = (String) doctorInfoMap.get("DEPT_CODE");
			doctorCode = (String) doctorInfoMap.get("DOCTOR_CODE");
			deptName = (String) doctorInfoMap.get("DEPT_NAME");
			doctorTitle = (String)doctorInfoMap.get("DOCTOR_TITLE");

			doctorName = doctorName.trim();
			hospitalId = hospitalId.trim();
			deptCode = deptCode.trim();
			doctorCode = doctorCode.trim();
			deptName = deptName.trim();
			doctorTitle = doctorTitle.trim();

		} catch (Exception e) {
			logger.error("从App获取医生的数据有误, " + e);
		}

		if (TextUtil.isNotEmpty(doctorName)) {
			articleDescription = "感谢关注" + doctorName + "医生，您可以和医生在线交流，享受更多服务。";
			articleUrl = appWebUrl
					+ "?wx_forward=qrcode_skip_controller&userSource=0&PublicServiceType=020000&UUID="
					+ uuid + "&openId=" + openId +"&businessType=1"
					+ "&hospitalID=" + hospitalId + "&hospitalId=" + hospitalId + "&HOSPITAL_ID=" + hospitalId
					+ "&deptCode=" + URLEncoder.encode(deptCode) + "&doctorCode=" + URLEncoder.encode(doctorCode)
					+ "&deptName=" + URLEncoder.encode(deptName) + "&doctorName=" + URLEncoder.encode(doctorName) 
					+"&doctorTitle=" + URLEncoder.encode(doctorTitle) + "#/qrcode_skip_controller";

			//获取医患关系图文小心的内容模板
			//d% 为医生姓名
			try{
				Map articleInfo = getQRcodeArticleInfo("1",hospitalId,openId,deptCode,doctorCode);
				articleTitle = (String) articleInfo.get("title");
				articlePictureUrl = (String) articleInfo.get("pictureUrl");
				articleDescription = (String) articleInfo.get("description");
				//用医生姓名替换姓名占位符
				articleTitle = articleTitle.replace("d%",doctorName);
				articleDescription = articleDescription.replace("d%",doctorName);
			}catch (Exception e){
				logger.error("从App获取图文消息模板数据有误 " + e);
			}
		}

		Article article = new Article();
		article.setDescription(articleDescription);
		article.setPicUrl(articlePictureUrl);
		article.setTitle(articleTitle);
		article.setUrl(articleUrl);
		List<Article> list = new ArrayList<Article>();
		list.add(article); 
		
		return list;
	}

	// 处理医患互动二维码   任务号：XAQYJG-258
	// 标准码，携带医院参数(hospitalId)
	private List<Article> handleHosDoctorQRcode(String hospitalId, String openId, String businessType){

		String articleTitle = "咨询医生，助您康复";
		String articlePictureUrl = "http://md-read.oss-cn-shanghai.aliyuncs.com/Images/weiChat1.jpg";
		String articleDescription = "医院开通在线咨询服务啦，主治医生就在对面，复诊咨询不必来院。在线享受更加精准可靠的医疗服务，助您康复。";
		String articleUrl = appWebUrl
				+ "?wx_forward=qrcode_skip_controller&userSource=0&PublicServiceType=020000"
				+ "&openId="  + openId
				+ "&hospitalID=" + hospitalId + "&hospitalId=" + hospitalId + "&HOSPITAL_ID=" + hospitalId
				+ "&businessType=" + "8"	//因为正式二维码根据businessType=6生成，但是公共跳转6已被占用，该分支businessType分配为8。
				+"#/qrcode_skip_controller";

		//获取医患关系标准图文消息的内容模板
		try{
			Map articleInfo = getQRcodeArticleInfo(businessType,hospitalId,openId);
			articleTitle = (String) articleInfo.get("title");
			articlePictureUrl = (String) articleInfo.get("pictureUrl");
			articleDescription = (String) articleInfo.get("description");
		}catch (Exception e){
			logger.error("从App获取图文消息模板数据有误 " + e);
		}

		Article article = new Article();
		article.setDescription(articleDescription);
		article.setPicUrl(articlePictureUrl);
		article.setTitle(articleTitle);
		article.setUrl(articleUrl);
		List<Article> list = new ArrayList<Article>();
		list.add(article);

		return list;
	}

	// 处理科室随访二维码   任务号：XAQYJG-356
	private List<Article> handleDeptQRcode(String hospitalId, String deptCode, String hospitalName, String deptName, String openId, String businessType){
		String articleTitle = "关注医生，在线随访";
		String articlePictureUrl = "https://app-dev.oss-cn-qingdao.aliyuncs.com/DevelopLibrary/Upload/wxBusinessPhoto//wxBusinessPhoto201709011350000.jpg";
		String articleDescription = "hosp%dept%已经开通在线随访，主治医生对您的病情在线跟踪，请填写必要信息，方便医生为您提供帮助。";

		//组装图文消息跳转链接
		String articleUrl = appWebUrl
				+ "?wx_forward=qrcode_skip_controller&userSource=0&PublicServiceType=020000"
				+ "&openId="  + openId
				+ "&hospitalID=" + hospitalId + "&hospitalId=" + hospitalId + "&HOSPITAL_ID=" + hospitalId
				+ "&deptCode=" + deptCode
				+ "&businessType=" + businessType
				+"#/qrcode_skip_controller";

		//获取图文消息的内容模板
		try{
			Map articleInfo = getQRcodeArticleInfo(businessType, hospitalId, openId, deptCode, null);
			articleTitle = (String) articleInfo.get("title");
			articlePictureUrl = (String) articleInfo.get("pictureUrl");
			articleDescription = (String) articleInfo.get("description");
		}catch (Exception e){
			logger.error("从App获取图文消息模板数据有误 " + e);
		}

		//用参数替换占位符
		try{
			articleDescription = articleDescription.replace("hosp%",hospitalName);
			articleDescription = articleDescription.replace("dept%",deptName);
		}catch (Exception e){
			logger.error("二维码携带未携带医院名或科室名，" + e);
		}


		Article article = new Article();
		article.setDescription(articleDescription);
		article.setPicUrl(articlePictureUrl);
		article.setTitle(articleTitle);
		article.setUrl(articleUrl);
		List<Article> list = new ArrayList<Article>();
		list.add(article);

		return list;
	}

	// 新版随访功能   任务号：XAQYJG-718
	private List<Article> handleDeptQRcode(String[] parmArr, String openId, String businessType){
		String articleTitle = "关注医生，在线随访";
		String articlePictureUrl = "https://app-dev.oss-cn-qingdao.aliyuncs.com/DevelopLibrary/Upload/wxBusinessPhoto//wxBusinessPhoto201709011350000.jpg";
		String articleDescription = "hosp%dept%已经开通在线随访，主治医生对您的病情在线跟踪，请填写必要信息，方便医生为您提供帮助。";

		String hospitalId = parmArr[0];
		String deptCode = parmArr[1];
		String hospitalName = parmArr[2];
		String deptName = parmArr[3];
		String registType = parmArr[4];
		String deptType = null;
		if(parmArr.length > 5){
			deptType = parmArr[5];
		}


		//组装图文消息跳转链接
		String articleUrl = appWebUrl
				+ "?wx_forward=qrcode_skip_controller&userSource=0&PublicServiceType=020000"
				+ "&openId="  + openId
				+ "&hospitalID=" + hospitalId + "&hospitalId=" + hospitalId + "&HOSPITAL_ID=" + hospitalId
				+ "&deptCode=" + deptCode
				+ "&businessType=" + businessType
				+ (TextUtil.isNotEmpty(registType) ? ("&registType=" + registType) : "")
				+ (TextUtil.isNotEmpty(deptType) ? ("&deptType=" + deptType) : "")
				+"#/qrcode_skip_controller";

		//获取图文消息的内容模板
		try{
			Map articleInfo = getQRcodeArticleInfo(businessType, hospitalId, openId, deptCode, null);
			articleTitle = (String) articleInfo.get("title");
			articlePictureUrl = (String) articleInfo.get("pictureUrl");
			articleDescription = (String) articleInfo.get("description");
		}catch (Exception e){
			logger.error("从App获取图文消息模板数据有误: " + e);
		}

		//获取医院及科室参数
		try {
			Map deptInfo = getNameByCode(hospitalId, deptCode);
			hospitalName = (String) deptInfo.get("hospitalName");
			deptName = (String) deptInfo.get("deptName");
		}catch (Exception e){
			logger.error("从App获取医院消息数据有误: " + e);
		}

		//用参数替换占位符
		try{
			articleDescription = articleDescription.replace("hosp%",hospitalName);
			articleDescription = articleDescription.replace("dept%",deptName);
		}catch (Exception e){
			logger.error("二维码参数不正确或网络异常，" + e);
		}


		Article article = new Article();
		article.setDescription(articleDescription);
		article.setPicUrl(articlePictureUrl);
		article.setTitle(articleTitle);
		article.setUrl(articleUrl);
		List<Article> list = new ArrayList<Article>();
		list.add(article);

		return list;
	}


	// 向APP云发请求，根据UUID获取医生信息
	public Map getDoctorInfoFromApp(String uuid) {
		String respFromApp = "";
		logger.info("UUID为" + uuid);
		try {
			String getDoctorNameFromApp = appBackEndServer
					+ "appoint/action/DoctorPatientRelationActionC.jspx?op=getDoctorInfoByQrActionC"
					+ "&DOCTOR_QR_CODE=" + uuid;
			respFromApp = HttpUtil.Sync.get(getDoctorNameFromApp);
			JsonObject jsonObject = JsonUtil.json2Object(respFromApp, JsonObject.class);
			Map doctorMap = jsonObject.getData();
			return doctorMap;
		} catch (Exception e) {
			logger.error("查询医生姓名失败" + respFromApp);
			return null;
		}
	}

	// 向APP云发请求，根据code查找name  hospitalId->hospitalName  deptCode->deptName
	private Map getNameByCode(String hospitalId, String deptCode){
		String respFromApp = "";
		logger.info("查询入参 hospitalId：" + hospitalId + "  deptCode:" + deptCode);
		try {
			String url = appBackEndServer
					+ "user/action/QRCodeBusinessActionC.jspx?op=getNameByCode"
					+ "&hospitalId=" + hospitalId
					+ "&deptCode=" + deptCode;
			respFromApp = HttpUtil.Sync.get(url);
			logger.info("请求APP云:" + url);
			JsonObject jsonObject = JsonUtil.json2Object(respFromApp, JsonObject.class);
			Map dataMap = jsonObject.getData();
			return dataMap;
		} catch (Exception e) {
			logger.error("通过code查询name失败：" + respFromApp);
			return null;
		}
	}
}
