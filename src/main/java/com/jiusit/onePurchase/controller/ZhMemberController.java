package com.jiusit.onePurchase.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.jfinal.ApiController;
import com.jiusit.common.utils.DateUtil;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.model.PurchasePrizes;
import com.jiusit.onePurchase.model.PurchaseZhUser;

@ControllerBind(controllerKey = "/member", viewPath = "/pages")
/*
 * 招行会员活动
 */
public class ZhMemberController extends ApiController {
	private static final Logger log = Logger.getLogger(ZhMemberController.class);

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

	// 跳转到招行异兽卡活动页面
	public void forwardZhMember() {
		String zh_id = getCookie("zh_id");
		if(!StringUtil.isEmpty(zh_id)){
			PurchaseZhUser checkCookie = PurchaseZhUser.dao.findById(zh_id);
			if(checkCookie!=null){
				setAttr("checkCookie", checkCookie);
				setAttr("server_uri", PropKit.get("server_uri"));
				if(StringUtil.isEmpty(checkCookie.getStr("credential"))){
					render("basepage.html");
				}else{
					if(StringUtil.isEmpty(checkCookie.getStr("commodity_id"))){
						List<PurchasePrizes> pcList = PurchasePrizes.dao.find("select * from purchase_prizes where status='01' and commodity_type='01' and sort!='0' order by sort");
						setAttr("pcList", pcList);
						if(StringUtil.isEmpty(checkCookie.getStr("zh_card_no"))){
							render("userpage.html");
						}else{
							PurchasePrizes pp = PurchasePrizes.dao.findFirst("select * from purchase_prizes where sort='0'");
							if(pp!=null){
								setAttr("num", pp.getInt("version_stock"));
							}
							render("vippage.html");
						}
					}else{
						PurchaseZhUser pzu = PurchaseZhUser.dao.findFirst("select a.*,b.commodity_name,b.commodity_logo from purchase_zh_user a left join purchase_prizes b on a.commodity_id=b.id where a.id='"+zh_id+"'");
						setAttr("pzu", pzu);
						render("certificatepage.html");
					}
				}
			}else{
				setCookie("zh_id", null, 0, "/");
				render("basepage.html");
			}
		}else{
			render("basepage.html");
		}
	}
	public void photoUpload() {
		String zh_id = getCookie("zh_id");
		String img_data = getPara("formFile");
		PurchaseZhUser  pzu =PurchaseZhUser.dao.set("id", zh_id)
				.set("credential", img_data);
		
		pzu.update();
		renderJson("{\"status\":0}");
	}
	// 招行用户信息存储
	public void zhUser() {
		Map map=new HashMap();
		String user_name = getPara("user_name");
		String id_card = getPara("id_card");
		String telephone = getPara("telephone");
		String zh_card_no = getPara("zh_card_no");
		if(StringUtil.isEmpty(zh_card_no)){
			zh_card_no = null;
		}
		PurchaseZhUser check = PurchaseZhUser.dao.findFirst("select * from purchase_zh_user where id_card='"+id_card+"' or telephone='"+telephone+"'");
		if(check==null){
			String zh_id = StringUtil.getUUID();
			PurchaseZhUser pzu = PurchaseZhUser.dao
					.set("id", zh_id)
					.set("user_name", user_name)
					.set("id_card", id_card)
					.set("telephone", telephone)
					.set("zh_card_no", zh_card_no)
					.set("is_receive", "02")
					.set("credential", null)
					.set("create_by", telephone)
					.set("create_name", user_name)
					.set("create_date", new Date())
					.set("update_by", telephone)
					.set("update_name", user_name)
					.set("update_date", new Date())
					.set("remarks","添加");
			if(pzu.save()){
				Cookie cookie = new Cookie("zh_id", zh_id);
				cookie.setMaxAge(24 * 60 * 60 * 60);
				cookie.setPath("/");
				setCookie(cookie);
				map.put("code", 0);
				map.put("msg", "提交成功");
			}else{
				map.put("code", -1);
				map.put("msg", "系统繁忙，请稍后再试");
			}
		}else{
			if(StringUtil.isEmpty(check.getStr("commodity_id"))){
				map.put("code", 0);
				map.put("msg", "提交成功");
			}else{
				map.put("code", -2);
				map.put("msg", "已领取");
			}
		}
		renderJson(map);	
	}
	// 跳转到招行商品页面
	public void forwardZhCommodity() {
		String zh_id = getCookie("zh_id");
		if(!StringUtil.isEmpty(zh_id)){
			PurchaseZhUser pzu = PurchaseZhUser.dao.findById(zh_id);
			if(pzu!=null){
				List<PurchasePrizes> pcList = PurchasePrizes.dao.find("select * from purchase_prizes where status='01' and commodity_type='01' and sort!='0' order by sort");
				PurchasePrizes pp = PurchasePrizes.dao.findFirst("select * from purchase_prizes where sort='0'");
				if(pp!=null){
					setAttr("num", pp.getInt("version_stock"));
				}
				setAttr("server_uri", PropKit.get("server_uri"));
				setAttr("pcList", pcList);
				if(!StringUtil.isEmpty(pzu.getStr("zh_card_no"))){
					render("vippage.html");
				}else{
					render("userpage.html");
				}
			}else{
				setCookie("zh_id", null, 0, "/");
				render("basepage.html");
			}
		}else{
			render("basepage.html");
		}
	}
	// 领取衣服
	public void getZhTshit() {
		Map map=new HashMap();
		String zh_id = getCookie("zh_id");
		if(!StringUtil.isEmpty(zh_id)){
			PurchasePrizes pc = PurchasePrizes.dao.findFirst("select * from purchase_prizes where sort='0'");
			if(pc==null){
				map.put("code", -2);
				map.put("msg", "已领完");
			}else{
				PurchaseZhUser pzu = PurchaseZhUser.dao.findById(zh_id);
				if(pzu==null){
					map.put("code", -1);
					map.put("msg", "请先填写用户信息");
				}else{
					if(pc.getInt("version_stock")>0){
						if(pzu.getStr("is_receive").equals("02")){
							Db.update("update purchase_zh_user set commodity_id='"+pc.getStr("id")+"' where id='"+zh_id+"' ");
							PurchasePrizes pcNum = PurchasePrizes.dao.findFirst("select * from purchase_prizes where sort='0'");
							map.put("num", pcNum.getInt("version_stock"));
							map.put("code", 0);
							map.put("msg", "您成功领取了");
						}else{
							map.put("code", -2);
							map.put("msg", "已领取过商品");
						}
					}else{
						map.put("code", -3);
						map.put("msg", "已领完");
					}
				}
			}
		renderJson(map);
		}else{
			render("basepage.html");
		}
	}
	// 领取招行商品
	public void getZhCommodity() {
		Map map=new HashMap();
		String zh_id = getCookie("zh_id");
		String commodity_id = getPara("commodity_id");
		if(!StringUtil.isEmpty(zh_id)){
			if(!StringUtil.isEmpty(commodity_id)){
				PurchasePrizes pc = PurchasePrizes.dao.findFirst("select * from purchase_prizes where id='"+commodity_id+"'");
				if(pc==null){
					map.put("code", -2);
					map.put("msg", "已领完");
				}else{
					PurchaseZhUser pzu = PurchaseZhUser.dao.findById(zh_id);
					if(pzu==null){
						map.put("code", -1);
						map.put("msg", "请先填写用户信息");
					}else{
						if(pc.getInt("version_stock")>0){
							if(pzu.getStr("is_receive").equals("02")){
								Db.update("update purchase_zh_user set commodity_id='"+commodity_id+"' where id='"+zh_id+"' ");
								PurchasePrizes pcNum = PurchasePrizes.dao.findFirst("select * from purchase_prizes where id='"+commodity_id+"'");
								map.put("num", pcNum.getInt("version_stock"));
								map.put("code", 0);
								map.put("msg", "您成功领取了");
							}else{
								map.put("code", -2);
								map.put("msg", "已领取过商品");
							}
						}else{
							map.put("code", -3);
							map.put("msg", "已领完");
						}
					}
				}
			}else{
				map.put("code", -1);
				map.put("msg", "系统繁忙，请稍后再试");
			}
			renderJson(map);
		}else{
			render("basepage.html");
		}
	}
	/*跳转凭据页面*/
	public void forwardPinJu() {
		String zh_id = getCookie("zh_id");
		if(!StringUtil.isEmpty(zh_id)){
			PurchaseZhUser pzu = PurchaseZhUser.dao.findFirst("select a.*,b.commodity_name,b.commodity_logo from purchase_zh_user a left join purchase_prizes b on a.commodity_id=b.id where a.id='"+zh_id+"'");
			setAttr("pzu", pzu);
			setAttr("server_uri", PropKit.get("server_uri"));
			render("certificatepage.html");
		}else{
			render("basepage.html");
		}
	}
	/*确认领取*/
	public void confirmReceive() {
		Map map=new HashMap();
		String zh_id = getCookie("zh_id");
		if(!StringUtil.isEmpty(zh_id)){
			PurchaseZhUser  pzuUpdate =PurchaseZhUser.dao.set("id", zh_id)
					.set("is_receive", "01");
			pzuUpdate.update();
			PurchaseZhUser pzu = PurchaseZhUser.dao.findById(zh_id);
			Db.update("update purchase_prizes set version_stock=version_stock-1 where id='"+pzu.getStr("commodity_id")+"'");
			int msg = 0;
			if(pzu!=null){
				if(!StringUtil.isEmpty(pzu.getStr("zh_card_no"))){
					msg = 1;
				}
			}
			map.put("code", 0);
			map.put("msg", msg);
			renderJson(map);
		}else{
			render("basepage.html");
		}
	}
	/*返回主页*/
	public void returnIndex() {
		String zh_id = getCookie("zh_id");
		if(!StringUtil.isEmpty(zh_id)){
			setCookie("zh_id", null, 0, "/");
			render("basepage.html");
		}else{
			render("basepage.html");
		}
	}
}
