package com.quyiyuan.weichat.modules.openthird.service.impl;

import com.kyee.nextframework.core.base.domain.internal.EmptyDomain;
import com.quyiyuan.weichat.comm.service.impl.WxBaseService;
import com.quyiyuan.weichat.modules.openthird.aes.WXBizMsgCrypt;
import com.quyiyuan.weichat.modules.openthird.beans.*;
import com.quyiyuan.weichat.modules.openthird.dao.IOpenThirdDao;
import com.quyiyuan.weichat.modules.openthird.enums.MsgTypeEnum;
import com.quyiyuan.weichat.modules.openthird.service.IOpenThirdService;
import com.quyiyuan.weichat.modules.openthird.service.ISendRequestToWXService;
import com.quyiyuan.weichat.modules.openthird.util.MessageUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Service("openThirdService")
public class OpenThirdService extends WxBaseService<EmptyDomain, Serializable> implements IOpenThirdService {

	protected Logger logger = Logger.getLogger(getClass());
	private static final String BASE_URL_WX = "https://api.weixin.qq.com/";
	private static final String KEY_COMPONENT_VERIFY_TICKET = "ComponentVerifyTicket";
	private static final String KEY_COMPONENT_ACCESS_TOKEN = "ComponentAccessToken";
	private static final String AUTHORIZER_ACCESS_TOKEN = "authorizer_access_token";
	private static final String AUTHORIZER_REFRESH_TOKEN = "authorizer_refresh_token";
	private static final String AUTHORIZER_APPID = "authorizer_appid";
	//服务ip
	private String serverAddress;

	//全网发布专用公众号的username
	private static final String ALL_NETWORK_CHECK_APP_USERNAME = "gh_3c884a361561";

	private String encodingAesKey;

	private String token;

	private String secret;

	private String appId;

	@Autowired
	private ApplicationContext context;

	@Autowired
	@Qualifier("openThirdDao")
	private IOpenThirdDao openThirdDao;

	@Autowired
	@Qualifier("sendRequestToWXService")
	private ISendRequestToWXService sendRequestToWXService;

	//必须自己重新注入，不然不起事务
	private IOpenThirdService proxySelf;

	/**
	 * 初始配置文件
	 */
	@PostConstruct
	public void coreServiceInit() {
		Properties properties = new Properties();
		InputStream path = this.getClass().getClassLoader().getResourceAsStream("system-config.properties");
		try {
			proxySelf = context.getBean(IOpenThirdService.class);
			properties.load(path);
			serverAddress = properties.getProperty("server_address");
			encodingAesKey = properties.getProperty("third_encodingAesKey");
			token =  properties.getProperty("third_token");
			secret =  properties.getProperty("third_secret");
			appId =  properties.getProperty("third_appId");
			// 从配置文件获取app网页版地址
		} catch (IOException e) {
			logger.info("加载system-config配置文件出错");
			e.printStackTrace();
		}
	}

	/**
	 * 获取component_verify_ticket,并保存
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@Override
	public String updateAuthorizeEvent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("OpenThirdService acceptAuthorizeEvent start");
		final Map<String,String> parseXml = decryptXml(request);
		logger.info("component_verify_ticket info"+parseXml.toString());
		//存储ticket
		String component_verify_ticket=parseXml.get(KEY_COMPONENT_VERIFY_TICKET);
		if (!StringUtils.equals(component_verify_ticket, null)) {
			openThirdDao.updateComponentTokenOrTicket(KEY_COMPONENT_VERIFY_TICKET,component_verify_ticket);
		}
		logger.info("OpenThirdService acceptAuthorizeEvent end");
		return "success";
	}

	/**
	 * 获取ComponentToken,如果已经过期，则重新获取
	 * @return
	 */
	@Override
	public String updateOrGetComponentToken() {
		logger.info("OpenThirdService getComponentToken start");
		HashMap<String,Object> componentTokenInfo= (HashMap<String, Object>) openThirdDao.getComponentTokenFromTable(KEY_COMPONENT_ACCESS_TOKEN);
		String IsInvalid=componentTokenInfo.get("IsInvalid")+"";
		if("0".equals(IsInvalid))
			return componentTokenInfo.get(KEY_COMPONENT_ACCESS_TOKEN)+"";
		ComponentTokenRequest request = new ComponentTokenRequest();
		request.setComponent_appid(appId);
		request.setComponent_appsecret(secret);
		HashMap<String,Object> componentVerifyTicketInfo= (HashMap<String, Object>) openThirdDao.getComponentTokenFromTable(KEY_COMPONENT_VERIFY_TICKET);
		request.setComponent_verify_ticket(componentVerifyTicketInfo.get(KEY_COMPONENT_VERIFY_TICKET)+"");
		logger.info("获取ComponentToken传入参数"+request.toString());
		return getComponentToken(request);
	}

