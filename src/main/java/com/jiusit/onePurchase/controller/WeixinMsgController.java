/**
 * Copyright (c) 2011-2014, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.jiusit.onePurchase.controller;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.kit.DateKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.TemplateMsgApi;
import com.jfinal.weixin.sdk.api.UserApi;
import com.jfinal.weixin.sdk.jfinal.MsgController;
import com.jfinal.weixin.sdk.msg.in.InImageMsg;
import com.jfinal.weixin.sdk.msg.in.InLinkMsg;
import com.jfinal.weixin.sdk.msg.in.InLocationMsg;
import com.jfinal.weixin.sdk.msg.in.InShortVideoMsg;
import com.jfinal.weixin.sdk.msg.in.InTextMsg;
import com.jfinal.weixin.sdk.msg.in.InVideoMsg;
import com.jfinal.weixin.sdk.msg.in.InVoiceMsg;
import com.jfinal.weixin.sdk.msg.in.event.InCustomEvent;
import com.jfinal.weixin.sdk.msg.in.event.InFollowEvent;
import com.jfinal.weixin.sdk.msg.in.event.InLocationEvent;
import com.jfinal.weixin.sdk.msg.in.event.InMassEvent;
import com.jfinal.weixin.sdk.msg.in.event.InMenuEvent;
import com.jfinal.weixin.sdk.msg.in.event.InQrCodeEvent;
import com.jfinal.weixin.sdk.msg.in.event.InTemplateMsgEvent;
import com.jfinal.weixin.sdk.msg.in.speech_recognition.InSpeechRecognitionResults;
import com.jfinal.weixin.sdk.msg.out.News;
import com.jfinal.weixin.sdk.msg.out.OutCustomMsg;
import com.jfinal.weixin.sdk.msg.out.OutImageMsg;
import com.jfinal.weixin.sdk.msg.out.OutNewsMsg;
import com.jfinal.weixin.sdk.msg.out.OutTextMsg;
import com.jiusit.common.utils.DataCoding;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.interceptor.UserInterceptor;
import com.jiusit.onePurchase.model.OnePurchaseUser;

/**
 * 将此 DemoController 在YourJFinalConfig 中注册路由，
 * 并设置好weixin开发者中心的 URL 与 token ，使 URL 指向该
 * DemoController 继承自父类 WeixinController 的 index
 * 方法即可直接运行看效果，在此基础之上修改相关的方法即可进行实际项目开发
 */
@ControllerBind(controllerKey="/msg",viewPath="/")
public class WeixinMsgController extends MsgController {

	static Logger logger = Logger.getLogger(WeixinMsgController.class);
	private static final String helpStr = "\t发送 help 可获得帮助，发送\"视频\" 可获取视频教程，发送 \"美女\" 可看美女，发送 music 可听音乐 ，发送新闻可看JFinal新版本消息。公众号功能持续完善中";
	
	/**
	 * 如果要支持多公众账号，只需要在此返回各个公众号对应的  ApiConfig 对象即可
	 * 可以通过在请求 url 中挂参数来动态从数据库中获取 ApiConfig 属性值
	 */
	public ApiConfig getApiConfig() {
		ApiConfig ac = new ApiConfig();
		// 配置微信 API 相关常量
		ac.setToken(PropKit.get("token"));
		ac.setAppId(PropKit.get("appId"));
		ac.setAppSecret(PropKit.get("appSecret"));
		/**
		 *  是否对消息进行加密，对应于微信平台的消息加解密方式：
		 *  1：true进行加密且必须配置 encodingAesKey
		 *  2：false采用明文模式，同时也支持混合模式
		 */
		ac.setEncryptMessage(PropKit.getBoolean("encryptMessage", false));
		ac.setEncodingAesKey(PropKit.get("encodingAesKey", "setting it in config file"));
		return ac;
	}
	

	protected void processInTextMsg(InTextMsg inTextMsg)
	{
		//转发给多客服PC客户端
		logger.debug("关键字：" + inTextMsg.getContent());
		String key=inTextMsg.getContent();
		String from_user=inTextMsg.getFromUserName();
		String host_uri=PropKit.get("host_uri");
		String server_uri=PropKit.get("server_uri");
		/*Keyword keyword=Keyword.dao.findFirst("select * from yxg_keyword_call where key_word=?",key);
		if(keyword!=null){
			News news=new News(keyword.getStr("title"), 
					keyword.getStr("des"), 
					server_uri+"/"+keyword.getStr("img_path"), 
					host_uri+keyword.getStr("forward_path")+"?from_user="+from_user);
			OutNewsMsg newMsg=new OutNewsMsg(inTextMsg);
			newMsg.addNews(news);
			render(newMsg);
		}else{
			OutCustomMsg outCustomMsg = new OutCustomMsg(inTextMsg);
			render(outCustomMsg);
		}*/
	}

