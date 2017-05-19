package com.jiusit.onePurchase.controller;

import java.util.Map;

import com.jfinal.kit.HttpKit;
import com.jfinal.kit.PropKit;
import com.jfinal.weixin.sdk.api.AccessToken;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;

public class TicketUtil {
	private static JssdkTicket jssdkTicket;
	private static CardTicket cardTicket;	//卡卷

	public static JssdkTicket getJssdkTicket() {
		if (jssdkTicket != null && jssdkTicket.isAvailable()) {
			return jssdkTicket;
		} else {
			refreshAccessToken();   
			return jssdkTicket;
		}
	}
	
	public static CardTicket getCardTicket() {
		if (cardTicket != null && cardTicket.isAvailable()) {
			return cardTicket;
		} else {
			refreshCardAccessToken();
			return cardTicket;
		}
	}
	
	private static void refreshAccessToken() {
		jssdkTicket = requestJssdkTicket(); 
	}
	
	private static synchronized JssdkTicket requestJssdkTicket() {
		ApiConfig apiConfig = new ApiConfig(PropKit.get("token"), PropKit.get("appId"), PropKit.get("appSecret"));
		ApiConfigKit.setThreadLocalApiConfig(apiConfig);
		AccessToken accessToken = AccessTokenApi.getAccessToken();
		System.out.println("分享token==="+accessToken.getAccessToken());
		String json = HttpKit.get("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + accessToken.getAccessToken() + "&type=jsapi");
		JssdkTicket result = new JssdkTicket(json);
		if (!result.isAvailable()) {
			refreshAccessToken();
		}
		return result;
	}
	
	private static void refreshCardAccessToken() {
		cardTicket = requestCardTicket();
	}

	private static synchronized CardTicket requestCardTicket() {
		ApiConfig apiConfig = new ApiConfig(PropKit.get("token"), PropKit.get("appId"), PropKit.get("appSecret"));
		ApiConfigKit.setThreadLocalApiConfig(apiConfig);
		AccessToken accessToken = AccessTokenApi.getAccessToken();
		String json = HttpKit.get("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + accessToken.getAccessToken() + "&type=wx_card");
		CardTicket result = new CardTicket(json);
		if (!result.isAvailable()) {
			refreshCardAccessToken();
		}
		return result;
	}
}
