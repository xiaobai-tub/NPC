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
public class UserInterceptor implements Interceptor {
	private static final Logger log = Logger.getLogger(UserInterceptor.class);

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
		String from_user = controller.getCookie("wx_openid");
		HttpServletRequest request = controller.getRequest();
		String agent_num = request.getParameter("agent_num");
		String agent_id = "";
		String commodity_id = request.getParameter("id");
		if (StringUtil.isEmpty(agent_num)) {
			agent_num = "";
		}else{
			String[] agentNums=agent_num.split(",");
			agent_id=agentNums[0];
			if(agentNums.length>=2){
			if(StringUtil.isEmpty(commodity_id)){
				commodity_id=agentNums[1];
			 }
			}
		}
		if(!StringUtil.isEmpty(agent_num)){
			path+="?agent_num="+agent_num;
		}
		System.out.println("代理商id-------------------" + agent_id);
		System.out.println("商品id-------------------" + commodity_id);
		String agent = controller.getRequest().getHeader("User-Agent")
				.toLowerCase();
		if (agent.indexOf("micromessenger") <= 0) {
			try {
				path=path.replace("onePurchase_wx", "onePurchaseMl_wb");
				controller.getResponse().sendRedirect(
						PropKit.get("host_uri", "setting it in config file")
								+ path);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		System.out.println("UserInterceptor Cookie from_user 1:" + from_user);
		if (StringUtil.isEqual(from_user, "undefined") || from_user == null
				|| from_user.equals("")) {
			from_user = controller.getAttrForStr("wx_openid");
			System.out.println("UserInterceptor Attribute from_user:"
					+ from_user);
			if (from_user == null || from_user.equals("")) {
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
						OnePurchaseUser user = OnePurchaseUser.dao.findFirst(
								"select * from purchase_users where wx_id=?",
								openId);
						String user_id = "";
						String nickname = "";
						if (user == null) {
							String dataStr = HttpKit
									.get("https://api.weixin.qq.com/sns/userinfo?access_token="
											+ access_token
											+ "&openid="
											+ openId + "&lang=zh_CN");
							Map<String, Object> map = StringUtil
									.getMap(dataStr);
							nickname = (String) map.get("nickname");
							nickname = URLEncoder.encode(nickname);
							Integer sex = (Integer) map.get("sex");
							String headimgurl = (String) map.get("headimgurl");
							if (!StringUtil.isEmpty(headimgurl)) {
								StringUtil.replace(headimgurl, "\\/", "/");
							}
							if(!StringUtil.isEmpty(agent_id)){
								TradeMarketUser  checkMarketUser=TradeMarketUser.dao.findFirst("select * from trade_market_user where id='"+agent_id+"'");
								if(checkMarketUser == null ){
									agent_id="";
								  }
								}
							user_id = StringUtil.getUUID();
							OnePurchaseUser purchaseUser = new OnePurchaseUser()
									.set("id", user_id)
									.set("nick_name", nickname)
									.set("real_name", nickname).set("sex", sex)
									.set("wx_id", openId)
									.set("head_path", headimgurl)
									.set("is_follow", "0")
									.set("amount_money", 0.00)
									.set("login_num", 1)
									.set("login_type", "02")
									.set("agent_code", agent_id)
									.set("vip_identify", "01")
									.set("create_by", user_id)
									.set("create_name", nickname)
									.set("create_date", new Date())
									.set("update_by", user_id)
									.set("update_name", nickname)
									.set("update_date", new Date());
							purchaseUser.save();

						} else {
							user_id = user.getStr("id");
							nickname = user.getStr("nickname");
							int login_num = user.getInt("login_num") + 1;
							if (!StringUtil.isEmpty(agent_id)) {
								if (StringUtil.isEqual(agent_id,
										user.getStr("agent_code"))) {
									OnePurchaseUser purchaseUser = new OnePurchaseUser()
											.set("id", user.get("id"))
											.set("login_num", login_num)
											.set("update_by", user.get("id"))
											.set("update_name",
													user.get("nick_name"))
											.set("update_date", new Date());
									purchaseUser.update();
								} else {
									OnePurchaseUser purchaseUser = new OnePurchaseUser()
											.set("id", user.get("id"))
											.set("agent_code", agent_id)
											.set("login_num", login_num)
											.set("update_by", user.get("id"))
											.set("update_name",
													user.get("nick_name"))
											.set("update_date", new Date());
									purchaseUser.update();
								}
							}else{
								OnePurchaseUser purchaseUser = new OnePurchaseUser()
								.set("id", user.get("id"))
								.set("login_num", login_num)
								.set("update_by", user.get("id"))
								.set("update_name",
										user.get("nick_name"))
								.set("update_date", new Date());
						purchaseUser.update();
							}
						}
						from_user = openId;
						Cookie cookie = new Cookie("wx_openid", from_user);
						cookie.setMaxAge(24 * 60 * 60 * 30);
						cookie.setPath("/");
						controller.setCookie(cookie);
						System.out.println("设置cookie1-----------------------"+from_user);
						if (!StringUtil.isEmpty(agent_id)) {
									TradeMarketUser marketUser = TradeMarketUser.dao
											.findById(agent_id);
									if (marketUser != null) {
										TradeUserAccess userAccess = TradeUserAccess.dao
												.set("id", StringUtil.getUUID())
												.set("user_id", user_id)
												.set("agent_id",agent_id)
												.set("agent_code", agent_id)
												.set("access_date", new Date())
												.set("ip_address",
														IpKit.getRealIp(request))
												.set("create_by", user_id)
												.set("create_name", nickname)
												.set("create_date", new Date())
												.set("update_by", user_id)
												.set("update_name", nickname)
												.set("update_date", new Date())
												.set("remarks", "用户访问");
										userAccess.save();
										Db.update("update trade_market_user set access_amount=access_amount+1 where id='"
												+ agent_id
												+ "'");
									}
						}
					} else {
						log.debug("get openId fail ");
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			OnePurchaseUser user = OnePurchaseUser.dao.findFirst(
					"select * from purchase_users where wx_id=?", from_user);
			if (user != null) {
				if (!StringUtil.isEmpty(agent_id)) {
					TradeMarketUser marketUser = TradeMarketUser.dao
							.findFirst("select * from trade_market_user where id='"
									+ agent_id + "'");
					if (marketUser != null) {
								Db.update("update purchase_users set login_num=login_num+1,agent_code='"
										+ agent_id
										+ "' where wx_id='"
										+ from_user + "'");
								TradeUserAccess userAccess = TradeUserAccess.dao
										.set("id", StringUtil.getUUID())
										.set("user_id", user.getStr("id"))
										.set("agent_id",
												agent_id)
										.set("agent_code", agent_id)
										.set("access_date", new Date())
										.set("ip_address",
												IpKit.getRealIp(request))
										.set("create_by", user.getStr("id"))
										.set("create_name",
												user.getStr("nick_name"))
										.set("create_date", new Date())
										.set("update_by", user.getStr("id"))
										.set("update_name",
												user.getStr("nick_name"))
										.set("update_date", new Date())
										.set("remarks", "用户访问");
								userAccess.save();
								Db.update("update trade_market_user set access_amount=access_amount+1 where id='"
										+ agent_id + "'");
							}
				}else{
					Db.update("update purchase_users set login_num=login_num+1 where wx_id='"
							+ from_user + "'");
				}
			}
			Cookie cookie = new Cookie("wx_openid", from_user);
			cookie.setMaxAge(24 * 60 * 60 * 30);
			cookie.setPath("/");
			controller.setCookie(cookie);
		}
		
	}

}