	@Override
	protected void processInVoiceMsg(InVoiceMsg inVoiceMsg)
	{
		//转发给多客服PC客户端
		OutCustomMsg outCustomMsg = new OutCustomMsg(inVoiceMsg);
		render(outCustomMsg);
	}

	@Override
	protected void processInVideoMsg(InVideoMsg inVideoMsg)
	{
		//转发给多客服PC客户端
		OutCustomMsg outCustomMsg = new OutCustomMsg(inVideoMsg);
		render(outCustomMsg);
	}

	@Override
	protected void processInShortVideoMsg(InShortVideoMsg inShortVideoMsg)
	{
		//转发给多客服PC客户端
		OutCustomMsg outCustomMsg = new OutCustomMsg(inShortVideoMsg);
		render(outCustomMsg);
	}

	@Override
	protected void processInLocationMsg(InLocationMsg inLocationMsg)
	{
		//转发给多客服PC客户端
		OutCustomMsg outCustomMsg = new OutCustomMsg(inLocationMsg);
		render(outCustomMsg);
	}

	@Override
	protected void processInLinkMsg(InLinkMsg inLinkMsg)
	{
		//转发给多客服PC客户端
		OutCustomMsg outCustomMsg = new OutCustomMsg(inLinkMsg);
		render(outCustomMsg);
	}

	@Override
	protected void processInCustomEvent(InCustomEvent inCustomEvent)
	{
		logger.debug("测试方法：processInCustomEvent()");
		renderNull();
	}

	protected void processInImageMsg(InImageMsg inImageMsg)
	{
		//转发给多客服PC客户端
		OutCustomMsg outCustomMsg = new OutCustomMsg(inImageMsg);
		render(outCustomMsg);
	}

	/**
	 * 实现父类抽方法，处理关注/取消关注消息
	 */
	protected void processInFollowEvent(InFollowEvent inFollowEvent)
	{
		String from_user=inFollowEvent.getFromUserName();
		
		if (InFollowEvent.EVENT_INFOLLOW_SUBSCRIBE.equals(inFollowEvent.getEvent()))
		{
			Db.update("update purchase_users set is_follow='1' where wx_id='"+from_user+"'");
			logger.debug("关注：" + inFollowEvent.getFromUserName());
			
			OutTextMsg outMsg = new OutTextMsg(inFollowEvent);
			outMsg.setContent("感谢您的关注！米力社将努力为您打造更低本、高效、便捷的购物平台!"); 
			//outMsg.setContent("您好，欢迎您关注羽林盟，请问您需要什么帮助？\r\n"+
					//"1.我要维修\r\n"+
					//"2.下单流程\r\n"+
					//"3.维修标准\r\n"+
					//"4.平台简介\r\n"+
			//"您可以回复序号了解相关内容，如有其它需要请联系我们客服！热线：0591-88601860");
			render(outMsg);
		}
		// 如果为取消关注事件，将无法接收到传回的信息
		if (InFollowEvent.EVENT_INFOLLOW_UNSUBSCRIBE.equals(inFollowEvent.getEvent()))
		{
			Db.update("update purchase_users set is_follow='0' where wx_id='"+from_user+"'");
			/*onePurchaseUser purchaseUser = new onePurchaseUser()
			.set("id", user.get("id"))
			.set("is_follow","0")
			;
	        purchaseUser.update();*/
			logger.debug("取消关注：" + inFollowEvent.getFromUserName());
		}
	}

	@Override
	protected void processInQrCodeEvent(InQrCodeEvent inQrCodeEvent)
	{
		if (InQrCodeEvent.EVENT_INQRCODE_SUBSCRIBE.equals(inQrCodeEvent.getEvent()))
		{
			logger.debug("扫码未关注：" + inQrCodeEvent.getFromUserName());
			OutTextMsg outMsg = new OutTextMsg(inQrCodeEvent);
			outMsg.setContent("感谢您的关注，二维码内容：" + inQrCodeEvent.getEventKey());
			render(outMsg);
		}
		if (InQrCodeEvent.EVENT_INQRCODE_SCAN.equals(inQrCodeEvent.getEvent()))
		{
			logger.debug("扫码已关注：" + inQrCodeEvent.getFromUserName());
		}
	}

