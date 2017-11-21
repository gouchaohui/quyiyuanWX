package com.quyiyuan.weichat.modules.templatemessage.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import com.kyee.nextframework.core.common.utils.CommonUtils;
import com.quyiyuan.weichat.modules.openthird.service.IOpenThirdService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kyee.nextframework.core.base.service.impl.BaseService;
import com.kyee.nextframework.core.common.utils.CommonUtils.JsonUtil;
import com.kyee.nextframework.core.support.cache.normal.impl.bean.CacheNode;
import com.quyiyuan.weichat.comm.HttpProxy;
import com.quyiyuan.weichat.modules.accesstoken.service.IAccessTokenService;
import com.quyiyuan.weichat.modules.templatemessage.beans.AppointSuccessData;
import com.quyiyuan.weichat.modules.templatemessage.beans.BaseValue;
import com.quyiyuan.weichat.modules.templatemessage.beans.CommProperty;
import com.quyiyuan.weichat.modules.templatemessage.beans.Data;
import com.quyiyuan.weichat.modules.templatemessage.beans.HospitalReportData;
import com.quyiyuan.weichat.modules.templatemessage.beans.StopInformData;
import com.quyiyuan.weichat.modules.templatemessage.dao.ITemplateIdDao;
import com.quyiyuan.weichat.modules.templatemessage.domain.TemplateId;
import com.quyiyuan.weichat.modules.templatemessage.protocol.TemplateMessageProtocolOut;
import com.quyiyuan.weichat.modules.templatemessage.service.ITemplateMsgService;

import net.sf.ehcache.config.CacheConfiguration;

@Service("templateMsgService")
public class TemplateMsgService extends BaseService<TemplateId, String> implements ITemplateMsgService {
	@Autowired
	@Qualifier("templateIdDao")
	private ITemplateIdDao templateIdDao;

	@Autowired
	@Qualifier("accessTokenService")
	private IAccessTokenService accessTokenService;

	@Qualifier("openThirdService")
	private IOpenThirdService openThirdService;

	private Properties properties;
	private static final String CHCHE_NAME = "TemplateIdCache";

	@PostConstruct
	private void init() {
		logger.info("正在建立服务器缓存");
		CacheConfiguration cfg = new CacheConfiguration();
		cfg.setName(CHCHE_NAME);
		cfg.setTimeToIdleSeconds(60 * 60);
		cfg.setMaxElementsInMemory(20);
		localCacheService.addCache(cfg);
		properties = new Properties();
		InputStream path = this.getClass().getClassLoader().getResourceAsStream("system-config.properties");
		try {
			properties.load(path);
		} catch (IOException e) {
			logger.info("加载system-config配置文件出错");
			e.printStackTrace();
		}
	}

	/**
	 * 根据msgType选择模板推送
	 *
	 * @param openId
	 * @param msgType
	 *            推送类型
	 * @param data
	 *            推送内容
	 * @return TemplateMessageProtocolOut
	 */
	@Override
	public TemplateMessageProtocolOut doChooseTemplateMsg(String openId, String msgType, String data) {
		String respMessage = null;
		TemplateMessageProtocolOut templateMessageProtocolOut = new TemplateMessageProtocolOut();
		switch (msgType) {
		// 预约挂号成功通知
		case "appointSuccess":
			respMessage = doAppointSuccess(openId, data);
			break;
		case "appointFailed":
			respMessage = doAppointFailed(openId, data);
			break;
		case "registerFailed":
			respMessage = doRegisterFailed(openId, data);
			break;
		case "registerReminder":	//放号提醒
			respMessage = doRegisterReminder(openId, data);
			break;
		case "cancleAppointSuccess":
			respMessage = doCancelAppointSuccess(openId, data);
			break;
		case "cancleAppointFail":
			respMessage = doCancelAppointFailed(openId, data);
			break;
		case "transferAppointSuccess":
			respMessage = doTransferAppointSuccess(openId, data);
			break;
		case "transferAppointFail":
			respMessage = doTransferAppointFailed(openId, data);
			break;
		case "appointInform":
			respMessage = doAppointInform(openId, data);
			break;
		case "stopInform":
			respMessage = doStopInform(openId, data);
			break;
		case "hospitalReport":
			respMessage = doHospitalReport(openId, data);
			break;
		case "medicalInform":
			respMessage = doMedicalInform(openId, data);
			break;
		case "doctorInform":
			respMessage = doDoctorInform(openId, data);
			break;
		case "medicalRecordInform":
			respMessage = doMedicalRecordInform(openId, data);
			break;
		case "patientReport":
			respMessage = doPatientReport(openId, data);
			break;
		case "dayList":
			respMessage = doDayList(openId, data);
			break;
		case "remindOfConsultation": // 咨询提醒
			respMessage = doRemindOfConsultation(openId, data);
			break;
		case "followUp": // 随访提醒
			respMessage = doFollowUp(openId, data);
			break;
		case "taskReminder": // 任务提醒
			respMessage = doTaskReminder(openId, data);
			break;
			//任务号：XAQYJG-194   begin
		case "rushSuccess": //抢号成功提醒
			respMessage = doRushSuccess(openId, data);
			break;
		case "rushFail": //抢号失败提醒
			respMessage = doRushFail(openId, data);
			break;
		case "rushOutDate": //抢号过期提醒
			respMessage = doRushOutDate(openId, data);
			break;
		case "rushHasClinic": //有号提醒
			respMessage = doRushHasClinic(openId, data);
			break;
		case "suggestWeiXinPush": //意见反馈处理通知
			respMessage = doFeedbackRemind(openId, data);
			break;
		case "patientRechargeSuccess": //就诊卡充值成功提醒
			respMessage = doRechargeSuccess(openId, data);
			break;
		case "patientRechargeFalse": //就诊卡充值失败提醒
			respMessage = doRechargeFail(openId, data);
			break;
		//任务号：XAQYJG-194   end

		//任务号：XAQYJG-201   begin
		case "noReception": //超时未接诊提醒
			respMessage = doNoReception(openId, data);
			break;
		case "payRemind": //待支付提醒（超过5分钟未支付）
			respMessage = doPayRemind(openId, data);
			break;
		case "receptionRemind": //医生接诊提醒
			respMessage = doReceptionRemind(openId, data);
			break;
		case "rejectRemind": //医生驳回提醒
			respMessage = doRejectRemind(openId, data);
			break;
		case "consultRemind": //电话咨询和视频咨询快到约定时间提醒（约定时间前10分钟提醒）
			respMessage = doConsultRemind(openId, data);
			break;
		case "consultFinish": //咨询完成提醒
			respMessage = doConsultFinish(openId, data);
			break;
		//任务号：XAQYJG-201   end
		case "videoFinish": //视频问诊完成提醒
			respMessage = doVideoFinish(openId, data);
			break;
		case "onlinePreFinish": //购药开单
			respMessage = doOnlinePreFinish(openId, data);
			break;
		}
		try {
			if (respMessage == null) {
				throw new Exception();
			}
			templateMessageProtocolOut.setRespMsg(respMessage.toString());
			return templateMessageProtocolOut;
		} catch (Exception e) {
			logger.error("模板消息推送失败");
			return null;
		}
	}

