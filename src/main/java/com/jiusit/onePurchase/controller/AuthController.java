package com.jiusit.onePurchase.controller;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.jfinal.ApiController;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.interceptor.AuthInterceptor;
import com.jiusit.onePurchase.model.PurchaseCommodity;
import com.jiusit.onePurchase.model.PurchaseCommodityCar;
import com.jiusit.onePurchase.model.OnePurchaseUser;
import com.jiusit.onePurchase.model.PurchaseTicket;
import com.jiusit.onePurchase.model.PurchaseTicketRandom;
import com.jiusit.onePurchase.model.WbVar;
import com.jiusit.onePurchase.service.StoredProcedureService;

@ControllerBind(controllerKey="/auth",viewPath="/pages")
/*
 * 分享
 * */
public class AuthController extends ApiController{
	private static final Logger log = Logger
			.getLogger(AuthController.class);

	@Override
	public ApiConfig getApiConfig() {
		ApiConfig ac = new ApiConfig();

		// 配置微信 API 相关常量
		ac.setToken(PropKit.get("token"));
		ac.setAppId(PropKit.get("appId"));
		ac.setAppSecret(PropKit.get("appSecret"));

		/**
		 * 是否对消息进行加密，对应于微信平台的消息加解密方式： 1：true进行加密且必须配置 encodingAesKey
		 * 2：false采用明文模式，同时也支持混合模式
		 */
		ac.setEncryptMessage(PropKit.getBoolean("encryptMessage", false));
		ac.setEncodingAesKey(PropKit.get("encodingAesKey",
				"setting it in config file"));
		return ac;
	}