	/**
	 * 获取授权前的PreAuthCode
	 * @return
	 */
	@Override
	public String doGetPreAuthCode() {
		logger.info("OpenThirdService getPreAuthCode start");
		PreAuthCodeRequest request = new PreAuthCodeRequest();
		request.setComponent_appid(appId);
		String preAuthCode = getPreAuthCode(request);
		logger.info("preAuthCode:"+preAuthCode);
		logger.info("OpenThirdService getPreAuthCode end");
		return preAuthCode;
	}

	/**
	 * 一键授权
	 * @param request
	 * @param response
	 */
	@Override
	public void doGoAuthor(HttpServletRequest request, HttpServletResponse response) {
		logger.info("OpenThirdService goAuthor start");
		try {
			//预授权码
			String preAuthCode = proxySelf.doGetPreAuthCode();
			String url = "https://mp.weixin.qq.com/cgi-bin/componentloginpage?component_appid="+appId+"&pre_auth_code="+preAuthCode
					+"&redirect_uri=http://" +serverAddress+"/publicservice/openThirdwx/authorCallback";
			logger.info("OpenThirdService goAuthor end url="+url);
			response.sendRedirect(url);
		} catch (Exception e) {
			logger.error("一键授权失败"+e.toString());
		}
	}

	/**
	 * 授权完成后回调,获取授权公众号信息
	 * @param request
	 * @param response
	 */
	@Override
	public void execAuthorCallback(HttpServletRequest request, HttpServletResponse response) {
		try {
		logger.info("OpenThirdService execAuthorCallback start");
		String authorization_code = request.getParameter("auth_code");
		String expires_in = request.getParameter("expires_in");
		logger.info("authorization_code"+authorization_code
				+" expires_in"+expires_in);
		//向微信发请求
		getAuthorizerRefreshToken(authorization_code);
		logger.info("OpenThirdService execAuthorCallback end");
		MessageUtil.output(response,"<div>授权成功</div>");
		} catch (Exception e) {
			MessageUtil.output(response,"<div>"+e.getMessage()+"</div>");
		}

	}
	/**
	 * 获取第三方平台调用微信公众号的凭证
	 * 单个微信公众号获取调用凭证
	 */
	@Override
	public String doGetAuthorizerAccessTokenSingle(String authorizerAppId) {
		HashMap<String,Object> authorizerInfo= (HashMap<String, Object>)openThirdDao.getAuthorizerAccessTokenInfo(authorizerAppId);
		if(null==authorizerInfo)
		{
			logger.error("根据appid查询调用凭证失败authorizerAppId="+authorizerAppId);
			return "";
		}
		String IsInvalid=authorizerInfo.get("IsInvalid")+"";
		if("0".equals(IsInvalid))
			return authorizerInfo.get(AUTHORIZER_ACCESS_TOKEN)+"";
		return getOrRefreshAuthorizerAccessToken(authorizerAppId,authorizerInfo.get(AUTHORIZER_REFRESH_TOKEN)+"");
	}
	/**
	 * 获取第三方平台调用微信公众号的凭证
	 * 批量获取所有已经授权的公众号调用凭证
	 */
	@Override
	public List<Map<String, Object>> doGetAuthorizerAccessTokenAll() {
		List<Map<String, Object>> authorizerInfoList= openThirdDao.getAuthorizerAccessTokenInfoAll();
		for(int i=0;i<authorizerInfoList.size();i++){
			String IsInvalid=authorizerInfoList.get(i).get("IsInvalid")+"";
			if("1".equals(IsInvalid)){
				String authorizerAccessToken=getOrRefreshAuthorizerAccessToken(authorizerInfoList.get(i).get(AUTHORIZER_APPID)+"",authorizerInfoList.get(i).get(AUTHORIZER_REFRESH_TOKEN)+"");
				if(!StringUtils.isEmpty(authorizerAccessToken)){
					authorizerInfoList.get(i).put(AUTHORIZER_ACCESS_TOKEN,authorizerAccessToken);
					authorizerInfoList.get(i).put("IsInvalid","0");
				}
			}
		}
		return authorizerInfoList;
	}