	@Override
	protected void processInLocationEvent(InLocationEvent inLocationEvent)
	{
		logger.debug("发送地理位置事件：" + inLocationEvent.getFromUserName());
		OutTextMsg outMsg = new OutTextMsg(inLocationEvent);
		outMsg.setContent("地理位置是：" + inLocationEvent.getLatitude());
		//设置用户坐标，用户进行对话时上报一次
		OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id=?",inLocationEvent.getFromUserName());
		if(user!=null){
			user.set("longitude", inLocationEvent.getLongitude());
			user.set("latitude", inLocationEvent.getLatitude());
			user.update();
		}
		renderNull();
	}

	@Override
	protected void processInMassEvent(InMassEvent inMassEvent)
	{
		logger.debug("测试方法：processInMassEvent()");
		renderNull();
	}

	/**
	 * 实现父类抽方法，处理自定义菜单事件
	 */
	protected void processInMenuEvent(InMenuEvent inMenuEvent)
	{
		logger.debug("菜单事件：" + inMenuEvent.getFromUserName());
		OutTextMsg outMsg = new OutTextMsg(inMenuEvent);
		outMsg.setContent("菜单事件内容是：" + inMenuEvent.getEventKey());
		render(outMsg);
	}

	@Override
	protected void processInSpeechRecognitionResults(InSpeechRecognitionResults inSpeechRecognitionResults)
	{
		logger.debug("语音识别事件：" + inSpeechRecognitionResults.getFromUserName());
		OutTextMsg outMsg = new OutTextMsg(inSpeechRecognitionResults);
		outMsg.setContent("语音识别内容是：" + inSpeechRecognitionResults.getRecognition());
		render(outMsg);
	}

	@Override
	protected void processInTemplateMsgEvent(InTemplateMsgEvent inTemplateMsgEvent)
	{
		logger.debug("测试方法：processInTemplateMsgEvent()");
		renderNull();
	}
	