	public void getUserInfo(){
	            String code=getPara("code");
	            String card_code=getPara("card_code");
	            String url=getPara("url");
	            String c=getPara("c");
	            String userInfo="";
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
				Map data;
				try {
					data = mapper.readValue(userAccessTokenData, Map.class);
					if (data.get("openid") != null) {
						String openId = data.get("openid").toString();
						String access_token = data.get("access_token").toString();
						userInfo = HttpKit
									.get("https://api.weixin.qq.com/sns/userinfo?access_token="
											+ access_token
											+ "&openid="
											+ openId + "&lang=zh_CN");
					 }else{
						 log.debug("get openId fail ");
					   }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			Map<String, Object> map=null;	
			map = StringUtil.getMap(userInfo);
			String nickname = (String) map.get("nickname");
			nickname = URLEncoder.encode(nickname);
			Integer sex = (Integer) map.get("sex");
			String headimgurl = (String) map.get("headimgurl");
			if (!StringUtil.isEmpty(headimgurl)) {
				StringUtil.replace(headimgurl, "\\/", "/");
			}
			
			String user_id = StringUtil.getUUID();
			String openId=map.get("openid").toString();
			OnePurchaseUser user = OnePurchaseUser.dao.findFirst(
					"select * from purchase_users where wx_id=?",
					openId);
			if(user == null ){
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
			.set("agent_code", "")
			.set("create_by", user_id)
			.set("create_name", nickname)
			.set("create_date", new Date())
			.set("update_by", user_id)
			.set("update_name", nickname)
			.set("update_date", new Date());
	        purchaseUser.save();
			}
			setCookie("wx_openid", openId, 24 * 60 * 60 * 30, "/");
			String redirect_url=url;
			if(!StringUtil.isEmpty(c)){
				redirect_url+="&c="+c;
			}
			redirect_url+="&nickname="+URLEncoder.encode((String)map.get("nickname"))+"&headimgurl="+map.get("headimgurl")+"&openid="+map.get("openid");
			if(!StringUtil.isEmpty(card_code)){
				redirect_url+="&card_code="+card_code;
			}
			
			redirect(redirect_url);
	}
	public void Auth(){
		String card_code=getPara("card_code");
		String url=getPara("url");
		String c=getPara("c");
		String redirect_uri1_0="/onePurchase_wx/auth/getUserInfo";
		if(!StringUtil.isEmpty(url)){
			   redirect_uri1_0+="?url="+url;
		}
		if(!StringUtil.isEmpty(card_code)){
			   redirect_uri1_0+="&card_code="+card_code;
		}
		if(!StringUtil.isEmpty(c)){
			   redirect_uri1_0+="&c="+c;
		}
		try {
			redirect_uri1_0 = URLEncoder.encode(PropKit.get("host_uri",
					"setting it in config file")+redirect_uri1_0,
					"utf-8");
			String m1_0 = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
					+ PropKit.get("appId")
					+ "&redirect_uri="
					+ redirect_uri1_0
					+ "&response_type=code&scope=snsapi_userinfo&state=defaultToHome#wechat_redirect";
			redirect(m1_0);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	//会员赠送卡劵
	public void userVip(){
		String callback = getPara("jsoncallback");
		String openId=getPara("openid");
		String telephone=getPara("telephone");
		Map<String,String> map =new HashMap<String,String>();
		OnePurchaseUser user = OnePurchaseUser.dao.findFirst(
				"select * from purchase_users where wx_id=?",
				openId);
		if(user != null ){
			//开卡红包
			PurchaseTicket opendCardTicketSql=PurchaseTicket.dao.findFirst("SELECT a.* FROM purchase_ticket a  WHERE a.STATUS='1' AND a.ticket_type='01' AND a.card_category='04' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(a.begin_date) AND TO_DAYS(a.end_date)");
			if(opendCardTicketSql != null){
				PurchaseTicketRandom purchaseTicketRandom = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_status='01' and ticket_id='"+opendCardTicketSql.getStr("id")+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ORDER BY rand() limit 0,1 ");
			    if(purchaseTicketRandom != null ){
					  StoredProcedureService.trackresult(purchaseTicketRandom.getStr("id"),openId,user.getStr("id"),user.getStr("nick_name"),new BigDecimal("0"),"05",2,3,"01");
			    }
			}
			//会员卡劵
			PurchaseTicket vipTicketSql=PurchaseTicket.dao.findFirst("SELECT a.* FROM purchase_ticket a  WHERE a.STATUS='1' AND a.ticket_type='01' AND a.card_category='03' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(a.begin_date) AND TO_DAYS(a.end_date)");
			if(vipTicketSql != null){
				PurchaseTicketRandom purchaseTicketRandom = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_status='01' and ticket_id='"+vipTicketSql.getStr("id")+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ORDER BY rand() limit 0,1 ");
			    if(purchaseTicketRandom != null ){
					  StoredProcedureService.trackresult(purchaseTicketRandom.getStr("id"),openId,user.getStr("id"),user.getStr("nick_name"),new BigDecimal("0"),"05",2,3,"01");
			    }
			}
			//生日寿礼
			PurchaseTicket birthdayTicketSql=PurchaseTicket.dao.findFirst("SELECT a.* FROM purchase_ticket a  WHERE a.STATUS='1' AND a.ticket_type='01' AND a.card_category='05' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(a.begin_date) AND TO_DAYS(a.end_date)");
			if(birthdayTicketSql != null){
				PurchaseTicketRandom purchaseTicketRandom = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_status='01' and ticket_id='"+birthdayTicketSql.getStr("id")+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ORDER BY rand() limit 0,1 ");
			    if(purchaseTicketRandom != null ){
					  StoredProcedureService.trackresult(purchaseTicketRandom.getStr("id"),openId,user.getStr("id"),user.getStr("nick_name"),new BigDecimal("0"),"05",2,3,"01");
			    }
			}
			//新品体验券
			PurchaseTicket newTicketSql=PurchaseTicket.dao.findFirst("SELECT a.* FROM purchase_ticket a  WHERE a.STATUS='1' AND a.ticket_type='01' AND a.card_category='02' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(a.begin_date) AND TO_DAYS(a.end_date)");
			if(newTicketSql != null){
				PurchaseTicketRandom purchaseTicketRandom = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_status='01' and ticket_id='"+newTicketSql.getStr("id")+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ORDER BY rand() limit 0,1 ");
			    if(purchaseTicketRandom != null ){
					  StoredProcedureService.trackresult(purchaseTicketRandom.getStr("id"),openId,user.getStr("id"),user.getStr("nick_name"),new BigDecimal("0"),"05",1,1,"01");
			    }
			}
			Db.update("update purchase_users set user_telephone='"+telephone+"',vip_identify='02' where wx_id='"+openId+"'");
			map.put("code", "0");
			map.put("msg", "恭喜您,成为米力社的会员!");
		}else{
			map.put("code", "1");
			map.put("msg", "用户不存在");
		}
		JSONObject jsonObj = new JSONObject(map);
		renderJson(callback + "(" + jsonObj.toString() + ")");
	}
	
	//转盘赠送卡劵
		public void wheel(){
			String callback = getPara("jsoncallback");
			String openId=getPara("openid");
			Map<String,String> map =new HashMap<String,String>();
			OnePurchaseUser user = OnePurchaseUser.dao.findFirst(
					"select * from purchase_users where wx_id=?",
					openId);
			if(user != null ){
				WbVar wv = WbVar.dao.findFirst("select * from wb_var where  VAR_NAME='wheel_ticket_id'");
				if(wv != null ){
				PurchaseTicket opendCardTicketSql=PurchaseTicket.dao.findFirst("SELECT a.* FROM purchase_ticket a  WHERE a.STATUS='1' AND a.ticket_type='01' and id='"+wv.getStr("VAR_VALUE")+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(a.begin_date) AND TO_DAYS(a.end_date)");
				if(opendCardTicketSql != null){
					PurchaseTicketRandom purchaseTicketRandom = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_status='01' and ticket_id='"+opendCardTicketSql.getStr("id")+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ORDER BY rand() limit 0,1 ");
				    if(purchaseTicketRandom != null ){
						  int result=StoredProcedureService.trackresult(purchaseTicketRandom.getStr("id"),openId,user.getStr("id"),user.getStr("nick_name"),new BigDecimal("0"),"06",2,3,"01");
						  if(result == 1){
							  map.put("code", "0");
							  map.put("msg", "恭喜您,获得"+purchaseTicketRandom.getStr("ticket_title")+"!");
						  }else{
							  map.put("code", "2");
							  map.put("msg", "系统繁忙，请稍后再试!");
						  }
				    }else{
						  map.put("code", "3");
						  map.put("msg", "卡劵已发放完");
					  }
				}else{
					  map.put("code", "4");
					  map.put("msg", "卡劵已发放完");
				  }
				}else{
					map.put("code", "5");
					map.put("msg", "暂无卡劵");
				}
			}else{
				map.put("code", "1");
				map.put("msg", "用户不存在");
			}
			JSONObject jsonObj = new JSONObject(map);
			renderJson(callback + "(" + jsonObj.toString() + ")");
		}
}
