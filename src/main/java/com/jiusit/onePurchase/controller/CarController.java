package com.jiusit.onePurchase.controller;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.jfinal.ApiController;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.interceptor.UserInterceptor;
import com.jiusit.onePurchase.model.PurchaseCommodity;
import com.jiusit.onePurchase.model.PurchaseCommodityCar;
import com.jiusit.onePurchase.model.OnePurchaseUser;
@ControllerBind(controllerKey="/car",viewPath="/pages")
/*
 * 普通商品
 * */
public class CarController extends ApiController{
	private static final Logger log = Logger
			.getLogger(CarController.class);

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

	public void carList(){
		String from_user=getCookie("wx_openid");
		if(!StringUtil.isEmpty(from_user)){
		String commodity_type = getPara("commodity_type");
		OnePurchaseUser opu = OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id = '"+from_user+"'");
		String commodity = "";
		String oneCommodity = "";
		
		if(null ==commodity_type ||"".equals(commodity_type)){
			commodity_type = "01";
			commodity = "on";
		}else{
			oneCommodity = "on";
		}
			
		if(opu!=null){
			setAttr("opu",opu);
		}
		//商品列表
		List<PurchaseCommodityCar> commoditylist = PurchaseCommodityCar.dao.find("select * from purchase_commodity_car where user_id = '"+opu.getStr("id")+"' and order_type='01' and commodity_type = '01' order by create_date desc ");
		setAttr("commoditylist", commoditylist);
		//一元购商品
		List<PurchaseCommodityCar> onetylist = PurchaseCommodityCar.dao.find("select a.id,a.commodity_name,a.commodity_logo,a.version_type,a.buy_num,b.total_count,b.surplus_count from purchase_commodity_car a left join purchase_time_slot b on a.time_slot=b.id where a.user_id = '"+opu.getStr("id")+"' and a.order_type='01' and a.commodity_type = '02' order by a.create_date desc");
		setAttr("commodity", commodity);
		setAttr("oneCommodity", oneCommodity);
		setAttr("onetylist", onetylist);
		setAttr("server_uri", PropKit.get("server_uri"));
		setAttr("commodity_type",commodity_type);
		render("cart.html");
		}else{
			String url=getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
			render("error.html");
		}
	}
	//删除
	public void delList(){
		String cartId = getPara("cartId");
		String from_user=getCookie("wx_openid");
		if(!StringUtil.isEmpty(from_user)){
		OnePurchaseUser opu = OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id = '"+from_user+"'");
		String[] id=cartId.split(",");
		Map ret = new HashMap<>();
		boolean flag = false;
		for(int i=0;i<id.length;i++){
			flag=Db.deleteById("purchase_commodity_car", id[i]);
		}
		if(flag==true){
			List<PurchaseCommodityCar> commoditylist = PurchaseCommodityCar.dao.find("select * from purchase_commodity_car where user_id = '"+opu.getStr("id")+"' and order_type='01' and commodity_type = '01' order by create_date desc ");
			ret.put("size", commoditylist.size());
			ret.put("code", "0");
			ret.put("msg", "删除成功");
		}
		renderJson(ret);
		}else{
			String url=getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
			render("error.html");
		}
	}
	//提交结算数量跟总价修改
	public void changList(){
		String cartId = getPara("cartId");
		String[] id=cartId.split(",");
		String buy_num = getPara("buy_num");
		String[] num=buy_num.split(",");
		String total_price = getPara("total_price");
		String[] price=total_price.split(",");
		String from_user=getCookie("wx_openid");
		if(!StringUtil.isEmpty(from_user)){
		Map ret = new HashMap<>();
		int rel=0;
		for(int i=0;i<id.length;i++){
			rel=Db.update("update purchase_commodity_car set total_price='"+price[i]+"',buy_num='"+num[i]+"' where id='"+id[i]+"' ");
		}
		if(rel>0){
			ret.put("code", "0");
			ret.put("msg", "修改成功");
		}
		renderJson(ret);
		}else{
			String url=getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
			render("error.html");
		}
	}

}
