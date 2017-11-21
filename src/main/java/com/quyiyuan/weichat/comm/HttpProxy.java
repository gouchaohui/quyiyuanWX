package com.quyiyuan.weichat.comm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class HttpProxy {
	public static String httpPost(String url, String params) throws ClientProtocolException, IOException {
		// 从配置文件中加载
	    Properties properties = new Properties();
		InputStream path = HttpProxy.class.getClassLoader().getResourceAsStream("httpProxy.properties");
		properties.load(path);
		String ip = properties.getProperty("Proxy.ipAddress");
		String port = properties.getProperty("Proxy.port");
		int port1 = Integer.valueOf(port).intValue();
		String user = properties.getProperty("Proxy.user");
		String password = properties.getProperty("Proxy.password");
		//创建HttpPost方法
		HttpClientBuilder builder = HttpClientBuilder.create();
		CloseableHttpClient httpClient = builder.build();	
		HttpPost httppost = new HttpPost(url);
		StringEntity body = new StringEntity(params, "UTF-8");
		body.setContentType(ContentType.APPLICATION_JSON.toString());
		httppost.setEntity(body);
		if (!StringUtils.isEmpty(ip)) {
			CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(new AuthScope(ip, port1), new UsernamePasswordCredentials(user, password));
			builder.setDefaultCredentialsProvider(provider);
			RequestConfig config = RequestConfig.custom().setProxy(new HttpHost(ip, port1)).build();
			httppost.setConfig(config);
		}		
		CloseableHttpResponse resp = httpClient.execute(httppost);
		return EntityUtils.toString(resp.getEntity());
	}

	/**
	 * 获取请求方Ip地址
	 * @param HttpServletRequest
	 * @return String
	 */
	public static String getIPAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

}