	/**
	 * 预约挂号成功模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doAppointSuccess(String openId, String data) {
		logger.info("appointSuccess");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "appointSuccess")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("appointSuccess").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("appointSuccess", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "appointSuccess").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_appoint");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String hospitalName = JsonUtil.getValue(data, "hospitalname", String.class);
		String deptName = JsonUtil.getValue(data, "deptname", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorname", String.class);
		String planStartTime = JsonUtil.getValue(data, "planstarttime", String.class);
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String withdrawingTime = (String) JsonUtil.getValue(data, "withdrawingTime", String.class);
		String info = (String) JsonUtil.getValue(data, "info", String.class);
		// Data部分参数
		AppointSuccessData appointSuccessData = new AppointSuccessData();
		appointSuccessData.setFirst(new BaseValue("您好，您已预约挂号成功！"));
		appointSuccessData.setHospitalName(new BaseValue(hospitalName));
		appointSuccessData.setDeptName(new BaseValue(deptName));
		appointSuccessData.setDoctorName(new BaseValue(doctorName));
		appointSuccessData.setPlanStartTime(new BaseValue(planStartTime));
		// remark参数
		String remarkValue = "就诊人：" + patientName;
		if (withdrawingTime != null && !(withdrawingTime.equals(""))) {
			remarkValue = remarkValue + "\n退号时间：" + withdrawingTime;
		}
		if (info != null && !(info.equals(""))) {
			remarkValue = remarkValue + "\n就诊提示：" + info;
		}
		appointSuccessData.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(appointSuccessData);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 预约失败模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doAppointFailed(String openId, String data) {
		logger.info("appointFailed");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "appointFailed")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("appointFailed").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("appointFailed", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "appointFailed").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_appoint");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String hospitalName = JsonUtil.getValue(data, "hospitalname", String.class);
		String deptName = JsonUtil.getValue(data, "deptname", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorname", String.class);
		String submitTime = JsonUtil.getValue(data, "submitTime", String.class);
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String failedReason = (String) JsonUtil.getValue(data, "failedReason", String.class);
		// Data部分参数
		Data appointFail = new Data();
		appointFail.setFirst(new BaseValue("您好，很抱歉告知您预约失败。"));
		appointFail.setKeyword1(new BaseValue(hospitalName));
		appointFail.setKeyword2(new BaseValue(deptName));
		appointFail.setKeyword3(new BaseValue(doctorName));
		appointFail.setKeyword4(new BaseValue(submitTime));
		appointFail.setKeyword5(new BaseValue(failedReason));
		// remark参数
		String remarkValue = "就诊人：" + patientName + "\n\n为了顺利就医，请重新预约挂号。如已缴费，退款将在7个工作日内通过原支付渠道返还。";
		appointFail.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(appointFail);
		String msgJson = JsonUtil.object2Json(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 挂号失败模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doRegisterFailed(String openId, String data) {
		logger.info("registerFailed");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "registerFailed")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("registerFailed").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("registerFailed", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "registerFailed").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_appoint");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String hospitalName = JsonUtil.getValue(data, "hospitalname", String.class);
		String deptName = JsonUtil.getValue(data, "deptname", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorname", String.class);
		String appointTime = JsonUtil.getValue(data, "appointTime", String.class);
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String failedReason = (String) JsonUtil.getValue(data, "failedReason", String.class);
		// Data部分参数
		Data registerFailed = new Data();
		registerFailed.setFirst(new BaseValue("您好，很抱歉告知您挂号失败。"));
		registerFailed.setKeyword1(new BaseValue(hospitalName));
		registerFailed.setKeyword2(new BaseValue(deptName));
		registerFailed.setKeyword3(new BaseValue(doctorName));
		registerFailed.setKeyword4(new BaseValue(appointTime));
		// remark参数
		String remarkValue = "失败原因：" + failedReason + "\n就诊人：" + patientName
				+ "\n\n为了顺利就医，请重新挂号。如已缴费，退款将在7个工作日内通过原支付渠道返还。";
		registerFailed.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(registerFailed);
		String msgJson = JsonUtil.object2Json(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 放号提醒模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doRegisterReminder(String openId, String data) {
		logger.info("registerReminder");
		String templateId = getTemplateIdByName("registerReminder");
		// 生成详情页跳转url
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_doctorList");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		String deptName = JsonUtil.getValue(data, "deptname", String.class);
		String deptCode = JsonUtil.getValue(data, "deptcode", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&deptCode=" + deptCode + "&deptName=" + deptName + "&hospitalID=" + hospitalId + "&isShowAllTab=1" + "#/\",";


		// 模板消息入参
		String hospitalName = JsonUtil.getValue(data, "hospitalname", String.class);
		String planStartTime = JsonUtil.getValue(data, "planstarttime", String.class);
		// Data部分参数
		Data registerReminder = new Data();
		registerReminder.setFirst(new BaseValue("您好，您关注的科室将在5分钟之内放号。"));
		registerReminder.setKeyword1(new BaseValue(planStartTime));
		registerReminder.setKeyword2(new BaseValue(hospitalName));
		// remark参数
		String remarkValue = "科室：" + deptName + "\n\n请尽快查看该科室号源情况，预祝您预约挂号成功！";
		registerReminder.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(registerReminder);
		String msgJson = JsonUtil.object2Json(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 取消预约挂号成功模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doCancelAppointSuccess(String openId, String data) {
		logger.info("cancelAppointSuccess");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "cancelAppointSuccess")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("cancelAppointSuccess").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("cancelAppointSuccess", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "cancelAppointSuccess").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_appoint");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String hospitalName = JsonUtil.getValue(data, "hospitalname", String.class);
		String deptName = JsonUtil.getValue(data, "deptname", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorname", String.class);
		String appointTime = JsonUtil.getValue(data, "appointTime", String.class);
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		// Data部分参数
		Data cancelAppointSuccess = new Data();
		cancelAppointSuccess.setFirst(new BaseValue("您好，您的预约挂号信息已取消成功。"));
		cancelAppointSuccess.setKeyword1(new BaseValue(patientName));
		cancelAppointSuccess.setKeyword2(new BaseValue(hospitalName));
		cancelAppointSuccess.setKeyword3(new BaseValue(deptName));
		cancelAppointSuccess.setKeyword4(new BaseValue(doctorName));
		cancelAppointSuccess.setKeyword5(new BaseValue(appointTime));
		// remark参数
		String remarkValue = "" + "\n如已缴费，退款将在7个工作日内通过原支付渠道返还，祝您身体健康。";
		cancelAppointSuccess.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(cancelAppointSuccess);
		String msgJson = JsonUtil.object2Json(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 取消预约挂号失败模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doCancelAppointFailed(String openId, String data) {
		logger.info("cancelAppointFailed");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "cancelAppointFailed")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("cancelAppointFailed").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("cancelAppointFailed", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "cancelAppointFailed").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_appoint");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String hospitalName = JsonUtil.getValue(data, "hospitalname", String.class);
		String deptName = JsonUtil.getValue(data, "deptname", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorname", String.class);
		String appointTime = JsonUtil.getValue(data, "appointTime", String.class);
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String failedReason = (String) JsonUtil.getValue(data, "failedReason", String.class);
		// Data部分参数
		Data cancelAppointFailed = new Data();
		cancelAppointFailed.setFirst(new BaseValue("您好，您的预约挂号信息已取消失败。"));
		cancelAppointFailed.setKeyword1(new BaseValue(patientName));
		cancelAppointFailed.setKeyword2(new BaseValue(hospitalName));
		cancelAppointFailed.setKeyword3(new BaseValue(deptName));
		cancelAppointFailed.setKeyword4(new BaseValue(doctorName));
		cancelAppointFailed.setKeyword5(new BaseValue(appointTime));
		// remark参数
		String remarkValue = "失败原因：" + failedReason + "\n\n如已缴费，退款将在7个工作日内通过原支付渠道返还，祝您身体健康。";
		cancelAppointFailed.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(cancelAppointFailed);
		String msgJson = JsonUtil.object2Json(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 转挂号成功模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doTransferAppointSuccess(String openId, String data) {
		logger.info("transferSuccess");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "transferSuccess")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("transferSuccess").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("transferSuccess", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "transferSuccess").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_appoint");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String hospitalName = JsonUtil.getValue(data, "hospitalname", String.class);
		String deptName = JsonUtil.getValue(data, "deptname", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorname", String.class);
		String planStartTime = JsonUtil.getValue(data, "planstarttime", String.class);
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String withdrawingTime = (String) JsonUtil.getValue(data, "withdrawingTime", String.class);
		String info = (String) JsonUtil.getValue(data, "info", String.class);
		// Data部分参数
		AppointSuccessData transferSuccessData = new AppointSuccessData();
		transferSuccessData.setFirst(new BaseValue("您好，您已转挂号成功!"));
		transferSuccessData.setHospitalName(new BaseValue(hospitalName));
		transferSuccessData.setDeptName(new BaseValue(deptName));
		transferSuccessData.setDoctorName(new BaseValue(doctorName));
		transferSuccessData.setPlanStartTime(new BaseValue(planStartTime));
		// remark参数
		String remarkValue = "就诊人：" + patientName;
		if (withdrawingTime != null && !(withdrawingTime.equals(""))) {
			remarkValue = remarkValue + "\n退号时间：" + withdrawingTime;
		}
		if (info != null && !(info.equals(""))) {
			remarkValue = remarkValue + "\n就诊提示：" + info;
		}
		transferSuccessData.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(transferSuccessData);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 转挂号失败模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doTransferAppointFailed(String openId, String data) {
		logger.info("transferFailed");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "transferFailed")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("transferFailed").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("transferFailed", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "transferFailed").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_appoint");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String hospitalName = JsonUtil.getValue(data, "hospitalname", String.class);
		String deptName = JsonUtil.getValue(data, "deptname", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorname", String.class);
		String submitTime = JsonUtil.getValue(data, "submitTime", String.class);
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String failedReason = (String) JsonUtil.getValue(data, "failedReason", String.class);
		// Data部分参数
		Data transferFailed = new Data();
		transferFailed.setFirst(new BaseValue("您好，很抱歉告知您转挂号失败。"));
		transferFailed.setKeyword1(new BaseValue(hospitalName));
		transferFailed.setKeyword2(new BaseValue(deptName));
		transferFailed.setKeyword3(new BaseValue(doctorName));
		transferFailed.setKeyword4(new BaseValue(submitTime));
		transferFailed.setKeyword5(new BaseValue(failedReason));
		// remark参数
		String remarkValue = "就诊人：" + patientName + "\n\n为了顺利就医，请重新转挂号。如已缴费，退款将在7个工作日内通过原支付渠道返还。";
		transferFailed.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(transferFailed);
		String msgJson = JsonUtil.object2Json(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 就诊提醒模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doAppointInform(String openId, String data) {
		logger.info("appointInform");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "appointInform")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("appointInform").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("appointInform", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "appointInform").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_appoint");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String hospitalName = JsonUtil.getValue(data, "hospitalname", String.class);
		String deptName = JsonUtil.getValue(data, "deptname", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorname", String.class);
		String appointTime = JsonUtil.getValue(data, "appointTime", String.class);
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String cardNum = JsonUtil.getValue(data, "cardNum", String.class);
		// Data部分参数
		Data appointFail = new Data();
		appointFail.setFirst(new BaseValue("您好，您的预约挂号信息如下："));
		appointFail.setKeyword1(new BaseValue(patientName));
		appointFail.setKeyword2(new BaseValue(cardNum));
		appointFail.setKeyword3(new BaseValue(hospitalName));
		appointFail.setKeyword4(new BaseValue(deptName));
		appointFail.setKeyword5(new BaseValue(appointTime));
		// remark参数
		String remarkValue = "医生：" + doctorName + "\n\n请按时前往医院就诊，祝您身体健康。";
		appointFail.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(appointFail);
		String msgJson = JsonUtil.object2Json(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 停诊通知模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doStopInform(String openId, String data) {
		logger.info("stopInform");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "stopInform")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("stopInform").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("stopInform", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "stopInform").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_appoint");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String hospitalName = JsonUtil.getValue(data, "hospitalname", String.class);
		String deptName = JsonUtil.getValue(data, "deptname", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorname", String.class);
		String planStartTime = JsonUtil.getValue(data, "appointTime", String.class);
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		// Data部分参数
		StopInformData stopInformData = new StopInformData();
		stopInformData.setFirst(new BaseValue("很抱歉，接到医院通知，您预约的医生临时停诊，具体如下："));
		stopInformData.setHospitalname(new BaseValue(hospitalName));
		stopInformData.setDeptname(new BaseValue(deptName));
		stopInformData.setDoctorname(new BaseValue(doctorName));
		stopInformData.setPlanstarttime(new BaseValue(planStartTime));
		// remark参数
		String remarkValue = "就诊人：" + patientName + "\n\n您可预约其他医生，如已缴费，退款将在7个工作日内通过原支付渠道返还。";
		stopInformData.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(stopInformData);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 检查检验单模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doHospitalReport(String openId, String data) {
		logger.info("hospitalReport");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "hospitalReport")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("hospitalReport").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("hospitalReport", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "hospitalReport").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_report");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String test = JsonUtil.getValue(data, "report", String.class);
		String testdate = JsonUtil.getValue(data, "reportDate", String.class);
		String hospitalName = JsonUtil.getValue(data, "hospitalName", String.class);
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		// Data部分参数
		HospitalReportData hospitalReportData = new HospitalReportData();
		hospitalReportData.setFirst(new BaseValue("您好，您的报告单已出。"));
		hospitalReportData.setTest(new BaseValue(test));
		hospitalReportData.setTestdate(new BaseValue(testdate));
		// remark参数
		String remarkValue = "就诊人：" + patientName + "\n医院：" + hospitalName + "\n\n点击快速查看报告单内容。";
		hospitalReportData.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(hospitalReportData);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 用药提醒模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doMedicalInform(String openId, String data) {
		logger.info("medicalInform");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "medicalInform")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("medicalInform").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("medicalInform", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "medicalInform").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_medical");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		String messagePkValue = JsonUtil.getValue(data, "messagePkValue", String.class);

		String hcrmMsgType = JsonUtil.getValue(data, "hcrmMsgType", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&messagePkValue="+messagePkValue+"&hospitalId=" + hospitalId
				+ (CommonUtils.TextUtil.isNotEmpty(hcrmMsgType) ? ("&hcrmMsgType=" + hcrmMsgType) : "")
				+ "#/\",";
		// 模板消息入参
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		//获取当前时间作为用药时间
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		String remindTime = formatter.format(currentTime);
		// Data部分参数
		Data medicalInform = new Data();
		medicalInform.setFirst(new BaseValue("您好，您的用药时间到了！"));
		medicalInform.setKeyword1(new BaseValue(patientName));
		medicalInform.setKeyword2(new BaseValue(remindTime));
		// remark参数
		String remarkValue = "\n\n请及时查看详细的用药计划。";
		medicalInform.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(medicalInform);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 医嘱提醒模板
	 * @param openId
	 * @param data
	 * 推送内容
	 * @return respMsg
	 */
	@Override
	public String doDoctorInform(String openId, String data) {
		logger.info("doctorInform");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "doctorInform")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("doctorInform").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("doctorInform", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "doctorInform").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_doctor");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		String messagePkValue = JsonUtil.getValue(data, "messagePkValue", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&messagePkValue="+messagePkValue+"&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorName", String.class);
		String doctorTitle = JsonUtil.getValue(data, "doctorTitle", String.class);
		String hospitalName = JsonUtil.getValue(data, "hospitalName", String.class);
		String deptName = JsonUtil.getValue(data, "deptName", String.class);
		String illnessState = JsonUtil.getValue(data, "illnessState", String.class);
		// Data部分参数
		Data doctorInform = new Data();
		doctorInform.setFirst(new BaseValue(patientName + "，您好，医生针对您的病情，向您推送了一条医嘱提醒。"));
		doctorInform.setKeyword1(new BaseValue(doctorName));
		doctorInform.setKeyword2(new BaseValue(doctorTitle));
		doctorInform.setKeyword3(new BaseValue(hospitalName));
		doctorInform.setKeyword4(new BaseValue(deptName));
		// remark参数
		String remarkValue = "病情主诉： " + illnessState + "\n\n\n看看医生都说了什么吧。";
		doctorInform.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(doctorInform);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 病历提醒模板
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doMedicalRecordInform(String openId, String data) {
		logger.info("medicalRecordInform");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "medicalRecordInform")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("medicalRecordInform").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("medicalRecordInform", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "medicalRecordInform").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_medicalInform");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		String messagePkValue = JsonUtil.getValue(data, "messagePkValue", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&messagePkValue="+messagePkValue+"&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String type = JsonUtil.getValue(data, "type", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorName", String.class);
		String illnessState = JsonUtil.getValue(data, "illnessState", String.class);
		String hisPresentIll = JsonUtil.getValue(data, "hisPresentIll", String.class);
		String historyIll = JsonUtil.getValue(data, "historyIll", String.class);
		// Data部分参数
		Data medicalRecord = new Data();
		medicalRecord.setFirst(new BaseValue(patientName + "，您好，医生针对您的病历信息，向您推送了一条病历提醒。"));
		medicalRecord.setKeyword1(new BaseValue(type));
		medicalRecord.setKeyword2(new BaseValue(doctorName));
		medicalRecord.setKeyword3(new BaseValue(illnessState));
		medicalRecord.setKeyword4(new BaseValue(hisPresentIll));
		medicalRecord.setKeyword5(new BaseValue(historyIll));
		// remark参数
		String remarkValue = "\n\n为了您的健康，请及时查看。";
		medicalRecord.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(medicalRecord);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;

	}

	/**
	 * 病友圈检查检验单提醒
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doPatientReport(String openId, String data) {
		logger.info("patientReport");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "patientReport")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("patientReport").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("patientReport", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "patientReport").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_patientReport");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		String messagePkValue = JsonUtil.getValue(data, "messagePkValue", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&messagePkValue="+messagePkValue+"&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String projectName = JsonUtil.getValue(data, "projectName", String.class);
		String clinicTime = JsonUtil.getValue(data, "clinicTime", String.class);
		// Data部分参数
		Data patientReport = new Data();
		patientReport.setFirst(new BaseValue(patientName + "，您好，您的检验检查结果已出。"));
		patientReport.setKeyword1(new BaseValue(patientName));
		patientReport.setKeyword2(new BaseValue(projectName));
		patientReport.setKeyword3(new BaseValue(clinicTime));
		// remark参数
		String remarkValue = "\n\n为了您的健康，请及时查看。";
		patientReport.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(patientReport);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 每日清单
	 *
	 * @param openId
	 * @param data
	 *            推送内容
	 * @return respMsg
	 */
	@Override
	public String doDayList(String openId, String data) {
		logger.info("dayList");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "dayList")) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName("dayList").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("dayList", templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "dayList").getObjectValue();
		}
		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_dayList");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		String messagePkValue = JsonUtil.getValue(data, "messagePkValue", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=" + patientType + "&messagePkValue="+messagePkValue+"&hospitalId=" + hospitalId + "#/\",";
		// 模板消息入参
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String inpatientNum = JsonUtil.getValue(data, "inpatientNum", String.class);
		String listTime = JsonUtil.getValue(data, "listTime", String.class);
		// Data部分参数
		Data dayList = new Data();
		dayList.setFirst(new BaseValue(patientName + "，您好，最新的住院费用-每日清单已更新。"));
		dayList.setKeyword1(new BaseValue(inpatientNum));
		dayList.setKeyword2(new BaseValue(patientName));
		dayList.setKeyword3(new BaseValue(listTime));
		// remark参数
		String remarkValue = "\n\n内含费用明细，请及时查看。";
		dayList.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(dayList);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 咨询提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doRemindOfConsultation(String openId, String data){
		logger.info("remindOfConsultation");
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "remindOfConsultation")) {
			logger.info("从数据库中取咨询提醒的TemplateId");
			templateId = templateIdDao.findTemplateIdByName("remindOfConsultation").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("remindOfConsultation", templateId));
		} else {
			logger.info("从服务器缓存取咨询提醒的TemplateId");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "remindOfConsultation").getObjectValue();
		}

		// 点击详情页需要传给前端的参数
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty("wx_forward_remindOfConsultation");
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);
		String patientType = JsonUtil.getValue(data, "patientType", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		String deptCode = JsonUtil.getValue(data, "deptCode", String.class);
		String doctorCode = JsonUtil.getValue(data, "doctorCode", String.class);
		String deptName = JsonUtil.getValue(data, "deptName", String.class);
		String hospitalName = JsonUtil.getValue(data, "hospitalName", String.class);
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&businessType=6&userId=" + userId
				+ "&userVsId=" + userVsId + "&patientType=" + patientType +"&hospitalID=" + hospitalId
				+ "&deptCode=" + deptCode + "&doctorCode=" + doctorCode + "&deptName=" + URLEncoder.encode(deptName)
				+ "&hospitalName=" + URLEncoder.encode(hospitalName) + "#/" + wx_forward;

		Date currentTime = new Date();
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
	    String remindTime = formatter.format(currentTime);
		Data reminder = new Data();
		reminder.setFirst(new BaseValue("康复问题在线咨询，无需往返医院，省时省力更省心。"));
		reminder.setKeyword1(new BaseValue("您好，您在" + hospitalName + deptName + "就诊过，如果康复过程中有疑问或病情反复，可以在线咨询主治医生。"));
		reminder.setKeyword2(new BaseValue(remindTime));
		String remarkValue = "\n快去体验吧。";
		reminder.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(reminder);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);

		return respMessage;
	}


	/**
	 * 随访提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doFollowUp(String openId, String data){
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "followUp")) {
			logger.info("从数据库中取随访提醒的TemplateId");
			templateId = templateIdDao.findTemplateIdByName("followUp").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("followUp", templateId));
		} else {
			logger.info("从服务器缓存取随访提醒的TemplateId");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "followUp").getObjectValue();
		}

		String deptName = JsonUtil.getValue(data, "deptName", String.class);
		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String hospitalId = JsonUtil.getValue(data, "hospitalId", String.class);
		String hospitalName = JsonUtil.getValue(data, "hospitalName", String.class);
		String userId = JsonUtil.getValue(data, "userId", String.class);
		String userVsId = JsonUtil.getValue(data, "userVsId", String.class);

		String hcrmMsgType = JsonUtil.getValue(data, "hcrmMsgType", String.class);
		String url;

		url = properties.getProperty("url");
		// 点击详情进入医患聊天页面
		String wx_forward = properties.getProperty("wx_forward_followUp");
		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward + "&hospitalId=" + hospitalId
				+ "&userId=" + userId + "&userVsId=" + userVsId
				+ "&patientType=1&showAllRouter=1"
				+ (CommonUtils.TextUtil.isNotEmpty(hcrmMsgType) ? ("&hcrmMsgType=" + hcrmMsgType) : "")
				+"#/message-%3EMAIN_TAB";

		Date currentTime = new Date();
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
	    String remindTime = formatter.format(currentTime);
		Data reminder = new Data();
		reminder.setFirst(new BaseValue("您好，您有一份新的随访问卷需要反馈"));
		reminder.setKeyword1(new BaseValue(patientName));
		reminder.setKeyword2(new BaseValue(remindTime));
		reminder.setKeyword3(new BaseValue("" + hospitalName + "" + deptName + "患者回访单"));
		String remarkValue = "\n请点击详情反馈，" + hospitalName + "医院祝您身体健康。";
		reminder.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(reminder);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);

		return respMessage;
	}

	/**
	 * 任务提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doTaskReminder(String openId, String data){
		String templateId;
		if (!localCacheService.containsNode(CHCHE_NAME, "taskReminder")) {
			logger.info("从数据库中取任务提醒的TemplateId");
			templateId = templateIdDao.findTemplateIdByName("taskReminder").getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode("taskReminder", templateId));
		} else {
			logger.info("从服务器缓存取任务提醒的TemplateId");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, "taskReminder").getObjectValue();
		}

		String patientName = JsonUtil.getValue(data, "patientName", String.class);
		String event = JsonUtil.getValue(data, "event", String.class);
		String hospitalName = JsonUtil.getValue(data, "hospitalName", String.class);
		String state = JsonUtil.getValue(data, "state", String.class);
		String timeStamp = JsonUtil.getValue(data, "timeStamp", String.class);

		// 点击进入‘健康宣教列表’页面
		String url = properties.getProperty("YDYL_server_url"); // 移动医疗服务器域名
		url = url + "View/QyWeChatView/index.html"
				+ "?openId=" + openId + "&state=" + URLEncoder.encode(state) + "&timeStamp=" + timeStamp
				+ "#/edudetail";

		Date currentTime = new Date();
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    String remindTime = formatter.format(currentTime);
		Data task = new Data();
		task.setFirst(new BaseValue("您有来自医院的健康宣教如下："));
		task.setKeyword1(new BaseValue(hospitalName));
		task.setKeyword2(new BaseValue(patientName));
		task.setKeyword3(new BaseValue(event));
		task.setKeyword4(new BaseValue(remindTime));
		String remarkValue = "\n点击查看健康宣教详细内容，并仔细阅读。";
		task.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(task);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);

		return respMessage;
	}

	//任务号：XAQYJG-194   begin
	/**
	 * 抢号成功提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doRushSuccess(String openId, String data) {
		logger.info("rushSuccess");
		String templateId = getTemplateIdByName("rushSuccess");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","rushId","hospitalId"};
		String url = createDetailUrl("wx_forward_rushSuccess",openId,data,props);
		url = url.replace("hospitalId","hospitalID");
		url = url.replace("#","&isShowAllTab=1#");
		// 模板消息入参
		String withdrawingTime = (String) JsonUtil.getValue(data, "time", String.class);
		String hospitalName = JsonUtil.getValue(data, "hospitalName", String.class);
		String deptName = JsonUtil.getValue(data, "deptName", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorName", String.class);
		String planStartTime = JsonUtil.getValue(data, "regDate", String.class);
		// Data部分参数
		Data rushSuccess = new Data();
		rushSuccess.setFirst(new BaseValue("您好，您有一笔抢号记录处理成功。"));
		rushSuccess.setKeyword1(new BaseValue(withdrawingTime));
		rushSuccess.setKeyword2(new BaseValue(hospitalName));
		// remark参数
		String remarkValue = "科室：" + deptName + "\n医生：" + doctorName + "\n就诊时间：" + planStartTime +"\n\n小趣恭喜您使用自动抢号功能成功完成了预约挂号，请按时就医！";
		rushSuccess.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(rushSuccess);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 抢号失败提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doRushFail(String openId, String data) {
		logger.info("rushFail");
		String templateId = getTemplateIdByName("rushFail");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","rushId","hospitalId","addClinicType"};
		String url = createDetailUrl("wx_forward_rushDetail",openId,data,props);
		url = url.replace("hospitalId","hospitalID");
		url = url.replace("#","&isShowAllTab=1#");
		// 模板消息入参
		String withdrawingTime = (String) JsonUtil.getValue(data, "time", String.class);
		String hospitalName = JsonUtil.getValue(data, "hospitalName", String.class);
		String deptName = JsonUtil.getValue(data, "deptName", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorName", String.class);
		String failReason = JsonUtil.getValue(data, "handleMessage", String.class);
		// Data部分参数
		Data rushFail = new Data();
		rushFail.setFirst(new BaseValue("您好，您添加的抢号失败。"));
		rushFail.setKeyword1(new BaseValue(withdrawingTime));
		rushFail.setKeyword2(new BaseValue(hospitalName));
		// remark参数
		String remarkValue = "科室：" + deptName + "\n医生：" + doctorName + "\n失败原因：" + failReason +"\n\n请及时处理失败情况，以免延误您预约挂号！";
		rushFail.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(rushFail);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 抢号过期提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doRushOutDate(String openId, String data) {
		logger.info("rushOutDate");
		String templateId = getTemplateIdByName("rushOutDate");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","rushId","hospitalId","addClinicType"};
		String url = createDetailUrl("wx_forward_rushDetail",openId,data,props);
		url = url.replace("hospitalId","hospitalID");
		url = url.replace("#","&isShowAllTab=1#");
		// 模板消息入参
		String withdrawingTime = (String) JsonUtil.getValue(data, "time", String.class);
		String hospitalName = JsonUtil.getValue(data, "hospitalName", String.class);
		String deptName = JsonUtil.getValue(data, "deptName", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorName", String.class);
		// Data部分参数
		Data rushOutDate = new Data();
		rushOutDate.setFirst(new BaseValue("您好，您的抢号信息已过期。"));
		rushOutDate.setKeyword1(new BaseValue(withdrawingTime));
		rushOutDate.setKeyword2(new BaseValue(hospitalName));
		// remark参数
		String remarkValue = "科室：" + deptName + "\n医生：" + doctorName + "\n\n您可以尝试选择其他医生进行预约挂号，或者重新添加抢号！祝您预约挂号成功！";
		rushOutDate.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		// addClinicType==1 需要详情  addClinicType==0 不需要详情
		String addClinicType = JsonUtil.getValue(data, "addClinicType", String.class);
		if(addClinicType.equals("1")){
			commProperty.setUrl(url);
		}
		commProperty.setData(rushOutDate);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 有号提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doRushHasClinic(String openId, String data) {
		logger.info("rushHasClinic");
		String templateId = getTemplateIdByName("rushHasClinic");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","hospitalId","deptCode","deptName","doctorCode","doctorName"};
		String url = createDetailUrl("wx_forward_doctorIndex",openId,data,props);
		url = url.replace("hospitalId","hospitalID");
		url = url.replace("#","&isShowAllTab=1#");
		// 模板消息入参
		String withdrawingTime = (String) JsonUtil.getValue(data, "time", String.class);
		String hospitalName = JsonUtil.getValue(data, "hospitalName", String.class);
		String deptName = JsonUtil.getValue(data, "deptName", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorName", String.class);
		// Data部分参数
		Data rushHasClinic = new Data();
		rushHasClinic.setFirst(new BaseValue("您好，您关注的"+ doctorName +"医生有号了。"));
		rushHasClinic.setKeyword1(new BaseValue(withdrawingTime));
		rushHasClinic.setKeyword2(new BaseValue(hospitalName));
		// remark参数
		String remarkValue = "科室：" + deptName + "\n医生：" + doctorName + "\n\n请尽快查看该医生号源情况，预祝您预约挂号成功！";
		rushHasClinic.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(rushHasClinic);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 意见反馈处理通知
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doFeedbackRemind(String openId, String data) {
		logger.info("suggestWeiXinPush");
		String templateId = getTemplateIdByName("suggestWeiXinPush");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","hospitalId"};
		String url = createDetailUrl("wx_forward_feedback",openId,data,props);
		// 模板消息入参
		String reply = JsonUtil.getValue(data, "replyContext", String.class);
		String replyTime = (String) JsonUtil.getValue(data, "replyTime", String.class);
		// Data部分参数
		Data suggestWeiXinPush = new Data();
		suggestWeiXinPush.setFirst(new BaseValue("我们对您提出的建议反馈进行了回复。"));
		suggestWeiXinPush.setKeyword1(new BaseValue(reply));
		suggestWeiXinPush.setKeyword2(new BaseValue(replyTime));
		// remark参数
		String remarkValue = "\n\n感谢您对我们提出的宝贵建议！";
		suggestWeiXinPush.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(suggestWeiXinPush);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 就诊卡充值成功提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doRechargeSuccess(String openId, String data) {
		logger.info("patientRechargeSuccess");
		String templateId = getTemplateIdByName("rechargeSuccess");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","hospitalId"};
		String url = createDetailUrl("wx_forward_rechargeRecord",openId,data,props);
		// 模板消息入参
		String cardNumber = JsonUtil.getValue(data, "cardNo", String.class);
		String userName = JsonUtil.getValue(data, "patientName", String.class);
		String payTime = JsonUtil.getValue(data, "payTime", String.class);
		String rechargeAmount = JsonUtil.getValue(data, "rechargeAmount", String.class);
		String cardBalance = JsonUtil.getValue(data, "totalAmount", String.class);
		String outTradeNo = JsonUtil.getValue(data, "outTradeNo", String.class);
		String cardNumberTail = cardNumber.length()<4 ? cardNumber : cardNumber.substring(cardNumber.length()-4);
		// Data部分参数
		Data patientRechargeSuccess = new Data();
		patientRechargeSuccess.setFirst(new BaseValue("您好，尾号为"+ cardNumberTail +"的就诊卡已充值"+ rechargeAmount +"元。"));
		patientRechargeSuccess.setKeyword1(new BaseValue(outTradeNo));
		patientRechargeSuccess.setKeyword2(new BaseValue(payTime));
		// remark参数
		String remarkValue = "姓名：" + userName + "\n卡号：" + cardNumber + "\n充值金额：" + rechargeAmount ;
		if(cardBalance != null){
			remarkValue += "\n就诊卡余额：" + cardBalance;
		}
		remarkValue += "\n\n请前往财务处开取发票。详情可咨询客服：400-080-1010！";

		patientRechargeSuccess.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(patientRechargeSuccess);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 就诊卡充值失败提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doRechargeFail(String openId, String data) {
		logger.info("patientRechargeFalse");
		String templateId = getTemplateIdByName("rechargeFalse");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","hospitalId"};
		String url = createDetailUrl("wx_forward_rechargeRecord",openId,data,props);
		// 模板消息入参
		String cardNumber = JsonUtil.getValue(data, "cardNo", String.class);
		String userName = JsonUtil.getValue(data, "patientName", String.class);
		String rechargeAmount = JsonUtil.getValue(data, "rechargeAmount", String.class);
		// Data部分参数
		Data rechargeFalse = new Data();
		rechargeFalse.setFirst(new BaseValue("很抱歉，您的就诊卡充值失败。"));
		rechargeFalse.setKeyword1(new BaseValue("就诊卡充值"));		//业务名称
		rechargeFalse.setKeyword2(new BaseValue(userName));		//充值账户
		// remark参数
		String remarkValue = "卡号：" + cardNumber + "\n充值金额：" + rechargeAmount + "\n\n退款将于3个工作日内退还至您的支付账户，详情可咨询客服：400-080-1010！";
		rechargeFalse.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(rechargeFalse);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}
	//任务号：XAQYJG-194  end

	//任务号：XAQYJG-201  begin
	/**
	 * 超时未接诊提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doNoReception(String openId, String data) {
		logger.info("noReception");
		String templateId = getTemplateIdByName("noReception");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","hospitalId"};
		String url = createDetailUrl("wx_forward_consultOrderDetail",openId,data,props);

		String sourceId = JsonUtil.getValue(data, "sourceId", String.class);
		String consultOrderId = sourceId.replace("noReception","");
		url = url.replace("#","&consultOrderID=" + consultOrderId + "#");

		// 模板消息入参
		String consultType = JsonUtil.getValue(data, "consultType", String.class);
		//获取当前时间
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String remindTime = sdf.format(date);
		// Data部分参数
		Data noReception = new Data();
		noReception.setFirst(new BaseValue("医生未接诊"));
		noReception.setKeyword1(new BaseValue("您的"+consultType+"咨询申请医生未接诊，订单超时关闭，医生可能正在忙，请稍后重新发起咨询。"));
		noReception.setKeyword2(new BaseValue(remindTime));
		// remark参数
		String remarkValue = "\n\n如果订单已产生费用，系统将为您原路退还，感谢使用趣医网。";
		noReception.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(noReception);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 待支付提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doPayRemind(String openId, String data) {
		logger.info("payRemind");
		String templateId = getTemplateIdByName("payRemind");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","hospitalId","payDueDate","doctorName","payAmount","orderNo","acceptDueTime"};
		String url = createDetailUrl("wx_forward_consultPay",openId,data,props);

		String sourceId = JsonUtil.getValue(data, "sourceId", String.class);
		String consultOrderId = sourceId.replace("payRemind","");
		url = url.replace("#","&consultOrderID=" + consultOrderId + "#");
		// 模板消息入参
		String consultType = JsonUtil.getValue(data, "consultType", String.class);
		String deadline = JsonUtil.getValue(data, "payDueDate", String.class);

		//获取当前时间
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String remindTime = sdf.format(date);
		// Data部分参数
		Data payRemind = new Data();
		payRemind.setFirst(new BaseValue("您有一笔待支付订单"));
		payRemind.setKeyword1(new BaseValue("您有一笔"+consultType+"咨询订单还未支付，请于"+deadline+"前完成支付，超时订单自动关闭。"));
		payRemind.setKeyword2(new BaseValue(remindTime));
		// remark参数
		String remarkValue = "\n\n感谢使用趣医网。";
		payRemind.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(payRemind);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 医生接诊提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doReceptionRemind(String openId, String data) {
		logger.info("receptionRemind");
		String templateId = getTemplateIdByName("receptionRemind");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","hospitalId"};
		String url = createDetailUrl("wx_forward_hasConsulted",openId,data,props);

		String sourceId = JsonUtil.getValue(data, "sourceId", String.class);
		String consultOrderId = sourceId.replace("receptionRemind","");
		url = url.replace("#","&consultOrderID=" + consultOrderId + "#");
		// 模板消息入参
		String consultType = JsonUtil.getValue(data, "consultType", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorName", String.class);
		String isShare = JsonUtil.getValue(data, "isShare", String.class);

		String remindContent = "";
		String title = "";
		//如果是抢单
		if("1".equals(isShare)){
			title = "同科医生已接诊";
			String shareDoctorName = JsonUtil.getValue(data, "shareDoctorName", String.class);
			if(consultType.equals("图文")){
				remindContent = "您申请咨询的"+ doctorName +"医生较忙，同科室的"+ shareDoctorName +"医生已经代为接诊，请尽快登录趣医院APP或趣医微信公众号与医生进行交流。";
			}else if(consultType.equals("电话")){
				remindContent = "您申请咨询的"+ doctorName +"医生较忙，同科室的"+ shareDoctorName +"医生已经代为接诊，请保持手机畅通，方便医生向您回拨电话。";
			}else if(consultType.equals("视频")){
				remindContent = "您申请咨询的"+ doctorName +"医生较忙，同科室的"+ shareDoctorName +"医生已经代为接诊，请保持趣医院APP的登录状态，方便医生向您回拨视频。";
			}
		}else{//非抢单
			title = "医生已接诊";
			if(consultType.equals("图文")){
				remindContent = "您申请咨询的"+ doctorName +"医生已经接诊，请尽快登录趣医院APP或趣医微信公众号与医生进行交流。";
			}else if(consultType.equals("电话")){
				remindContent = "您申请咨询的"+ doctorName +"医生已经接诊，请保持手机畅通，方便医生向您回拨电话。";
			}else if(consultType.equals("视频")){
				remindContent = "您申请咨询的"+ doctorName +"医生已经接诊，请保持趣医院APP的登录状态，方便医生向您回拨视频。";
			}
		}


		//获取当前时间
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String remindTime = sdf.format(date);
		// Data部分参数
		Data receptionRemind = new Data();
		receptionRemind.setFirst(new BaseValue(title));
		receptionRemind.setKeyword1(new BaseValue(remindContent));
		receptionRemind.setKeyword2(new BaseValue(remindTime));
		// remark参数
		String remarkValue = "\n\n趣医网祝您身体健康。";
		receptionRemind.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(receptionRemind);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 医生驳回提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doRejectRemind(String openId, String data) {
		logger.info("rejectRemind");
		String templateId = getTemplateIdByName("rejectRemind");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","hospitalId"};
		String url = createDetailUrl("wx_forward_consultOrderDetail",openId,data,props);

		String sourceId = JsonUtil.getValue(data, "sourceId", String.class);
		String consultOrderId = sourceId.replace("rejectRemind","");
		url = url.replace("#","&consultOrderID=" + consultOrderId + "#");
		// 模板消息入参
		String rejectReason = JsonUtil.getValue(data, "rejectReason", String.class);
		String doctorName = JsonUtil.getValue(data, "doctorName", String.class);
		String remindContent = "您申请咨询的"+doctorName+"医生驳回了您的订单";
		if(CommonUtils.TextUtil.isNotEmpty(rejectReason)) {
			remindContent += "，驳回原因：" + rejectReason;
		}
		remindContent += "，请重新咨询。";
		//获取当前时间
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String remindTime = sdf.format(date);
		// Data部分参数
		Data rejectRemind = new Data();
		rejectRemind.setFirst(new BaseValue("医生驳回订单"));
		rejectRemind.setKeyword1(new BaseValue(remindContent));
		rejectRemind.setKeyword2(new BaseValue(remindTime));
		// remark参数
		String remarkValue = "\n\n如果订单已产生费用，系统将为您原路退还，感谢使用趣医网。";
		rejectRemind.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(rejectRemind);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 咨询快到约定时间提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doConsultRemind(String openId, String data) {
		logger.info("consultRemind");
		String templateId = getTemplateIdByName("consultRemind");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","hospitalId"};
		String url = createDetailUrl("wx_forward_hasConsulted",openId,data,props);

		String sourceId = JsonUtil.getValue(data, "sourceId", String.class);
		String consultOrderId = sourceId.replace("consultRemind","");
		url = url.replace("#","&consultOrderID=" + consultOrderId + "#");
		// 模板消息入参
		String consultType = JsonUtil.getValue(data, "consultType", String.class);
		String remindContent = "";
		if(consultType.equals("电话")){
			remindContent = "您与医生约定的电话咨询时间快到了，请保持手机畅通。";
		}else if(consultType.equals("视频")){
			remindContent = "您与医生约定的视频咨询时间快到了，请提前登录趣医院APP。";
		}

		//获取当前时间
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String remindTime = sdf.format(date);
		// Data部分参数
		Data consultRemind = new Data();

		if(consultType.equals("电话")){
			consultRemind.setFirst(new BaseValue("电话咨询提醒"));
		}else if(consultType.equals("视频")){
			consultRemind.setFirst(new BaseValue("视频咨询提醒"));
		}
		consultRemind.setKeyword1(new BaseValue(remindContent));
		consultRemind.setKeyword2(new BaseValue(remindTime));
		// remark参数
		String remarkValue = "\n\n感谢使用趣医网。";
		consultRemind.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(consultRemind);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	/**
	 * 咨询完成提醒
	 *
	 * @param openId
	 * @param data 推送内容
	 * @return respMsg
	 */
	@Override
	public String doConsultFinish(String openId, String data) {
		logger.info("consultFinish");
		String templateId = getTemplateIdByName("consultFinish");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","hospitalId","orderNo","payDueDate"};
		String url = createDetailUrl("wx_forward_consultSatisfaction",openId,data,props);

		String sourceId = JsonUtil.getValue(data, "sourceId", String.class);
		String consultOrderId = sourceId.replace("consultFinish","");
		url = url.replace("#","&consultOrderID=" + consultOrderId + "#");
		// 模板消息入参
		String consultType = JsonUtil.getValue(data, "consultType", String.class);
		String remindContent = "您的"+ consultType +"咨询订单已经完成，别忘了评价医生哦。";

		//获取当前时间
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String remindTime = sdf.format(date);
		// Data部分参数
		Data consultFinish = new Data();
		consultFinish.setFirst(new BaseValue("咨询订单已完成"));
		consultFinish.setKeyword1(new BaseValue(remindContent));
		consultFinish.setKeyword2(new BaseValue(remindTime));
		// remark参数
		String remarkValue = "\n\n感谢使用趣医网。";
		consultFinish.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(consultFinish);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	@Override
	public String doVideoFinish(String openId, String data) {
		logger.info("videoFinish");
		String templateId = getTemplateIdByName("consultFinish");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","hospitalId","regId"};
		String url = createDetailUrl("wx_forward_videoInterrogation",openId,data,props);

		// 模板消息入参
		String remindContent = "您的视频问诊订单已经完成，别忘了进行评价哦。";

		//获取当前时间
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String remindTime = sdf.format(date);
		// Data部分参数
		Data consultFinish = new Data();
		consultFinish.setFirst(new BaseValue("咨询订单已完成"));
		consultFinish.setKeyword1(new BaseValue(remindContent));
		consultFinish.setKeyword2(new BaseValue(remindTime));
		// remark参数
		String remarkValue = "\n\n感谢使用趣医网。";
		consultFinish.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(consultFinish);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}

	@Override
	public String doOnlinePreFinish(String openId, String data) {
		logger.info("onlinePreFinish");
		String templateId = getTemplateIdByName("consultFinish");
		// 生成详情页跳转url
		String[] props = {"userId","userVsId","patientType","hospitalId","regId"};
		String url = createDetailUrl("wx_forward_purchaseMedince",openId,data,props);

		// 模板消息入参
		String remindContent = "您的购药开单订单已经完成，别忘了进行评价哦。";

		//获取当前时间
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String remindTime = sdf.format(date);
		// Data部分参数
		Data consultFinish = new Data();
		consultFinish.setFirst(new BaseValue("咨询订单已完成"));
		consultFinish.setKeyword1(new BaseValue(remindContent));
		consultFinish.setKeyword2(new BaseValue(remindTime));
		// remark参数
		String remarkValue = "\n\n感谢使用趣医网。";
		consultFinish.setRemark(new BaseValue(remarkValue));
		// 公有参数
		CommProperty commProperty = new CommProperty();
		commProperty.setTemplateId(templateId);
		commProperty.setTouser(openId);
		commProperty.setUrl(url);
		commProperty.setData(consultFinish);
		String msgJson = JsonUtil.getGson().toJson(commProperty);
		logger.info("Json is: " + msgJson);
		String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
		String respMessage = doSendTemplateMsg(msgJson, accessToken);
		return respMessage;
	}
	//任务号：XAQYJG-201  end




	/**
	 * 调用微信接口根据openId向用户推送模板消息
	 *
	 * @param params
	 *            模板所需参数及消息内容
	 * @param accessToken
	 * @return respMsg
	 */
	@Override
	public String doSendTemplateMsg(String params, String accessToken) {
		String jsonStr;
		try {
			jsonStr = HttpProxy.httpPost(
					"https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken, params);
			logger.info(jsonStr);
			Map<String, Object> respValue = JsonUtil.json2Map(jsonStr);
			if (!(respValue.get("errmsg").toString().equals("ok"))) {
				throw new IOException();
			}
			return jsonStr;
		} catch (IOException e) {
			logger.error("模板消息推送失败");
			return null;
		}
	}

	/**
	 * 根据模板名获取对应的模板ID
	 *
	 * @param templateName 模板名
	 * @return templateId  模板ID
	 */
	public String getTemplateIdByName(String templateName) {
		String templateId = "";
		if (!localCacheService.containsNode(CHCHE_NAME, templateName)) {
			logger.info("服务器缓存失效，现从数据库中取TemplateId");
			templateId = templateIdDao.findTemplateIdByName(templateName).getTemplateId();
			localCacheService.putOrUpdateNode(CHCHE_NAME, new CacheNode(templateName, templateId));
		} else {
			logger.info("服务器缓存TemplateId有效");
			templateId = (String) localCacheService.getNode(CHCHE_NAME, templateName).getObjectValue();
		}
		return templateId;
	}

	/**
	 * 创建点击详情对应跳转的前端url
	 *
	 * @param  forward  wx_forward 决定要跳转的前端页面
	 * @param  openId   用户的openId
	 * @param  data	   后台传来的参数
	 * @param  props	   需要传给前端页面的参数名
	 * @return url
	 */
	public String createDetailUrl(String forward, String openId, String data, String[] props){
		String url = properties.getProperty("url");
		String wx_forward = properties.getProperty(forward);

		url = url + "&openId=" + openId + "&wx_forward=" + wx_forward;
		//遍历参数，进行拼接
		for(String prop:props){
			url = url + "&" + prop + "=" + JsonUtil.getValue(data, prop, String.class);
		}
		url = url + "#/\",";

		return url;
	}

	/**
	 * @desc 获取不通情况下的token
	 * @desc 根据是否传appid来校验 给趣医公众号发消息还是给个性化公众号发送消息
	 * @author gouchaohui
	 * @date 2017年11月15日14:53:27
	 * @param data
	 * @return
	 */
	public String getAccessTokenByAppId(String data){
		String appId = JsonUtil.getValue(data, "appId", String.class);
		logger.info("获取当前是否传appId字段:"+appId);
		if(!StringUtils.isEmpty(appId)){
			String auAccessToken=openThirdService.doGetAuthorizerAccessTokenSingle(appId);
			logger.info("获取当前AuthorizerAccessToken:"+auAccessToken+" appId:"+appId);
			if(StringUtils.isEmpty(auAccessToken)){
				logger.error("获取AuthorizerAccessToken失败 appid:"+appId);
				return "";
			}
		}
		return accessTokenService.doGetAccessToken().getAccessToken();
	}
}