	/**
	 * 用于第三方消息事件回调
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	public void doMessageEventCallback(String appid,HttpServletRequest request, HttpServletResponse response)throws Exception {
		logger.info("messageEventCallback接收到微信推送的消息 start");
		Map<String, String> parseXml = decryptXml(request);
		final String toUserName = parseXml.get("ToUserName");
		//事件以及消息请求来自全网发布流程专用公众号
		if (StringUtils.equals(toUserName, ALL_NETWORK_CHECK_APP_USERNAME)) {
			//全网发布检测流程
			allNetworkCheck(parseXml,request,response);
		}else{
			//处理微信推送的消息和时间
			handleMessageWX(appid,parseXml,request,response);
		}
		logger.info("messageEventCallback接收到微信推送的消息 end");
		MessageUtil.output(response,"");
	}

	/**
	 * 处理微信公众号推送的消息
	 * @param parseXml
	 * @param request
	 * @param response
	 */
	private void handleMessageWX(String appid,Map<String, String> parseXml, HttpServletRequest request, HttpServletResponse response) {
		logger.info("OpenThirdService handleMessageWX Strart");
			// 发送方账号（openid）
			String fromUserName = parseXml.get("FromUserName");
			// 公众帐号
			String toUserName = parseXml.get("ToUserName");
			// 消息类型
			String msgType = parseXml.get("MsgType");
			//时间消息
		logger.info("OpenThirdService handleMessageWX param fromUserName:"+fromUserName+
				" toUserName:"+toUserName+" msgType"+msgType);
			if (StringUtils.equals(MsgTypeEnum.REQ_MESSAGE_TYPE_EVENT.getCode(), msgType)) {
				String event = parseXml.get("Event");
				logger.info("REQ_MESSAGE_TYPE_EVENT:"+event );
				switch (event) {
					case "subscribe":
						String eventPara = parseXml.get("EventKey");
						logger.info("扫关注码参数:"+eventPara);
						if(StringUtils.isEmpty(eventPara)&&eventPara.length()>8){
							eventPara=eventPara.substring(8);
						}
						sendMsgToWechat(appid,fromUserName,"你来弄啥里。。。");
						break;
					case "unsubscribe":
						break;
					default:
						break;
				}
			} else if (StringUtils.equals(MsgTypeEnum.REQ_MESSAGE_TYPE_TEXT.getCode(), msgType)) {
				sendMsgToWechat(appid,fromUserName,"别发了。。。");
			}
		logger.info("OpenThirdService handleMessageWX end");
	}