	/*
	*//**
	 * 接收个人工单状态变更通知接口
	 * http://w2yong77.vicp.cc/msg/personalOrderState      参数：order_id    order_state 
	 *//*
	public void personalOrderState() {
		String order_id = getPara("order_id");
		String order_state = getPara("order_state");
		String order_state_str="";
		logger.info("personalState: " + order_id);
		logger.info("personalState: " + order_state);
		Map res=new HashMap();
		try {
			if(order_id==null || order_state==null){
				res.put("code", -1);
				res.put("msg", "参数异常");
			}else{
				String order_status_s=order_state;
				String content="";
				String url="";
				String host_uri=PropKit.get("host_uri");
				//根据orderId获取相关数据
				PersonnelOrders orders=PersonnelOrders.dao.findById(order_id);
				RepairWorker rw=RepairWorker.dao.findById(orders.getStr("repair_worker_id"));
				User user=User.dao.findById(orders.get("apply_user"));
				if(order_state.equals("04")){
					content="尊敬的客人，您好，依修哥已接单….";
					order_state_str="服务预约派单通知";
				}else if(order_state.equals("05")){
					content="您好，您的订单已完成报价…. 请进入我的工单进行付款。";
					order_state_str="服务订单报价通知";
				}else if(order_state.equals("06")){
					content="您好，您的订单已开始维修，详细情况请进入我的工单查看。";
					order_state_str="服务订单维修通知";
				}else if(order_state.equals("07")){
					content="尊敬的客人，您好，您的家电依修哥已维修完成，请确认….";
					order_state_str="服务订单维修完成通知";
				}else if(order_state.equals("10")){
					content="尊敬的客人，您好，您的订单配件需要从厂家寄出，请耐心等待….";
					order_state_str="服务订单待件通知";
				}else if(order_state.equals("td")){
					content="您好，依修哥已同意您的退单申请….";
					order_state_str="服务取消通知";
				}else if(order_state.equals("gp")){
					content="您好，依修哥已同意您的改派申请….";
					order_state_str="改派人员通知";
				}
				Map msg=new HashMap();
				msg.put("wxg", rw.getStr("real_name"));
				msg.put("wxgdh", rw.getStr("telephone"));
				msg.put("url", host_uri+"/user/myGdList?from_user="+user.getStr("wx_id")+"&order_status="+order_state);
				msg.put("order_state_str", order_state_str);
				msg.put("content", content);
				msg.put("orderNo", orders.get("order_no"));
				msg.put("repair_type", orders.get("repair_type"));
				this.sendMsg(user.getStr("wx_id"), msg);
				res.put("code", 0);
				res.put("msg", "");
				renderJson(res);
			}
		} catch (Exception e) {
			e.printStackTrace();
			res.put("code", -1);
			res.put("msg", "接口异常");
			renderJson(res);
		}
	}
	
	
	*//**
	 * 接收个人工单状态变更通知接口
	 * http://w2yong77.vicp.cc/msg/teamOrderState      参数：order_id    order_state  
	 *//*
	public void teamOrderState() {
		String order_id = getPara("order_id");
		String order_state = getPara("order_state");
		String order_state_str="";
		logger.info("personalState: " + order_id);
		logger.info("personalState: " + order_state);
		Map res=new HashMap();
		try {
			if(order_id==null || order_state==null){
				res.put("code", -1);
				res.put("msg", "参数异常");
			}else{
				String order_status_s=order_state;
				String content="";
				String url="";
				String host_uri=PropKit.get("host_uri");
				//根据orderId获取相关数据
				TeamOrders orders=TeamOrders.dao.findById(order_id);
				//获取维修工电话
				TeamList rw=TeamList.dao.findById(orders.get("team_id"));
				User user=User.dao.findById(orders.get("apply_user"));
				if(order_state.equals("05")){
					content="尊敬的客人，您好，维修团队已接单….";
					order_state_str="服务预约派单通知";
				}else if(order_state.equals("06")){
					content="您好，您的订单已完成报价…. 请进入我的工单进行付款。";
					order_state_str="服务订单报价通知";
				}else if(order_state.equals("07")){
					content="您好，您的订单已开始维修，详细情况请进入我的工单查看。";
					order_state_str="服务订单维修通知";
				}else if(order_state.equals("08")){
					content="尊敬的客人，您好，您的家电依修哥已维修完成，请确认….";
					order_state_str="服务订单维修完成通知";
				}else if(order_state.equals("11")){
					content="尊敬的客人，您好，您的订单配件需要从厂家寄出，请耐心等待….";
					order_state_str="服务订单维修完成通知";
				}else if(order_state.equals("td")){
					content="您好，依修哥已同意您的退单申请….";
					order_state_str="服务取消通知";
				}else if(order_state.equals("gp")){
					content="您好，依修哥已同意您的改派申请….";
					order_state_str="改派人员通知";
				}
				Map msg=new HashMap();
				msg.put("wxg", rw.getStr("team_name"));
				msg.put("wxgdh", rw.getStr("telephone"));
				msg.put("url", host_uri+"/team/myGdList?from_user="+user.getStr("wx_id")+"&order_status="+order_state);
				msg.put("order_state_str", order_state_str);
				msg.put("content", content);
				msg.put("orderNo", orders.get("order_no"));
				msg.put("repair_type", orders.get("repair_type"));
				this.sendMsg(user.getStr("wx_id"), msg);
				res.put("code", 0);
				res.put("msg", "");
				renderJson(res);
			}
		} catch (Exception e) {
			e.printStackTrace();
			res.put("code", -1);
			res.put("msg", "接口异常");
			renderJson(res);
		}
	}
	
	*//**
	 * 发送模板消息
	 *//*
	public void sendMsg(String toUser,Map msg)
	{
		ApiConfigKit.setThreadLocalApiConfig(this.getApiConfig());
		String tempId=PropKit.get("pushTempId");
		//DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//String curdate=df.format(new Date());
		String str = " {\n" +
				"           \"touser\":\""+toUser+"\",\n" +
				"           \"template_id\":\""+tempId+"\",\n" +
				"           \"url\":\""+msg.get("url")+"\",\n" +
				"           \"topcolor\":\"#FF0000\",\n" +
				"           \"data\":{\n" +
				"                   \"first\": {\n" +
				"                       \"value\":\""+msg.get("order_state_str")+"\",\n" +
				"                       \"color\":\"#173177\"\n" +
				"                   },\n" +
				"                   \"keyword1\":{\n" +
				"                       \"value\":\""+msg.get("orderNo")+"\",\n" +
				"                       \"color\":\"#173177\"\n" +
				"                   },\n" +
				"                   \"keyword2\":{\n" +
				"                       \"value\":\""+msg.get("content")+"\",\n" +
				"                       \"color\":\"#173177\"\n" +
				"                   },\n" +
				"                   \"keyword3\":{\n" +
				"                       \"value\":\""+msg.get("repair_type")+"\",\n" +
				"                       \"color\":\"#173177\"\n" +
				"                   },\n" +
				"                   \"keyword4\":{\n" +
				"                       \"value\":\""+msg.get("wxg")+"\",\n" +
				"                       \"color\":\"#173177\"\n" +
				"                   },\n" +
				"                   \"keyword5\":{\n" +
				"                       \"value\":\""+msg.get("wxgdh")+"\",\n" +
				"                       \"color\":\"#173177\"\n" +
				"                   },\n" +
				"                   \"remark\":{\n" +
				"                       \"value\":\"维修费用请参考平台收费标准，如果您有任何问题或投诉建议，欢迎拨打客服电话：0591-88601860\",\n" +
				"                       \"color\":\"#173177\"\n" +
				"                   }\n" +
				"           }\n" +
				"       }";
		ApiResult apiResult = TemplateMsgApi.send(str);
		renderText(apiResult.getJson());
	}
	
	
	*//**
	 * 接收企业审核状态变更通知接口
	 * http://w2yong77.vicp.cc/msg/companyState      
	 * 参数：
	 *   company_id 企业ID  company_name 企业名称   
	 *   verify_time 审核时间   is_verify 审核状态
	 *//*
	public void companyState() {
		String company_id = getPara("company_id");
		String company_name = getPara("company_name");
		String verify_time = getPara("verify_time");
		String is_verify=getPara("is_verify");
		Map res=new HashMap();
		try {
			if(company_id==null || is_verify==null){
				res.put("code", -1);
				res.put("msg", "参数异常");
				renderJson(res);
			}else{
				UserCompany company=UserCompany.dao.findById(company_id);
				User user=User.dao.findById(company.get("wx_users_id"));
				String host_uri=PropKit.get("host_uri");
				Map msg=new HashMap();
				msg.put("title", "企业信息审核结果通知");
				msg.put("time", verify_time);
				msg.put("url", host_uri+"/user/companyEdit?from_user="+user.getStr("wx_id"));
				msg.put("name", company_name);
				String remark="";
				if(is_verify.equals("01")){
					remark="尊敬的用户您好，您的企业信息已通过依修哥平台审核。感谢您对依修哥的支持。如果您有任何问题或投诉建议，欢迎拨打客服电话：0591-88601860";
				}else{
					remark="尊敬的用户您好，您的企业信息未通过依修哥平台审核。请修改企业信息后重新提交审核。如果您有任何问题或投诉建议，欢迎拨打客服电话：0591-88601860";
				}
				msg.put("remark", remark);
				this.sendCompanyMsg(user.getStr("wx_id"), msg);
				res.put("code", 0);
				res.put("msg", "");
				renderJson(res);
			}
		} catch (Exception e) {
			e.printStackTrace();
			res.put("code", -1);
			res.put("msg", "接口异常");
			renderJson(res);
		}
	}*/

