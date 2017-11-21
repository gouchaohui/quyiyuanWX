package com.quyiyuan.weichat.modules.templatemessage.service;


import com.kyee.nextframework.core.base.service.IBaseService;
import com.quyiyuan.weichat.modules.templatemessage.domain.TemplateId;
import com.quyiyuan.weichat.modules.templatemessage.protocol.TemplateMessageProtocolOut;

public interface ITemplateMsgService extends IBaseService<TemplateId, String> {
	TemplateMessageProtocolOut doChooseTemplateMsg(String openId, String msgType, String data);
	String doAppointSuccess(String openId, String data);
	String doAppointFailed(String openId, String data);
	String doRegisterFailed(String openId, String data);
	String doRegisterReminder(String openId, String data);
	String doCancelAppointSuccess(String openId, String data);
	String doCancelAppointFailed(String openId, String data);
	String doTransferAppointSuccess(String openId, String data);
	String doTransferAppointFailed(String openId, String data);
	String doAppointInform(String openId, String data);
	String doStopInform(String openId, String data);
	String doHospitalReport(String openId, String data);
	String doMedicalInform(String openId, String data);
	String doDoctorInform(String openId,String data);
	String doMedicalRecordInform(String openId,String data);
	String doPatientReport(String openId,String data);
	String doDayList(String openId,String data);
	String doRemindOfConsultation(String openId, String data);
	String doTaskReminder(String openId, String data);
	String doFollowUp(String openId, String data);
	String doSendTemplateMsg(String params, String accessToken);
	//任务号：XAQYJG-194  begin
	String doRushSuccess(String openId, String data);
	String doRushFail(String openId, String data);
	String doRushOutDate(String openId, String data);
	String doRushHasClinic(String openId, String data);
	String doFeedbackRemind(String openId, String data);
	String doRechargeSuccess(String openId, String data);
	String doRechargeFail(String openId, String data);
	//任务号：XAQYJG-201  begin
	String doNoReception(String openId, String data);
	String doPayRemind(String openId, String data);
	String doReceptionRemind(String openId, String data);
	String doRejectRemind(String openId, String data);
	String doConsultRemind(String openId, String data);
	String doConsultFinish(String openId, String data);
	String doVideoFinish(String openId, String data);
	String doOnlinePreFinish(String openId, String data);
}