	/**
	 * 发送客服消息封装方法
	 * @param appid
	 * @param fromUserName
	 * @param content
	 */
	public void sendMsgToWechat(String appid,String fromUserName,String content){
		String authorizerAccessToken=proxySelf.doGetAuthorizerAccessTokenSingle(appid);
		if(StringUtils.isEmpty(authorizerAccessToken)){
			logger.error("发送客服消息失败，原因是authorizerAccessToken获取失败");
		}else{
			Map<String, Object> testCustomMsg = new HashMap<>();
			testCustomMsg.put("touser", fromUserName);
			testCustomMsg.put("msgtype", "text");
			Map<String, String> textMsg = new HashMap<>();
			textMsg.put("content", content);
			testCustomMsg.put("text", textMsg);
			//发送测试客服消息
			sendCustomMsgTest(testCustomMsg, authorizerAccessToken);
		}
	}
	/**
	 * 全网发布测试消息回调
	 * @param request
	 * @param response
	 */
	private void allNetworkCheck(Map<String, String> parseXml,HttpServletRequest request,HttpServletResponse response) throws Exception {
//		Map<String, String> parseXml = decryptXml(request);
		final String fromUserName = parseXml.get("FromUserName");
		final String toUserName = parseXml.get("ToUserName");
		if (StringUtils.equals(MsgTypeEnum.REQ_MESSAGE_TYPE_EVENT.getCode(), parseXml.get("MsgType"))) {
			// 1: 模拟粉丝触发专用测试公众号的事件，并推送事件消息到专用测试公众号，第三方平台方开发者需要提取推送XML信息中的event值，
			// 并在5秒内立即返回按照下述要求组装的文本消息给粉丝。
			final String event = parseXml.get("Event");
			final String content = event + "from_callback";
			final String encryptMsg = MessageUtil.createEncryptTextMsg(fromUserName, toUserName, content, request.getParameter("nonce"),
					new WXBizMsgCrypt(token, encodingAesKey, appId));
			MessageUtil.output(response, encryptMsg);

		} else if (StringUtils.equals(MsgTypeEnum.REQ_MESSAGE_TYPE_TEXT.getCode(), parseXml.get("MsgType"))) {
			final String content = parseXml.get("Content");
			if (StringUtils.equals("TESTCOMPONENT_MSG_TYPE_TEXT", content)) {
				// 2: 模拟粉丝发送文本消息给专用测试公众号，第三方平台方需根据文本消息的内容进行相应的响应
				final String replyContent = content + "_callback";
				final String encryptMsg = MessageUtil.createEncryptTextMsg(fromUserName, toUserName, replyContent, request.getParameter("nonce"),
						new WXBizMsgCrypt(token, encodingAesKey, appId));
				MessageUtil.output(response, encryptMsg);

			} else if (StringUtils.startsWithIgnoreCase(content, "QUERY_AUTH_CODE")) {
				// 3: 模拟粉丝发送文本消息给专用测试公众号，第三方平台方需在5秒内返回空串表明暂时不回复，然后再立即使用客服消息接口发送消息回复粉丝
				response.getWriter().print("");

				final String authorizationCode = content.split(":")[1];
				//获取authorizer_access_token
				AuthInfoPO reponse= getAuthorizerRefreshToken(authorizationCode);
				String authorizerAccessToken=reponse.getAuthorizer_access_token();
				//拼接测试map
				logger.info("authorizerAccessToken: "+ authorizerAccessToken);
				Map<String, Object> testCustomMsg = new HashMap<>();
				testCustomMsg.put("touser", fromUserName);
				testCustomMsg.put("msgtype", "text");
				Map<String, String> textMsg = new HashMap<>();
				textMsg.put("content", authorizationCode + "_from_api");
				testCustomMsg.put("text", textMsg);
				//发送测试客服消息
				sendCustomMsgTest(testCustomMsg, authorizerAccessToken);
			}
		}

	}

	private void sendCustomMsgTest(Map<String, Object> testCustomMsg, String authorizerAccessToken) {
		String url=BASE_URL_WX+"cgi-bin/message/custom/send?access_token="+authorizerAccessToken;
		String para=JSONObject.fromObject(testCustomMsg).toString();
		logger.info("全网发布测试sendCustomMsgTest url:"+url+"参数para"+para);
		String result=sendRequestToWXService.postSend(url,para);
		logger.info("发送客服消息结果"+result);
	}

