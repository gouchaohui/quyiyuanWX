package com.quyiyuan.weichat.modules.baseservice.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import com.quyiyuan.weichat.modules.baseservice.beans.*;
import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kyee.nextframework.core.base.domain.internal.EmptyDomain;
import com.kyee.nextframework.core.common.utils.CommonUtils.FileUtil;
import com.kyee.nextframework.core.common.utils.CommonUtils.JsonUtil;
import com.kyee.nextframework.core.common.utils.CommonUtils.TextUtil;
import com.quyiyuan.weichat.comm.EncryptUtil;
import com.quyiyuan.weichat.comm.HttpProxy;
import com.quyiyuan.weichat.comm.MenuConfig;
import com.quyiyuan.weichat.comm.MessageUtil;
import com.quyiyuan.weichat.comm.MsgPropertiesController;
import com.quyiyuan.weichat.comm.service.impl.WxBaseService;
import com.quyiyuan.weichat.modules.accesstoken.service.IAccessTokenService;
import com.quyiyuan.weichat.modules.baseservice.service.ICoreService;

@Service("coreService")
public class CoreService extends WxBaseService<EmptyDomain, Serializable> implements ICoreService {
	@Autowired
	@Qualifier("accessTokenService")
	private IAccessTokenService accessTokenService;

	@Autowired
	@Qualifier("QRcodeService")
	private QRcodeService qRcodeService;

	private String serverAddress;
	private String appWebUrl;  // app网页版地址
	private Properties properties;
	private static String jsapi_ticket = "";
	private static Double jsapi_expires_in = 7200.0;
	private static long jsapi_getTime = 0;
	public static String nonce_str = "";
	public static String timestamp = "";
	// 直接回复success，微信服务器不做任何处理
	private String NO_RESPONSE = "success";

	@PostConstruct
	public void coreServiceInit() {
		properties = new Properties();
		InputStream path = this.getClass().getClassLoader().getResourceAsStream("system-config.properties");
		try {
			properties.load(path);
			serverAddress = properties.getProperty("server_address");
			// 从配置文件获取app网页版地址
			appWebUrl = properties.getProperty("app_web_url");
		} catch (IOException e) {
			logger.info("加载system-config配置文件出错");
			e.printStackTrace();
		}
	}

	/**
	 * 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
	 * 
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @return Boolean
	 */
	@Override
	public boolean checkSignature(String signature, String timestamp, String nonce, String Token) {
		String[] arr = new String[] { Token, timestamp, nonce };
		// 将token、timestamp、nonce三个参数进行字典序排序
		try {
			Arrays.sort(arr);
		} catch (Exception e) {
			logger.error(e);
		}
		
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			content.append(arr[i]);
		}
		MessageDigest md = null;
		String tmpStr = null;

