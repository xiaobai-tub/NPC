package com.jiusit.onePurchase.controller;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.jfinal.ApiController;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.interceptor.UserInterceptor;
import com.jiusit.onePurchase.model.PurchaseCommodity;
import com.jiusit.onePurchase.model.PurchaseCommodityCar;
import com.jiusit.onePurchase.model.OnePurchaseUser;

@ControllerBind(controllerKey="/share",viewPath="/pages")
/*
 * 分享
 * */
public class ShareController extends ApiController{
	private static final Logger log = Logger
			.getLogger(ShareController.class);

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

	public void wxShare(){
		String callback = getPara("jsoncallback"); // 支持Jsonp调用tjr_id
		String reqUrl = getPara("reqUrl");
		String id="";
		String commodity_type="";
		String title="闽南异兽志初代T恤—短䘼衫上线 ！";
		String desc="神兽伏身，冰丝护体，热力强势压制，结界全开！自此夏虫语冰，隆暑不汗，宝宝甚是身凉。";
		String from_user=getCookie("wx_openid");
		if(reqUrl.contains("id")){
			id=reqUrl.substring(reqUrl.indexOf("id")+3,reqUrl.indexOf("id")+35);
			if(reqUrl.contains("commodityDetail")){
				commodity_type="01";
			}
			if(reqUrl.contains("getOnePurchase")){
				commodity_type="02";
			}
		}
		if(reqUrl.contains("agent_num")){
			String agent_num=reqUrl.substring(reqUrl.indexOf("agent_num")+10,reqUrl.indexOf("agent_num")+75);
			if(!StringUtil.isEmpty(agent_num)){
				String[] agentNums=agent_num.split(",");
				if(agentNums.length>=2){
					id=agentNums[1];
				}
			}
				commodity_type="01";
		}
		Map ret = new HashMap();
		String imgUrl=PropKit.get("host_uri", "setting it in config file")
				+ "/onePurchase_wx/resource/images/share.jpg";
		try {
			/* 微信JSSDK使用 */
			UUID uuid = UUID.randomUUID();
			String noncestr = uuid.toString().replace("-", "");
			String timestamp = Long.toString(System.currentTimeMillis() / 1000);
			String signStr = new StringBuilder().append(
					"jsapi_ticket=" + TicketUtil.getJssdkTicket().getTicket()
					        +"&noncestr=" + noncestr + "&timestamp="
							+ timestamp+"&url="+reqUrl).toString();
			signStr = HashKit.sha1(signStr);
			System.out.println("jssdk sign=" + signStr);
			String sql="";
            if(!StringUtil.isEmpty(id)){
            	if(StringUtil.isEqual(commodity_type, "01")){
            		sql="select * from purchase_commodity where id ='"+id+"'";
            	}
            	if(StringUtil.isEqual(commodity_type, "02")){
            		sql="select * from purchase_commodity where id =(select commodity_id from purchase_time_slot where id='"+id+"')";
            	}
            	if(!StringUtil.isEmpty(sql)){
            	PurchaseCommodity commodity=PurchaseCommodity.dao.findFirst(sql);
            	if(commodity != null){
            		imgUrl=PropKit.get("server_uri", "setting it in config file")
            				+commodity.get("commodity_logo");
            		title=commodity.get("commodity_name");
            		desc="【闽南异兽志~】"+title+"！12只闽南异兽终于全部解封，神兽伏身，冰丝护体，热力强势压制，结界全开！每一只都代表了一个闽南人最熟悉的声音，其技能各异、形象怪诞乖萌，等你来集结～";
            	}
            	}
            }
			Map wxConfig = new HashMap();
			wxConfig.put("appId", getApiConfig().getAppId());
			wxConfig.put("nonceStr", noncestr);
			wxConfig.put("timestamp", timestamp);
			wxConfig.put("signature", signStr);

			Map share = new HashMap();
			share.put("title", title);
			share.put("desc", desc);
			share.put("link", reqUrl);
			share.put("imgUrl",imgUrl);
			ret.put("code", "0");
			ret.put("wxConfig", wxConfig);
			ret.put("share", share);
		} catch (Exception e) {
			ret.put("msg", e.getMessage());
		}

		if (StrKit.isBlank(callback)) {
			renderJson(ret);
		} else {
			renderJson(callback + "(" + JsonKit.toJson(ret) + ")");
		}
	}
	
	public void ticketSign(){
		String callback = getPara("jsoncallback"); // 支持Jsonp调用tjr_id
		String reqUrl = getPara("reqUrl");
		String card_id = getPara("card_id");
		Map ret = new HashMap();
		try {
			/* 微信JSSDK使用 */
			UUID uuid = UUID.randomUUID();
			String noncestr = uuid.toString().replace("-", "");
			String timestamp = Long.toString(System.currentTimeMillis() / 1000);
			String ticket=TicketUtil.getCardTicket().getTicket();
			String signStr = new StringBuilder().append(
					timestamp+ticket+card_id).toString();
            System.out.println("sign string=" + signStr);
			signStr = HashKit.sha1(signStr);
			System.out.println("jssdk sign=" + signStr);
			Map wxConfig = new HashMap();
			wxConfig.put("nonceStr", noncestr);
			wxConfig.put("timestamp", timestamp);
			wxConfig.put("signature", signStr);
			ret.put("code", 0);
			ret.put("wxConfig", wxConfig);
		} catch (Exception e) {
			ret.put("msg", e.getMessage());
			System.out.println(e.getMessage());
		}
		if (StrKit.isBlank(callback)) {
			renderJson(ret);
		} else {
			renderJson(callback + "(" + JsonKit.toJson(ret) + ")");
		}
	}
	

	

}