	private String getOrRefreshAuthorizerAccessToken(String authorizerAppId,String authorizerRefreshToken) {
		AuthorizerRefreshTokenRequest request = new AuthorizerRefreshTokenRequest();
		request.setComponent_appid(appId);
		request.setAuthorizer_appid(authorizerAppId);
		request.setAuthorizer_refresh_token(authorizerRefreshToken);
		String componentAccessToken=proxySelf.updateOrGetComponentToken();
		String url=BASE_URL_WX+"cgi-bin/component/api_authorizer_token?component_access_token="+componentAccessToken;
		String result=sendRequestToWXService.postSend(url,JSONObject.fromObject(request).toString());
		logger.info("AuthorizerRefreshTokenResponse:"+ result);
		AuthorizerRefreshTokenResponse response = (AuthorizerRefreshTokenResponse)JSONObject.toBean(JSONObject.fromObject(result),AuthorizerRefreshTokenResponse.class);
		Assert.isTrue(response.isValid(), "获取getOrRefreshAuthorizerAccessToken失败:" + response);
		//重新获取的access_token需要更新到数据库中
		AuthInfoPO authInfoPO=new AuthInfoPO();
		authInfoPO.setAuthorizer_access_token(response.getAuthorizer_access_token());
		authInfoPO.setAuthorizer_refresh_token(response.getAuthorizer_refresh_token());
		authInfoPO.setAuthorizer_appid(authorizerAppId);
		authInfoPO.setFunc_info(null);
		openThirdDao.updateAuthorizerRefreshToken(authInfoPO);
		return response.getAuthorizer_access_token();
	}


	/**
	 * 发送请求到微信，获取授权完后公众号信息,并更新或保存
	 * 一定时间内，用户即便再次扫码，刷新码不变，
	 * 但是过一段时间后，用户再次扫描刷新码就会改变，
	 * 所以我们要在程序中处理，用户每一次扫描，
	 * 都要先更新它的刷新码和accussToken在进行其他业务的处理，避免刷新码失效。
	 * @param request
	 * @return
	 */
	private AuthInfoPO getAuthorizerRefreshToken(String authorization_code) {
		//使用授权码换取公众号或小程序的接口调用凭据和授权信息
		logger.info("getAuthorizerRefreshToken authorization_code:"+authorization_code);
		AuthorizerTokenRequest request=new AuthorizerTokenRequest();
		request.setComponent_appid(appId);
		request.setAuthorization_code(authorization_code);
		String componentAccessToken=proxySelf.updateOrGetComponentToken();
		String url=BASE_URL_WX+"cgi-bin/component/api_query_auth?component_access_token="+componentAccessToken;
		String result=sendRequestToWXService.postSend(url,JSONObject.fromObject(request).toString());
		logger.info("AuthInfoResponse:"+ result);
		AuthInfoResponse response = (AuthInfoResponse)JSONObject.toBean(JSONObject.fromObject(result),AuthInfoResponse.class);
		Assert.isTrue(response.isValid(), "获取getAuthorizerRefreshToken失败:" + response);
		AuthInfoPO authInfoPO=response.getAuthorization_info();
		logger.info("OpenThirdService getAuthorizerRefreshToken authInfoPO="+JSONObject.fromObject(request).toString());
		insertORupdateAuthorizerRefreshToken(authInfoPO);
		return authInfoPO;
	}

	/**
	 * 第一授权则插入,第二次授权更新
	 * @param authInfoPO
	 */
	private void insertORupdateAuthorizerRefreshToken(AuthInfoPO authInfoPO) {
		//如果Authorizer_appid如果已经存在则说明为第二次授权，更新所有信息，如果没有则插入
		int existence=openThirdDao.getAuthorizerApp(authInfoPO.getAuthorizer_appid());
		logger.info("OpenThirdService insertORupdateAuthorizerRefreshToken Authorizer_appid"
				+authInfoPO.getAuthorizer_appid()+" existence"+existence);
		if(existence>0){
			openThirdDao.updateAuthorizerRefreshToken(authInfoPO);
		}else{
			openThirdDao.insertAuthorizerRefreshToken(authInfoPO);
		}
	}