	/**
	 * 发送模板消息
	 */
	public void sendCompanyMsg(String toUser,Map msg)
	{
		ApiConfigKit.setThreadLocalApiConfig(this.getApiConfig());
		String tempId=PropKit.get("pushTempId2");
		//DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//String curdate=df.format(new Date());
		String str = " {\n" +
				"           \"touser\":\""+toUser+"\",\n" +
				"           \"template_id\":\""+tempId+"\",\n" +
				"           \"url\":\""+msg.get("url")+"\",\n" +
				"           \"topcolor\":\"#FF0000\",\n" +
				"           \"data\":{\n" +
				"                   \"first\": {\n" +
				"                       \"value\":\""+msg.get("title")+"\",\n" +
				"                       \"color\":\"#173177\"\n" +
				"                   },\n" +
				"                   \"keyword1\":{\n" +
				"                       \"value\":\""+msg.get("time")+"\",\n" +
				"                       \"color\":\"#173177\"\n" +
				"                   },\n" +
				"                   \"keyword2\":{\n" +
				"                       \"value\":\""+msg.get("name")+"\",\n" +
				"                       \"color\":\"#173177\"\n" +
				"                   },\n" +
				"                   \"remark\":{\n" +
				"                       \"value\":\""+msg.get("remark")+"\",\n" +
				"                       \"color\":\"#173177\"\n" +
				"                   }\n" +
				"           }\n" +
				"       }";
		ApiResult apiResult = TemplateMsgApi.send(str);
		renderText(apiResult.getJson());
	}
}