package com.jiusit.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;

public class SentMessage {

	/**
	 * 重置支付密码验证码
	 * @param telphone
	 * @param verify_code
	 * @return
	 */
	public static String sentResetPayPwd(String telphone,String verify_code){
		
		String ret = "";
		try {
			String accessToken = getAccessToken();
			
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Map<String, Object> paramMap = new HashMap<String, Object>();

			paramMap.put("param1", verify_code);
			paramMap.put("param2", "5");

			Map<String, Object> sendMap = new HashMap<String, Object>();
			//sendMap.put("app_id", Var.get("p2p.message.app_id"));
			sendMap.put("access_token", accessToken);
			//sendMap.put("access_token",	Var.get("p2p.message.access_token"));
			sendMap.put("acceptor_tel", telphone);
			//sendMap.put("template_id", Var.get("p2p.message.reset_pay_pwd.template_id"));
			sendMap.put("template_param", new JSONObject(paramMap).toString());
			sendMap.put("timestamp", df.format(new Date()));
			
			//String message_url = Var.get("p2p.message.message_url");
			
			//ret = HttpClientUtil.POSTMethod(message_url, sendMap, null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * 重置密码验证码
	 * @param telphone
	 * @param verify_code
	 * @return
	 */
	public static String sentResetPwd(String telphone,String verify_code){
		
		String ret = "";
		try {
			String accessToken = getAccessToken();
			
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Map<String, String> paramMap = new HashMap<String, String>();

			paramMap.put("param1", telphone);
			paramMap.put("param2", verify_code);
			
			String message_url = PropKit.get("message_url");
			
			ret = HttpKit.post(message_url, "app_id="+PropKit.get("app_id")+"&access_token="+accessToken+
					"&acceptor_tel="+telphone+"&template_id="+PropKit.get("reset_template_id")+
					"&template_param="+new JSONObject(paramMap).toString()+"&timestamp="+df.format(new Date()));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * 发送提现验证码
	 * @param telphone
	 * @param verify_code
	 * @return
	 */
	public static String sentCashCode(String telphone,String verify_code){
		
		String ret = "";
		try {
			String accessToken = getAccessToken();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Map<String, Object> paramMap = new HashMap<String, Object>();

			paramMap.put("param1", verify_code);
			paramMap.put("param2", "5");

			Map<String, Object> sendMap = new HashMap<String, Object>();
			//sendMap.put("app_id", Var.get("p2p.message.app_id"));
			//sendMap.put("access_token",	Var.get("p2p.message.access_token"));
			sendMap.put("access_token", accessToken);
			sendMap.put("acceptor_tel", telphone);
			//sendMap.put("template_id", Var.get("p2p.message.cash.template_id"));
			sendMap.put("template_param", new JSONObject(paramMap).toString());
			sendMap.put("timestamp", df.format(new Date()));
			
			//String message_url = Var.get("p2p.message.message_url");
			
			//ret = HttpClientUtil.POSTMethod(message_url, sendMap, null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * 发送手机验证验证码
	 * @param telphone
	 * @param verify_code
	 * @return
	 */
	public static String sentTelCode(String telphone,String verify_code){
		
		String ret = "";
		try {
			String accessToken = getAccessToken();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("param1", telphone);
			paramMap.put("param2", verify_code);

			String message_url = PropKit.get("message_url");
			ret = HttpKit.post(message_url, "app_id="+PropKit.get("app_id")+"&access_token="+accessToken+
					"&acceptor_tel="+telphone+"&template_id="+PropKit.get("register_template_id")+
					"&template_param="+new JSONObject(paramMap).toString()+"&timestamp="+df.format(new Date()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * 获取短信发送令牌
	 * @return
	 */
	public static String getAccessToken(){
		Map<String, String> send = new HashMap<String, String>();
		String ret = "";
		try {
			send.put("grant_type", "client_credentials");
			send.put("app_id", PropKit.get("app_id"));
			send.put("app_secret", PropKit.get("app_secret"));

			ret = HttpKit.post("https://oauth.api.189.cn/emp/oauth2/v3/access_token", send,"");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject retJson = new JSONObject(ret);
		return retJson.getString("access_token");
	}
}