		try {
			md = MessageDigest.getInstance("SHA-1");
			// 将三个参数字符串拼接成一个字符串进行sha1加密
			byte[] digest = md.digest(content.toString().getBytes());
			tmpStr = byteToStr(digest);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e);
		}
		content = null;
		// 将sha1加密后的字符串可与signature对比，标识该请求来源于微信
		return tmpStr != null ? tmpStr.equals(signature.toUpperCase()) : false;
	}

	/**
	 * 按照请求中的msgType来处理消息
	 * 
	 * @return String
	 */
	@Override
	public String processRequest(HttpServletRequest request) {
		logger.info("processRequest");
		String respMessage = null;
		try {
			// 默认返回的文本消息内容
			String respContent = "请求处理异常，请稍后尝试";
			Map<String, String> requestMap = MessageUtil.parseXml(request);
			// 发送方账号（openid）
			String fromUserName = requestMap.get("FromUserName");
			// 公众帐号
			String toUserName = requestMap.get("ToUserName");
			// 消息类型
			String msgType = requestMap.get("MsgType");

			// 文本消息
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
				String content = requestMap.get("Content");
				if (MsgPropertiesController.map.get(content) != null
						&& !"".equals(MsgPropertiesController.map.get(content))) {
					// 自动回复文本消息
					respContent = MsgPropertiesController.map.get(content);
					TextMessage textMsg = new TextMessage(toUserName, fromUserName, new Date().getTime(), respContent,
							0);
					respMessage = MessageUtil.textMessageToXml(textMsg);
				} else {// 除了message.properties 文件里 的自动回复信息之外都接入人工客服
					if ("1".equals(content) || "2".equals(content) || "3".equals(content) || "4".equals(content)) {
						NewsMessage newMessage = MessageUtil.getNewsMessage(content, fromUserName, toUserName);
						respMessage = MessageUtil.newsMessageToXml(newMessage);
					} else {
						CustomerServiceMsg cusMsg = new CustomerServiceMsg();
						cusMsg.setToUserName(fromUserName);
						cusMsg.setFromUserName(toUserName);
						cusMsg.setCreateTime(new Date().getTime());
						cusMsg.setMsgType("transfer_customer_service");
						respMessage = MessageUtil.cusMsgToXml(cusMsg);
					}
				}
			}
			// 图片消息、音频消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)
					|| msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
				// 图片消息、音频消息都接入人工客服
				CustomerServiceMsg cusMsg = new CustomerServiceMsg();
				cusMsg.setToUserName(fromUserName);
				cusMsg.setFromUserName(toUserName);
				cusMsg.setCreateTime(new Date().getTime());
				cusMsg.setMsgType("transfer_customer_service");
				respMessage = MessageUtil.cusMsgToXml(cusMsg);
			}
			// 地理位置消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
				respMessage = "";
			}
			// 链接消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
				respMessage = "";
			}
			// 事件推送
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
				// 事件类型
				String eventType = requestMap.get("Event");
				logger.info("eventType:" + eventType);
				// 订阅
				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
					String qrContent = requestMap.get("EventKey");
					// 带参数二维码关注
					if (TextUtil.isNotEmpty(qrContent)) {
						String qrValue = (String) qrContent.substring(8);
						String uuid = (String) qrValue.substring(0, qrValue.indexOf(","));
						String messageType = (String) qrValue.substring(qrValue.indexOf(",") + 1);
						logger.info("UUID为：" + uuid + " messageType为：" + messageType);
						List<Article> articles = qRcodeService.handleQRcodeWithArg(eventType, messageType, uuid,
								fromUserName);
						if(articles.size() > 0){
							NewsMessage newsMessage = new NewsMessage(toUserName, fromUserName, new Date().getTime(),
									articles);
							respMessage = MessageUtil.newsMessageToXml(newsMessage);
						}else{
							String content = getWelcomeMsg();
							TextMessage textMessage = new TextMessage(toUserName, fromUserName, new Date().getTime(),
									content, 0);
							respMessage = MessageUtil.textMessageToXml(textMessage);
						}
					}
					// 普通二维码关注，推送文本信息
					else {
						// 回复文本消息
						String content = getWelcomeMsg();
						TextMessage textMessage = new TextMessage(toUserName, fromUserName, new Date().getTime(),
								content, 0);
						respMessage = MessageUtil.textMessageToXml(textMessage);
					}
				}

				// 再次扫描关注二维码，扫描事件
				if (eventType.equals(MessageUtil.EVENT_TYPE_SCAN)) {
					// 判断是否为带参二维码
					String qrContent = requestMap.get("EventKey");
					// 无参二维码回复文本信息
					if (TextUtil.isEmpty(qrContent)) {
						String content = MsgPropertiesController.map.get("EVENT_TYPE_SUBSCRIBE_MESSAGE");
						// 替换链接中的openId
						respContent = content.replaceAll("openidvalue", fromUserName);
						TextMessage textMessage = new TextMessage(toUserName, fromUserName, new Date().getTime(),
								respContent, 0);
						respMessage = MessageUtil.textMessageToXml(textMessage);
					} else {
						String uuid = qrContent.substring(0, qrContent.indexOf(","));
						String messageType = (String) qrContent.substring(qrContent.indexOf(",") + 1);

						//TODO
						List<Article> articles = qRcodeService.handleQRcodeWithArg(eventType, messageType, uuid, fromUserName);
						if(articles.size() > 0){
							NewsMessage newsMessage = new NewsMessage(toUserName, fromUserName, new Date().getTime(),articles);
							respMessage = MessageUtil.newsMessageToXml(newsMessage);
						}else {
							respMessage = NO_RESPONSE;
						}
					}
				}

				// 取消订阅
				else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
					// TODO 取消订阅后用户再收不到公众号发送的消息，因此不需要回复消息
				}
				// 自定义菜单点击事件
				else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
					// TODO 自定义菜单权没有开放，暂不处理该类消息
					String eventKey = requestMap.get("EventKey");
					logger.info("eventKey:" + eventKey);
					if (MenuConfig.ONLINE_CONSULT_KEY.equals(eventKey)) {
						// 回复文本消息
						respContent = MsgPropertiesController.map.get("ONLINE_CONSULT_MESSAGE");
						;
						TextMessage textMessage = new TextMessage(toUserName, fromUserName, new Date().getTime(),
								respContent, 0);
						respMessage = MessageUtil.textMessageToXml(textMessage);
					} else if (MenuConfig.PRICE_ACTIVITY_KAY.equals(eventKey)) {
						// 回复文本消息
						respContent = MsgPropertiesController.map.get("PRICE_ACTIVITY_MESSAGE");
						TextMessage textMessage = new TextMessage(toUserName, fromUserName, new Date().getTime(),
								respContent, 0);
						respMessage = MessageUtil.textMessageToXml(textMessage);

					} else if (eventKey.equals("download")) {// 没开发的功能提示统一设置
						logger.info("进入download事件开始选择");
						respContent = "亲，请根据您的手机系统下载APP。\n\n"
								+ "IOS：<a href=\"https://itunes.apple.com/cn/app/qu-yi-yuan/id919915695&mt=8\">点击下载</a>\n\n"
								+ "Andorid：<a href=\"" + MenuConfig.QUYIYUAN_DOWNLOAD_URL + "\">点击下载</a>";
						TextMessage textMessage = new TextMessage(toUserName, fromUserName, new Date().getTime(),
								respContent, 0);
						respMessage = MessageUtil.textMessageToXml(textMessage);
					} else {
						// HLogger.info("其他点击事件");
						respContent = "广告合作：bd@quyiyuan.com";
						TextMessage textMessage = new TextMessage(toUserName, fromUserName, new Date().getTime(),
								respContent, 0);
						respMessage = MessageUtil.textMessageToXml(textMessage);
					}

				} else if (eventType.equals(MessageUtil.EVENT_TYPE_LOCATION)) {
					// 获取用户地理位置事件
					logger.info(MessageUtil.EVENT_TYPE_LOCATION);
					String latitude = requestMap.get("Latitude"); // 纬度
					String longitude = requestMap.get("Longitude"); // 经度
					respMessage = "";
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info(respMessage);
		return respMessage;
	}

	/**
	 * 将字节数组转换为十六进制字符串
	 * 
	 * @param byteArray
	 * @return String
	 */
	private static String byteToStr(byte[] byteArray) {
		String strDigest = "";
		for (int i = 0; i < byteArray.length; i++) {
			strDigest += byteToHexStr(byteArray[i]);
		}
		return strDigest;
	}

	/**
	 * 将字节转换为十六进制字符串
	 * 
	 * @param mByte
	 * @return String
	 */
	private static String byteToHexStr(byte mByte) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] tempArr = new char[2];
		tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
		tempArr[1] = Digit[mByte & 0X0F];

		String s = new String(tempArr);
		return s;
	}

	/**
	 * 创建菜单接口
	 * 
	 * @return String
	 */
	@Override
	public String doCreateMenu(Boolean testFlag) {
		try {
			// 读取配置文件 KYEEAPPC-10727 begin
			Properties configFile = FileUtil.loadClassPathProperties("system-config.properties");
			// KYEEAPPC-10727 end
			Gson gson = new GsonBuilder().disableHtmlEscaping().create();
			String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
			logger.info(accessToken);
			
			// 挂号.咨询 一级菜单
			SubButton appSubButton = new SubButton();
			List<CommProperty> appointSub = new ArrayList<>();
			appSubButton.setName("挂号·咨询");
			
			CommProperty appoint = new CommProperty();
			appoint.setName("预约挂号");
			appoint.setType("view");
			appoint.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
					+ "&redirect_uri=http%3a%2f%2f" + serverAddress
					+ "%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=index#wechat_redirect/");
			appointSub.add(appoint);
			
			CommProperty consultDoctor = new CommProperty();
			consultDoctor.setName("咨询医生");
			consultDoctor.setType("view");
			consultDoctor.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
					+ "&redirect_uri=http%3a%2f%2f" + serverAddress
					+ "%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=consultDoctorList#wechat_redirect/");
			appointSub.add(consultDoctor);
			
			CommProperty nurse = new CommProperty();
			nurse.setName("护士陪诊");
			nurse.setType("view");
			nurse.setUrl(configFile.getProperty("nurse"));
			appointSub.add(nurse);
			
			CommProperty queue = new CommProperty();
			queue.setName("叫号查询");
			queue.setType("view");
			queue.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
					+ "&redirect_uri=http%3a%2f%2f" + serverAddress
					+ "%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=queue#wechat_redirect/");
			appointSub.add(queue);
			
			CommProperty symptom = new CommProperty();
			symptom.setName("智能导诊");
			symptom.setType("view"); 
			symptom.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
					+ "&redirect_uri=http%3a%2f%2f" + serverAddress
					+ "%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=triageMain#wechat_redirect/");
			appointSub.add(symptom);
			
			appSubButton.setSub_button(appointSub);
			
			// 查询·缴费 一级菜单
			SubButton query = new SubButton();
			List<CommProperty> queryList = new ArrayList<>();
			query.setName("查询·缴费");
			
			CommProperty medRecord = new CommProperty();
			medRecord.setName("预约记录");
			medRecord.setType("view");
			medRecord.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
					+ "&redirect_uri=http%3a%2f%2f" + serverAddress
					+ "%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=medicalGuide#wechat_redirect");
			queryList.add(medRecord);

			CommProperty report = new CommProperty();
			report.setName("报告单查询");
			report.setType("view");
			report.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
					+ "&redirect_uri=http%3a%2f%2f" + serverAddress
					+ "%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=report_multiple#wechat_redirect");
			queryList.add(report);
			
			CommProperty patientCard = new CommProperty();
			patientCard.setName("就诊卡充值");
			patientCard.setType("view");
			patientCard.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
					+ "&redirect_uri=http%3a%2f%2f" + serverAddress
					+ "%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=patient_card_recharge#wechat_redirect");
			queryList.add(patientCard);
			
			CommProperty outPatient = new CommProperty();
			outPatient.setName("门诊缴费");
			outPatient.setType("view");
			outPatient.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
					+ "&redirect_uri=http%3a%2f%2f" + serverAddress
					+ "%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=clinic_payment_revise#wechat_redirect");
			queryList.add(outPatient);
			
			CommProperty inPatient = new CommProperty();
			inPatient.setName("住院缴费");
			inPatient.setType("view");
			inPatient.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
					+ "&redirect_uri=http%3a%2f%2f" + serverAddress
					+ "%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=inpatient_general#wechat_redirect");	
			queryList.add(inPatient);
			
			query.setSub_button(queryList);
			
			// 健康服务 一级菜单
			SubButton healthServe = new SubButton();
			List<CommProperty> healthList = new ArrayList<>();
			healthServe.setName("健康服务");

			CommProperty consultOrder = new CommProperty();
			consultOrder.setName("咨询订单");
			consultOrder.setType("view");
			consultOrder.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
					+ "&redirect_uri=http%3a%2f%2f" + serverAddress
					+ "%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=consult_order#wechat_redirect/");
			healthList.add(consultOrder);

			CommProperty insurance = new CommProperty();
			insurance.setName("健康保险");
			insurance.setType("view");
			insurance.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
					+ "&redirect_uri=http%3a%2f%2f" + serverAddress
					+ "%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=insurance#wechat_redirect/");
			healthList.add(insurance);

			CommProperty healthEducation = new CommProperty();
			healthEducation.setName("健康教育");
			healthEducation.setType("view");
			healthEducation.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
					+ "&redirect_uri=http%3a%2f%2f" + serverAddress
					+ "%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=health_education#wechat_redirect");
			healthList.add(healthEducation);

			//健康咨询已切换至小程序
			MiniProgram healthConsult = new MiniProgram();
			healthConsult.setName("健康资讯");
			healthConsult.setUrl(configFile.getProperty("healthConsult"));
			//如果是测试环境
			if(testFlag){
				healthConsult.setType("view");
			}else{
				healthConsult.setType("miniprogram");
				healthConsult.setAppid("wxe4cae03bfd3782cd");
				healthConsult.setPagepath("pages/healthInfoList/healthInfoList");
			}
			healthList.add(healthConsult);
			
			CommProperty download = new CommProperty();
			download.setName("APP下载");
			download.setType("view");
			download.setUrl(configFile.getProperty("download"));
			healthList.add(download);
			
			healthServe.setSub_button(healthList);

			// 主按钮
			BaseButton baseButton = new BaseButton();
			List<SubButton> baseList = new ArrayList<>();
			baseList.add(appSubButton);
			baseList.add(query);
			baseList.add(healthServe);
			baseButton.setButton(baseList);

			String msgJson = gson.toJson(baseButton);
			logger.info(msgJson);
			String respMsg = HttpProxy
					.httpPost("https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + accessToken, msgJson);
			logger.info(respMsg);
			return respMsg;

		} catch (Exception e) {
			logger.info("Failed to creat meun");
			return "Failed to creatMenu";
		}
	}

	/**
	 * 获取jsapi ticket 韩中华 2015-01-14 10：27
	 * 
	 * @return String
	 */
	@Override
	public String doGetJsApiTicket()  {
		long now = new Date().getTime();
		if ((now - jsapi_getTime) / 1000 >= (jsapi_expires_in - 2)) {
			String accessToken = accessTokenService.doGetAccessToken().getAccessToken();
			String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + accessToken
					+ "&type=jsapi";
			String val = "";
			try {
				val = HttpProxy.httpPost(url, "");
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info(val);
			if (JsonUtil.getValue(val, "errcode", Double.class)==0 ) {
				jsapi_ticket = JsonUtil.getValue(val, "ticket", String.class);
				jsapi_expires_in = JsonUtil.getValue(val, "expires_in", Double.class);
				jsapi_getTime = now;

			} else {
				jsapi_ticket = "";
			}
		}
		return jsapi_ticket;
	}

	/**
	 * 
	 * <pre>
	 * 任务： 
	 * 描述： 微信网页jsjdk使用的签名
	 * 作者：韩中华
	 * 时间：2015年1月15日上午11:05:46
	 * &#64;param noncestr
	 * &#64;param jsapiTicket
	 * &#64;param timestamp
	 * &#64;param url
	 * &#64;return
	 * returnType：String
	 * </pre>
	 */
	@Override
	public  String getSignatureInfo(String noncestr, String jsapiTicket, String timestamp, String url) {
		// noncestr（随机字符串）
		// 有效的jsapi_ticket
		// timestamp（时间戳）
		// url（当前网页的URL，不包含#及其后面部分）
		String str = "jsapi_ticket=" + jsapiTicket + "&noncestr=" + noncestr + "&timestamp=" + timestamp + "&url="
				+ url;
		String sha1Signature = EncryptUtil.sha1Encode(str);

		return "{\"signature\":\"" + sha1Signature + "\",\"noncestr\":\"" + noncestr + "\",\"timestamp\":\"" + timestamp+ "\",\"appId\":\"" + appId
				+ "\"}";
	}

	/**
	 * 
	 * <pre>
	 * 任务： 
	 * 描述： 生成一个20位的随机字符串 并且生成一个时间戳
	 * 作者：韩中华
	 * 时间：2015年1月15日上午11:09:49
	 * &#64;return
	 * returnType：String
	 * </pre>
	 */
	@Override
	public  String getNonceStr() {
		String str = "qwertyuioplkjhgfdsazxcvbnmMNBVCXZASDFGHJKLPOIUYTREWQ0123456789";
		Random rond = new Random();
		StringBuffer strbuf = new StringBuffer();

		for (int i = 0; i < 20; i++) {
			int number = rond.nextInt(str.length());
			strbuf.append(str.charAt(number));
		}
		nonce_str = strbuf.toString();
		timestamp = new Date().getTime() / 1000 + "";
		return nonce_str;
	}
	
	/**
	 *返回时间戳
	 */
	@Override
	public  String getTimeStamp() {
		return timestamp;
	}
	
	/**
	 * <pre>
	 * 任务： 
	 * 描述：统一下单接口 
	 * 作者：屈剑飞
	 * 时间：2015年1月16日下午1:11:00
	 * &#64;param pay 统一下单的参数实体
	 * &#64;return prepay_id
	 * returnType：String
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static String payOrder(PayOrders pay) throws ClientProtocolException, IOException {
		String url = "https://api.mch.weixin.qq.com/pay/unifiedorder?";
		String param = MessageUtil.ObjectToXml(pay);
		String jsonMsg = HttpProxy.httpPost(url, param);
		int startIndex = jsonMsg.indexOf("<prepay_id><![CDATA[") + "<prepay_id><![CDATA[".length();
		int endIndex = jsonMsg.indexOf("]]></prepay_id>");

		return jsonMsg.substring(startIndex, endIndex);
	}

	/**
	 * <pre>
	 * 任务： 
	 * 描述：微信支付时调用统一下单接口，生成sign参数
	 * 生成规则：根据所有参数名ASCII码从小到大顺序，使用url使用URL键值对的格式（即key1=value1&key2=value2…）拼接
	 * 作者：屈剑飞
	 * 时间：2015年1月23日下午1:11:38
	 * &#64;param appId 公众号ID
	 * &#64;param mch_id 微信支付分配的商户号
	 * &#64;param nonce_str 随机字符串，自己生成
	 * &#64;param body 商品描述
	 * &#64;param out_trade_no 商户系统内部的订单号
	 * &#64;param total_fee 支付金额单位为分
	 * &#64;param spbill_create_ip 用户端ip
	 * &#64;param notify_url 接收微信支付异步通知回调地址
	 * &#64;param trade_type JSAPI(固定)
	 * &#64;param apiKey API密钥的值
	 * &#64;return
	 * returnType：String
	 * </pre>
	 */
	public static String getSign(String appId, String mch_id, String nonce_str, String body, String out_trade_no,
			String total_fee, String spbill_create_ip, String notify_url, String trade_type, String apiKey) {
		/**
		 * String appId = "wx8888888888888888"; String mch_id =
		 * "1900000109";//商户号 String nonce_str = getNonceStr();//随机生成 String
		 * body = "Ipad mini 16G 白色";//商品描述 String out_trade_no =
		 * "1217752501201407033233368018";//商户订单号 String total_fee =
		 * "8888";//订单总金额(单位为分)；查询接口中单位为元 String spbill_create_ip =
		 * "192.168.0.1";//APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP String
		 * notify_url = "http://wxpay.weixin.qq.com/pub_v2/pay/notify.v2.php";//
		 * 接收微信支付异步通知回调地址 String trade_type = "JSAPI";//交易类型 String apiKey =
		 * "192006250b4c09247ec02edce69f6a2d";
		 */
		StringBuffer all = new StringBuffer("appId=" + appId + "&mch_id=" + mch_id + "&nonce_str=" + nonce_str
				+ "&body=" + body + "&out_trade_no=" + out_trade_no + "&total_fee=" + total_fee + "&spbill_create_ip="
				+ spbill_create_ip + "&notify_url=" + notify_url + "&trade_type=" + trade_type);
		String sign = EncryptUtil.encodeByMD5(all.toString());
		return sign;
	}
	
	public String getWelcomeMsg(){
		//就诊咨询和APP下载地址
		String nurseConsult = properties.getProperty("nurseConsult");
		String appDownload = properties.getProperty("download");
		// 回复文本消息
		String content = "感谢关注【趣医】—— 就医全流程，趣医全搞定。\n" + "· 体验更流畅的服务，请"+"<a href=" + "\"" + appDownload + "\"" + ">"
				+ "下载趣医APP。" + "</a>\n"+"· 如需导诊咨询，请进行"+"<a href=" + "\"" + nurseConsult + "\"" + ">"
				+ "护士导诊。" + "</a>";
		
		return content;
	}

}
