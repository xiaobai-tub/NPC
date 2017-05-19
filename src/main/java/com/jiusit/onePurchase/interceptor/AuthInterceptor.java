package com.jiusit.onePurchase.interceptor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.cookie.SetCookie;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.UserApi;
import com.jfinal.weixin.sdk.kit.IpKit;
import com.jiusit.common.utils.DateUtil;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.model.OnePurchaseUser;
import com.jiusit.onePurchase.model.TradeMarketCode;
import com.jiusit.onePurchase.model.TradeMarketShare;
import com.jiusit.onePurchase.model.TradeMarketUser;
import com.jiusit.onePurchase.model.TradeUserAccess;

/**
 * BlogInterceptor 此拦截器仅做为示例展示，在本 demo 中并不需要
 */
public class AuthInterceptor implements Interceptor {
	private static final Logger log = Logger.getLogger(AuthInterceptor.class);

	public void intercept(Invocation ai) {
		ApiConfig apiConfig = new ApiConfig();

		// 配置微信 API 相关常量
		apiConfig.setToken(PropKit.get("token"));
		apiConfig.setAppId(PropKit.get("appId"));
		apiConfig.setAppSecret(PropKit.get("appSecret"));
		/**
		 * 是否对消息进行加密，对应于微信平台的消息加解密方式： 1：true进行加密且必须配置 encodingAesKey
		 * 2：false采用明文模式，同时也支持混合模式
		 */
		apiConfig
				.setEncryptMessage(PropKit.getBoolean("encryptMessage", false));
		apiConfig.setEncodingAesKey(PropKit.get("encodingAesKey",
				"setting it in config file"));
		// 获取微信用户信息
		ApiConfigKit.setThreadLocalApiConfig(apiConfig);
		Controller controller = ai.getController();
		ai.invoke();

		String access_token = "";
		String path = controller.getRequest().getRequestURI();
		log.debug("UserInterceptor  path:" + path);
		String userInfo = controller.getCookie("userInfo");
		if (StringUtil.isEqual(userInfo, "undefined") || userInfo == null
				|| userInfo.equals("")) {
			userInfo = controller.getAttrForStr("userInfo");
			
			if (userInfo == null || userInfo.equals("")) {
				try {
					// 获取from_user
					String code = controller.getPara("code");
					if (code == null || "".equals(code)) {
						String redirect_uri1_0 = URLEncoder.encode(
								PropKit.get("host_uri",
										"setting it in config file") + path,
								"utf-8");
						String m1_0 = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
								+ PropKit.get("appId")
								+ "&redirect_uri="
								+ redirect_uri1_0
								+ "&response_type=code&scope=snsapi_userinfo&state=defaultToHome#wechat_redirect";
						controller.redirect(m1_0);
						return;
					}
					System.out.println("code:" + code);
					String userAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
							+ PropKit.get("appId")
							+ "&secret="
							+ PropKit.get("appSecret")
							+ "&code="
							+ code
							+ "&grant_type=authorization_code";
					String userAccessTokenData = HttpKit
							.get(userAccessTokenUrl);
					System.out.println("userAccessTokenData:" + userAccessTokenData);
					// 注意userAccessToken一天1000次与2小时限制
					ObjectMapper mapper = new ObjectMapper();
					Map data = mapper.readValue(userAccessTokenData, Map.class);
					if (data.get("openid") != null) {
						String openId = data.get("openid").toString();
						access_token = data.get("access_token").toString();
							 userInfo = HttpKit
									.get("https://api.weixin.qq.com/sns/userinfo?access_token="
											+ access_token
											+ "&openid="
											+ openId + "&lang=zh_CN");
							
					 }else{
						 log.debug("get openId fail ");
					   }
					} catch (Exception e) {
						e.printStackTrace();
					}
		      }
		
		}
		Cookie cookie = new Cookie("userInfo", userInfo);
		cookie.setMaxAge(24 * 60 * 60 * 30);
		cookie.setPath("/");
		controller.setCookie(cookie);
        }
	}