	/**
	 * 发送请求到微信，获取ComponentToken
	 * @param request
	 * @return
	 */
	public String getComponentToken(ComponentTokenRequest request) {
		String url=BASE_URL_WX+"cgi-bin/component/api_component_token";
		//发送请求给微信
		String result=sendRequestToWXService.postSend(url,JSONObject.fromObject(request).toString());
		logger.info("componentAccessTokenResponse:"+ result);
		ComponentAccessTokenResponse response=(ComponentAccessTokenResponse)JSONObject.toBean(JSONObject.fromObject(result),ComponentAccessTokenResponse.class);
		Assert.isTrue(response.isValid(), "获取ComponentAccessToken失败:" + response);
		String componentToken=response.getComponent_access_token();
		openThirdDao.updateComponentTokenOrTicket(KEY_COMPONENT_ACCESS_TOKEN,componentToken);
		return componentToken;
	}
	/**
	 * 发送请求到微信，获取PreAuthCode
	 * gouchaohui
	 * @param request
	 * @return
	 */
	public String getPreAuthCode(PreAuthCodeRequest request) {
		String componentAccessToken=proxySelf.updateOrGetComponentToken();
		String url=BASE_URL_WX+"cgi-bin/component/api_create_preauthcode?component_access_token="+componentAccessToken;
		//发送请求给微信
		String result=sendRequestToWXService.postSend(url,JSONObject.fromObject(request).toString());
		logger.info("preAuthCodeResponse:"+result);
		PreAuthCodeResponse response = (PreAuthCodeResponse)JSONObject.toBean(JSONObject.fromObject(result),PreAuthCodeResponse.class);
		Assert.isTrue(response.isValid(), "获取PreAuthCode失败:" + response);
		return response.getPre_auth_code();
	}
	/**
	 * 获取并解密xml
	 * @param request
	 * @return
	 */
	private Map<String, String> decryptXml(HttpServletRequest request) throws Exception {
		String timestamp=request.getParameter("timestamp");
		String encrypt_type=request.getParameter("encrypt_type");
		String nonce=request.getParameter("nonce");
		String msg_signature=request.getParameter("msg_signature");
		logger.info("timestamp: "+timestamp+" encrypt_type: "+encrypt_type+
		" nonce: "+nonce+" msg_signature "+msg_signature);
		StringBuilder sb = new StringBuilder();
		BufferedReader in = request.getReader();
		String line;
		while ((line = in.readLine()) != null) {
			sb.append(line);
		}
		String xml = sb.toString();
		//解密
		WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
		logger.info("解密 decryptXmlmsg_signature:"+msg_signature
		+" timestamp:"+timestamp+" nonce:"+nonce+" xml:"+xml);
		xml = pc.decryptMsg(msg_signature, timestamp, nonce, xml);
	 	//得到component_verify_ticket
	 	return MessageUtil.parseXml(xml);
	 }

	@Override
	public void docreateCode(HttpServletRequest request, HttpServletResponse response) {
		String appid=request.getParameter("appid");
		if(StringUtils.isEmpty(appid)){
			MessageUtil.output(response,"请输入要生成带参数二维码的公众号appid");
		}else {
			String accessToken = proxySelf.doGetAuthorizerAccessTokenSingle(appid);
			if(StringUtils.isEmpty(accessToken)){
				logger.error("发送客服消息失败，原因是authorizerAccessToken获取失败");
			}
			//获取永久二维码的ticket
			String ticket = preTicket4DimensionalCode(accessToken);
			String codeUrl = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + ticket;
			try {
				response.sendRedirect(codeUrl);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String preTicket4DimensionalCode(String accessToken) {
		HashMap<String,String> addpara=new HashMap<String,String>();
		String testData="tests";
		addpara.put("scene_str",testData);
		HashMap<String,Object> paratotal=new HashMap<String,Object>();
		paratotal.put("scene",addpara);
		HashMap<String,Object> para=new HashMap<>();
		para.put("action_name","QR_LIMIT_STR_SCENE");
		para.put("action_info",paratotal);
		String url=BASE_URL_WX+"cgi-bin/qrcode/create?access_token="+accessToken;
		String result=sendRequestToWXService.postSend(url,JSONObject.fromObject(para).toString());
		QRcodeTicketResponse response = (QRcodeTicketResponse)JSONObject.toBean(JSONObject.fromObject(result),QRcodeTicketResponse.class);
		Assert.isTrue(response.isValid(), "获取生成二维码的ticket失败:" + response.getErrmsg());
		return response.getTicket();
	}

}
