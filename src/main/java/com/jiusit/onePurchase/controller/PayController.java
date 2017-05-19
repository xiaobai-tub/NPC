package com.jiusit.onePurchase.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.jfinal.ApiController;
import com.jfinal.weixin.sdk.kit.IpKit;
import com.jiusit.common.utils.DateUtil;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.interceptor.UserInterceptor;
import com.jiusit.onePurchase.model.OnePurchaseUser;
import com.jiusit.onePurchase.model.PurchaseCommodity;
import com.jiusit.onePurchase.model.PurchaseCommodityBuy;
import com.jiusit.onePurchase.model.PurchaseCommodityBuyTotal;
import com.jiusit.onePurchase.model.PurchaseCommodityCar;
import com.jiusit.onePurchase.model.PurchaseCommodityPay;
import com.jiusit.onePurchase.model.PurchaseCommodityVersion;
import com.jiusit.onePurchase.model.PurchaseMarketing;
import com.jiusit.onePurchase.model.PurchaseOnepurchaseBuy;
import com.jiusit.onePurchase.model.PurchaseOnepurchaseBuyTotal;
import com.jiusit.onePurchase.model.PurchaseRandomNum;
import com.jiusit.onePurchase.model.PurchaseSentAddress;
import com.jiusit.onePurchase.model.PurchaseTicket;
import com.jiusit.onePurchase.model.PurchaseTicketRandom;
import com.jiusit.onePurchase.model.PurchaseTimeSlot;
import com.jiusit.onePurchase.model.PurchaseUserTicket;
import com.jiusit.onePurchase.model.SysAreas;
import com.jiusit.onePurchase.model.TradeMarketCode;
import com.jiusit.onePurchase.model.TradeMarketShare;
import com.jiusit.onePurchase.model.TradeMarketUser;
import com.jiusit.onePurchase.model.WbVar;
import com.jiusit.onePurchase.pay.Pay;
import com.jiusit.onePurchase.service.StoredProcedureService;

@ControllerBind(controllerKey = "/pay", viewPath = "/pages")
@Before(UserInterceptor.class)
public class PayController extends ApiController {
	private static final Logger log = Logger.getLogger(PayController.class);
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	DateFormat df1 = new SimpleDateFormat("HHmmssSSS");

	@Override
	public ApiConfig getApiConfig() {
		ApiConfig ac = new ApiConfig();

		// 配置微信 API 相关常量
		ac.setToken(PropKit.get("token"));
		ac.setAppId(PropKit.get("appId"));
		ac.setAppSecret(PropKit.get("appSecret"));

		// 支付信息

		/**
		 * 是否对消息进行加密，对应于微信平台的消息加解密方式： 1：true进行加密且必须配置 encodingAesKey
		 * 2：false采用明文模式，同时也支持混合模式
		 */
		ac.setEncryptMessage(PropKit.getBoolean("encryptMessage", false));
		ac.setEncodingAesKey(PropKit.get("encodingAesKey",
				"setting it in config file"));
		return ac;
	}

	// 订单确认页面
	public void forwardConfirm() {
		String id = getPara("id"); // 商品id
		String from_user = getCookie("wx_openid");
		String version_id = getPara("version_id"); // 型号id
		String countNum = getPara("countNum"); // 购买数量
		String timeSlot = getPara("time_slot");
		String slotId = getPara("slotId");
		String orderType = getPara("orderType");
		String address_id = getPara("address_id"); // 送回地址id
		BigDecimal allprice = new BigDecimal(getPara("price"));// 单价
		allprice = allprice.multiply(new BigDecimal(countNum));// 总
		String type = getPara("type"); // 购物车，还是直接下单 02 直接下单 01 添加购物车
		PurchaseCommodity commodity = PurchaseCommodity.dao.findById(id);
		setAttr("commodity_type", getPara("commodity_type"));
		if(!StringUtil.isEmpty(from_user)){ 
		//查询满足条件的卡卷 and a.card_category <> '02'
	    if(!commodity.getStr("commodity_category").equals("04")){
	    	if((int)allprice.doubleValue() * 100>0){
				String ticketSql="select a.*,date_format(a.begin_date,'%Y.%m.%d') as 'df_begin_date',date_format(a.end_date,'%Y.%m.%d') as 'df_end_date' from purchase_user_ticket a where a.minimum_threshold_amount<="+allprice+" and a.card_category <> '02' and a.ticket_status='02' and a.wx_id='"+from_user+"' and TO_DAYS(NOW()) BETWEEN TO_DAYS(a.begin_date) AND TO_DAYS(a.end_date) order by a.get_date desc";
				if(commodity != null ){
					if(StringUtil.isEqual(commodity.getStr("commodity_category"), "02")){
						ticketSql="select a.*,date_format(a.begin_date,'%Y.%m.%d') as 'df_begin_date',date_format(a.end_date,'%Y.%m.%d') as 'df_end_date' from purchase_user_ticket a where a.minimum_threshold_amount<="+allprice+" and a.ticket_status='02' and a.wx_id='"+from_user+"' and TO_DAYS(NOW()) BETWEEN TO_DAYS(a.begin_date) AND TO_DAYS(a.end_date) order by a.get_date desc";	
					}
				}
				List<PurchaseUserTicket> ptrList = PurchaseUserTicket.dao.find(ticketSql);
				setAttr("ptrList", ptrList);
			}
	    }
		setAttr("commodity_category", commodity.getStr("commodity_category"));
		PurchaseCommodityVersion commodityVersion = PurchaseCommodityVersion.dao
				.findById(version_id);
		OnePurchaseUser user = OnePurchaseUser.dao
				.findFirst("select * from purchase_users where wx_id='"
						+ from_user + "'");
		if (StringUtil.isEqual(type, "02")) {
			List<PurchaseCommodity> commodityList = PurchaseCommodity.dao
					.find("select a.*,b.id as 'version_id',b.version_type,"
							+ countNum
							+ " as 'countNum' from purchase_commodity a left join purchase_commodity_version b on b.commodity_id=a.id where a.id='"
							+ id + "' and b.id='" + version_id + "'");
			String sql = "select a.*,b.area_name as 'province_name',c.area_name as 'city_name',d.area_name as 'country_name' from purchase_sent_address a left join sys_areas b on b.area_code=a.province_code left join sys_areas c on c.area_code=a.city_code  left join sys_areas d on d.area_code=a.county_code where a.user_id='"
					+ user.get("id") + "'  ";
			if (!StringUtil.isEmpty(address_id)) {
				sql += " and a.id='" + address_id + "'";
			} else {
				sql += " and is_default='1' ";
			}
			PurchaseSentAddress sendAddress = PurchaseSentAddress.dao
					.findFirst(sql);
			setAttr("server_uri", PropKit.get("server_uri"));
			setAttr("commodityList", commodityList);
			setAttr("total_price", allprice.toString());
			setAttr("type", type);
			setAttr("countNum", countNum);
			setAttr("version_id", version_id);
			setAttr("id", id);
			setAttr("price", getPara("price"));
			setAttr("sendAddress", sendAddress);
			setAttr("timeSlot", timeSlot);
			setAttr("slotId", slotId);
			setAttr("orderType", orderType);
			render("order_confirm.html");

		} else {
			Map map = new HashMap();
			String is_buy = "01"; // 可买
			String msg = "";
			if(commodity.getStr("commodity_category").equals("04")){
				if(user.getStr("vip_identify").equals("02")){
					PurchaseCommodityCar pcc = PurchaseCommodityCar.dao.findFirst("select a.* from purchase_commodity_car a left join purchase_commodity b on a.commodity_id=b.id where a.user_id='"+user.getStr("id")+"' and b.commodity_category='04' ");
					if(pcc==null){
						PurchaseCommodityBuy pcb = PurchaseCommodityBuy.dao.findFirst("select a.* from purchase_commodity_buy a left join purchase_commodity b on a.commodity_id=b.id where a.user_id='"+user.getStr("id")+"' and b.commodity_category='04' ");
						if(pcb!=null){
							is_buy = "02"; // 不可买
							msg = "已兑换过该活动类型商品";
						}
					}else{
						is_buy = "02"; // 不可买
						msg = "购物车已有该活动类型商品";
					}
				}else{
					is_buy = "02"; // 不可买
					msg = "请先成为会员";
				}
			}
			if (!StringUtil.isEmpty(slotId)) {
				PurchaseTimeSlot time_slot = PurchaseTimeSlot.dao
						.findById(slotId);
				if (time_slot.getInt("surplus_count") < Integer
						.parseInt(countNum)
						|| StringUtil.isEqual(time_slot.getStr("is_over"), "1")) {
					is_buy = "02"; // 不可买
				}
			}
			if (!StringUtil.isEmpty(version_id)) {
				PurchaseCommodityVersion commodity_version = PurchaseCommodityVersion.dao
						.findById(version_id);
				int version_stock = 0;
				if (!StringUtil.isEmpty(commodity_version.getInt(
						"version_stock").toString())) {
					version_stock = commodity_version.getInt("version_stock");
				}
				if (version_stock < Integer.parseInt(countNum)) {
					is_buy = "02"; // 不可买
					msg = commodity_version.getStr("version_type") + "库存不足";
				}
			}
			if (StringUtil.isEqual(is_buy, "01")) {
				String sql = "select * from purchase_commodity_car where user_id='"
						+ user.get("id") + "'";
				if (!StringUtil.isEmpty(version_id)) {
					sql += " and version_id='" + version_id + "'";
				}
				if (!StringUtil.isEmpty(slotId)) {
					sql += " and time_slot='" + slotId + "'";
				}
				boolean is_sucess = true;
				PurchaseCommodityCar countCommodityCar = PurchaseCommodityCar.dao
						.findFirst(sql);
				PurchaseCommodityCar commodityCar = new PurchaseCommodityCar();
				if (countCommodityCar == null) {
					String buyId = StringUtil.getUUID();
					commodityCar
							.set("id", buyId)
							.set("user_id", user.get("id"))
							.set("commodity_id", id)
							.set("time_slot", slotId)
							.set("commodity_logo",
									commodity.get("commodity_logo"))
							.set("commodity_name",
									commodity.get("commodity_name"))
							.set("version_id", version_id)
							.set("version_type",
									commodityVersion.get("version_type"))
							.set("buy_date", new Date())
							.set("per_price", getPara("price"))
							.set("total_price", allprice.toString())
							.set("buy_num", getPara("countNum"))
							.set("order_type", type)
							.set("commodity_type", getPara("commodity_type"))
							.set("order_status", type)
							.set("create_by", user.get("id"))
							.set("create_name", user.get("nick_name"))
							.set("create_date", new Date())
							.set("update_by", user.get("id"))
							.set("update_name", user.get("nick_name"))
							.set("update_date", new Date())
							.set("remarks", "添加购物车下单");
					is_sucess = commodityCar.save();
				} else {
					commodityCar
							.set("id", countCommodityCar.get("id"))
							.set("total_price",
									allprice.add(countCommodityCar
													.getBigDecimal("total_price")
													))
							.set("buy_num",
									new BigDecimal(getPara("countNum")).add(
											new BigDecimal(countCommodityCar
													.getInt("buy_num")
													.toString())).toString());
					is_sucess = commodityCar.update();
				}
				if (is_sucess) {
					map.put("code", "0");
					map.put("msg", "成功加入购物车");
				} else {
					map.put("code", "1");
					map.put("msg", "加入购物车失败");
				}
			} else {
				map.put("code", "1");
				map.put("msg", "您购买的数量超过剩余数量");
				if (StringUtil.isEqual(getPara("commodity_type"), "01")) {
					map.put("msg", msg);
				}
			}
			renderJson(map);
		}
		}else{
			String url=getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x")+1, url.length())+"?id="+id);
			render("error.html");
		}
	}

	// 购物车订单确认页面
	public void carForwardConfirm() {
		String from_user = getCookie("wx_openid");
		String buy_id = getPara("buy_id"); // 订单id
		String address_id = getPara("address_id"); // 订单id
		String diff = getPara("diff"); // 区别
		if (!StringUtil.isEqual(diff, "01")) {
			buy_id = buy_id.substring(0, buy_id.lastIndexOf(","));
			buy_id = StringUtil.replace(buy_id, ",", "','");
		}
		String commodity_type = getPara("commodity_type"); // 商品类型
		if(!StringUtil.isEmpty(from_user)){ 
		OnePurchaseUser user = OnePurchaseUser.dao
				.findFirst("select * from purchase_users where wx_id='"
						+ from_user + "'");
		List<PurchaseCommodityCar> commodityList = PurchaseCommodityCar.dao
				.find("SELECT a.* FROM `purchase_commodity_car` a   where a.id in ('"
						+ buy_id + "')");
		for(PurchaseCommodityCar p : commodityList){
			PurchaseCommodity pc = PurchaseCommodity.dao.findById(p.getStr("commodity_id"));
			if(pc.getStr("commodity_category").equals("04")){
				setAttr("commodity_category", pc.getStr("commodity_category"));
			}
		}
		PurchaseCommodityCar commodityBuy = PurchaseCommodityCar.dao
				.findFirst("select sum(total_price) as 'total_price' from purchase_commodity_car where id in ('"
						+ buy_id + "')");
		String sql = "select a.*,b.area_name as 'province_name',c.area_name as 'city_name',d.area_name as 'country_name' from purchase_sent_address a left join sys_areas b on b.area_code=a.province_code left join sys_areas c on c.area_code=a.city_code  left join sys_areas d on d.area_code=a.county_code where a.user_id='"
				+ user.get("id") + "' ";
		if (!StringUtil.isEmpty(address_id)) {
			sql += "  and a.id='" + address_id + "'";
		} else {
			sql += " and is_default='1' ";
		}
		//查询满足条件的卡卷
		if(commodityBuy!=null){
			BigDecimal allprice = new BigDecimal(commodityBuy.get("total_price").toString());// 单价
			if((int)allprice.doubleValue() * 100 >0){
				String ticketSql="select a.*,date_format(a.begin_date,'%Y.%m.%d') as 'df_begin_date',date_format(a.end_date,'%Y.%m.%d') as 'df_end_date' from purchase_user_ticket a where a.minimum_threshold_amount<="+commodityBuy.get("total_price")+" and a.card_category <> '02' and a.ticket_status='02' and a.wx_id='"+from_user+"' and TO_DAYS(NOW()) BETWEEN TO_DAYS(a.begin_date) AND TO_DAYS(a.end_date) order by a.get_date desc";
				List<PurchaseUserTicket> ptrList = PurchaseUserTicket.dao.find(ticketSql);
				setAttr("ptrList", ptrList);
			}
		}
		PurchaseSentAddress sendAddress = PurchaseSentAddress.dao
				.findFirst(sql);
		setAttr("server_uri", PropKit.get("server_uri"));
		setAttr("total_price", commodityBuy.get("total_price"));
		setAttr("commodityList", commodityList);
		setAttr("sendAddress", sendAddress);
		setAttr("buy_ids", buy_id);
		setAttr("commodity_type", commodity_type);
		render("car_order_confirm.html");
	}else{
		String url=getRequest().getRequestURI();
		setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
		render("error.html");
	}

	}

	// 待支付订单确认页面
	public void orderForwardConfirm() {
		String from_user = getCookie("wx_openid");
		String total_id = getPara("total_id"); // 订单id
		String commodity_type = getPara("commodity_type"); // 商品类型
		String address_id = getPara("address_id"); // 送货地址
		if(!StringUtil.isEmpty(from_user)){ 
		OnePurchaseUser user = OnePurchaseUser.dao
				.findFirst("select * from purchase_users where wx_id='"
						+ from_user + "'");
		List<Record> commodityList = null;
		Record commodityBuyTotal = null;
		Record commodityBuy = null;
		if (StringUtil.isEqual(commodity_type, "01")) {
			commodityList = Db
					.find("SELECT a.* FROM `purchase_commodity_buy` a   where a.total_id = '"
							+ total_id + "' ");
			commodityBuyTotal = Db
					.findFirst("select * from purchase_commodity_buy_total where id ='"
							+ total_id + "' ");
			commodityBuy = Db
					.findFirst("SELECT CONCAT(a.id) as 'buy_ids' FROM `purchase_commodity_buy` a   where a.total_id = '"
							+ total_id + "' ");
		} else {
			commodityList = Db
					.find("SELECT a.* FROM `purchase_onepurchase_buy` a where a.total_id = '"
							+ total_id + "' ");
			commodityBuyTotal = Db
					.findFirst("select * from purchase_onepurchase_buy_total where id ='"
							+ total_id + "' ");
			commodityBuy = Db
					.findFirst("SELECT CONCAT(a.id) as 'buy_ids' FROM `purchase_onepurchase_buy` a where a.total_id = '"
							+ total_id + "' ");
		}

		String sql = "select a.*,b.area_name as 'province_name',c.area_name as 'city_name',d.area_name as 'country_name' from purchase_sent_address a left join sys_areas b on b.area_code=a.province_code left join sys_areas c on c.area_code=a.city_code  left join sys_areas d on d.area_code=a.county_code where a.user_id='"
				+ user.get("id") + "' ";
		if (!StringUtil.isEmpty(address_id)) {
			sql += "  and a.id='" + address_id + "'";
		} else {
			sql += " and is_default='1' ";
		}
		//是否是异兽卡活动
		for(Record r : commodityList){
			PurchaseCommodity pc = PurchaseCommodity.dao.findById(r.getStr("commodity_id"));
			if(pc.getStr("commodity_category").equals("04")){
				setAttr("commodity_category", pc.getStr("commodity_category"));
			}
		}
		//查询卡卷
		PurchaseUserTicket ptr = PurchaseUserTicket.dao.findFirst("select b.*,date_format(b.begin_date,'%Y.%m.%d') as 'df_begin_date',date_format(b.end_date,'%Y.%m.%d') as 'df_end_date' from purchase_commodity_buy_total a left join purchase_user_ticket b on a.ticket_id=b.id where a.id='"+total_id+"' and b.minimum_threshold_amount<="+commodityBuyTotal.get("total_price")+" and b.wx_id='"+from_user+"' and TO_DAYS(NOW()) BETWEEN TO_DAYS(b.begin_date) AND TO_DAYS(b.end_date) ");
		setAttr("ptr", ptr);
		PurchaseSentAddress sendAddress = PurchaseSentAddress.dao
				.findFirst(sql);
		setAttr("server_uri", PropKit.get("server_uri"));
		setAttr("commodityBuyTotal", commodityBuyTotal);
		setAttr("commodityList", commodityList);
		setAttr("sendAddress", sendAddress);
		setAttr("buy_ids", commodityBuy.get("buy_ids"));
		setAttr("commodity_type", commodity_type);
		setAttr("from_user", from_user);
		render("paid_order_confirm.html");
		}else{
			String url=getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
			render("error.html");
		}
	}

	// 生成微信订单
	public void createWXPayOrder() throws Exception {
		ApiConfigKit.setThreadLocalApiConfig(this.getApiConfig());
		String type = getPara("type"); // 购物车订单，还是非购物车订单
		String commodity_type = getPara("commodity_type"); // 普通商品，还是一元购
		String total_id = getPara("total_id"); // 总表id
		String onePrice = getPara("onePrice");
		String slotId = getPara("slotId");
		String order_no = getPara("order_no");
		String pay_order_no = getPara("pay_order_no");
		String send_cost = getPara("send_cost");
		String commodity_id = getPara("commodity_id");
		String version_id = getPara("version_id");
		String countNum = getPara("countNum");
		String address_id = getPara("address_id");
		String mortgage_amount = getPara("mortgage_amount");//卡卷抵押金额
		String ticket_id = getPara("ticket_id");//卡卷id
		//if (StringUtil.isEmpty(order_no)) {

			if (StringUtil.isEqual(commodity_type, "01")) {
				order_no = DateUtil.formatDate(new Date(), "yyyyMMddHHmmssSSS");
				order_no = "pt" + order_no;
			} else {
				order_no = DateUtil.formatDate(new Date(), "yyyyMMddHHmmssSSS");
				order_no = "op" + order_no;
			}
		//}
		Map<String, String> dataMap = new HashMap<String, String>();
		String buy_ids = getPara("buy_ids"); // 订单ids
		BigDecimal allprice = new BigDecimal("0.00");
		allprice = allprice.add(new BigDecimal(getPara("totalPrice")));

		String open_id = getCookie("wx_openid");

		String is_buy = "01"; // 可买
		String msg = ""; // 返回信息
		if(!StringUtil.isEmpty(open_id)){ 
		if (StringUtil.isEqual(commodity_type, "02")) {
			if (!StringUtil.isEmpty(buy_ids) && StringUtil.isEmpty(total_id)) {
				String buyIds = StringUtil.replace(buy_ids, "','", ",");
				String[] ids = buyIds.split(",");
				BigDecimal realTallprice = new BigDecimal("0.00"); // 总表总价
				BigDecimal realTallNum = new BigDecimal("0"); // 总购买数
				int count = 0; // 统计时间段剩余购买数为0的数量
				String totalId = ""; // 总表id
				for (int i = 0; i < ids.length; i++) {
					BigDecimal realprice = new BigDecimal("0.00"); // 明细总价
					PurchaseCommodityCar commodityCar = PurchaseCommodityCar.dao
							.findById(ids[i]);
					PurchaseTimeSlot timeSlot = PurchaseTimeSlot.dao
							.findById(commodityCar.get("time_slot"));
					PurchaseCommodityCar updateCommodityCar = new PurchaseCommodityCar();
					if (timeSlot.getInt("surplus_count") == 0) {
						is_buy = "02";
						msg = "剩余购买数0";
						System.out.println("剩余购买数量为0");
						count++;
						updateCommodityCar.set("id", ids[i]).set(
								"order_status", "07");
					} else if (timeSlot.getInt("surplus_count") < commodityCar
							.getInt("buy_num")) {
						System.out.println("剩余购买数量小于购买数量");
						is_buy = "02";
						msg = "剩余购买数不足";
					} else {
						realTallprice = realTallprice
								.add(commodityCar.getBigDecimal(
										"total_price"));
						System.out.println(realTallprice);
					}
				}
				allprice = realTallprice;
			}

		} else {
			if (!StringUtil.isEmpty(buy_ids) && StringUtil.isEmpty(total_id)) {
				String buyIds = StringUtil.replace(buy_ids, "','", ",");
				String[] ids = buyIds.split(",");
				BigDecimal realTallprice = new BigDecimal("0.00"); // 总表总价
				BigDecimal realTallNum = new BigDecimal("0"); // 总购买数
				int count = 0; // 统计时间段剩余购买数为0的数量
				String totalId = ""; // 总表id
				for (int i = 0; i < ids.length; i++) {
					BigDecimal realprice = new BigDecimal("0.00"); // 明细总价
					PurchaseCommodityCar commodityCar = PurchaseCommodityCar.dao
							.findById(ids[i]);
					PurchaseCommodityVersion commodityVersion = PurchaseCommodityVersion.dao
							.findById(commodityCar.get("version_id"));
					PurchaseCommodityCar updateCommodityCar = new PurchaseCommodityCar();
					int version_stock = 0;
					if (!StringUtil.isEmpty(commodityVersion.getInt(
							"version_stock").toString())) {
						version_stock = commodityVersion
								.getInt("version_stock");
					}
					if (version_stock == 0) {
						is_buy = "02";
						msg += commodityVersion.getStr("version_type") + " ";
						System.out.println("库存数量为0");
						count++;
						updateCommodityCar.set("id", ids[i]).set(
								"order_status", "07");
					} else if (version_stock < commodityCar.getInt("buy_num")) {
						is_buy = "02";
						msg += commodityVersion.getStr("version_type") + " ";
						System.out.println("剩余购买数量小于购买数量");
					} else {
						realTallprice = realTallprice
								.add(commodityCar.getBigDecimal(
										"total_price"));
						System.out.println(realTallprice);
					}
				}
				allprice = realTallprice.add(new BigDecimal(send_cost));
				msg += "库存不足";
			} else {
				if (!StringUtil.isEmpty(total_id)) {
					List<Record> commodityList = Db
							.find("SELECT a.buy_num,v.version_stock,v.version_type FROM `purchase_commodity_buy` a left join purchase_commodity_version v on a.version_id=v.id where a.total_id = '"
									+ total_id + "' ");
					for (Record r : commodityList) {
						if (r.getInt("buy_num") > r.getInt("version_stock")) {
							msg += r.getStr("version_type") + " ";
							is_buy = "02";
						}
					}
					msg += "库存不足";
				}
			}
		}

		OnePurchaseUser user = OnePurchaseUser.dao
				.findFirst("select * from purchase_users where wx_id = '"
						+ open_id + "'");
		WbVar wv = WbVar.dao.findFirst("select * from wb_var where  VAR_NAME='membership_discount' ");
		String membership_discount = wv.getStr("VAR_VALUE");
		String is_membership = "02";
		BigDecimal actually_paid_money = allprice;
		String commodity_category = "";
		int vip_num = 1;
		if(!StringUtil.isEmpty(commodity_id)){
			PurchaseCommodity pc = PurchaseCommodity.dao.findById(commodity_id);
			commodity_category = pc.getStr("commodity_category");
			vip_num = Integer.parseInt(countNum);
		}
		if(!StringUtil.isEmpty(total_id)){
			PurchaseCommodityBuy pcb = PurchaseCommodityBuy.dao.findFirst("select * from purchase_commodity_buy where total_id='"+total_id+"'");
			PurchaseCommodity pc = PurchaseCommodity.dao.findById(pcb.getStr("commodity_id"));
			commodity_category = pc.getStr("commodity_category");
			vip_num = Integer.parseInt(countNum);
		}
		if(commodity_category.equals("04")){
			if(user.getStr("vip_identify").equals("02")){
				if(vip_num>1){
					is_buy = "02"; // 不可买
					msg = "该活动类型商品只能兑换一件";
				}else{
					PurchaseCommodityBuy pcb = PurchaseCommodityBuy.dao.findFirst("select a.* from purchase_commodity_buy a left join purchase_commodity b on a.commodity_id=b.id where a.user_id='"+user.getStr("id")+"' and b.commodity_category='04' and pay_status='02' ");
					if(pcb!=null){
						is_buy = "02"; // 不可买
						msg = "已兑换过该活动类型商品";
					}else{
						if (user.getBigDecimal("amount_money").compareTo(actually_paid_money) >=0) {
							dataMap.put("type", type);
							dataMap.put("commodity_id", commodity_id);
							dataMap.put("countNum", countNum);
							dataMap.put("address_id", address_id);
							dataMap.put("version_id", version_id);
							dataMap.put("send_cost", send_cost);
							dataMap.put("buy_ids", buy_ids);
							dataMap.put("total_price",actually_paid_money.toString());
							dataMap.put("commodity_type", commodity_type);
							dataMap.put("total_id", total_id);
							dataMap.put("pay_order_no", pay_order_no);
							dataMap.put("commodity_total_price", actually_paid_money
									.subtract(new BigDecimal(send_cost)).toString());
							if (StringUtil.isEqual(is_buy, "01")) {
								if (!StringUtil.isEmpty(slotId)) {
									PurchaseTimeSlot timeSlot = PurchaseTimeSlot.dao
											.findById(slotId);
									if (timeSlot.getInt("surplus_count") >= Integer
											.parseInt(countNum)) {
										if (StringUtil.isEmpty(pay_order_no)) {
											is_buy = "01";
										}
									} else {
										is_buy = "02"; // 不可买
										msg += "剩余购买次数不够";
									}
								} else {
									if (!StringUtil.isEmpty(version_id)) {
										PurchaseCommodityVersion checkVersion = PurchaseCommodityVersion.dao
												.findById(version_id);
										int version_stock = 0;
										if (!StringUtil.isEmpty(checkVersion.getInt(
												"version_stock").toString())) {
											version_stock = checkVersion
													.getInt("version_stock");
										}
										if (version_stock >= Integer.parseInt(countNum)) {
											is_buy = "01";
										} else {
											is_buy = "02";
											msg += checkVersion.getStr("version_type") + "库存不足";
										}
									} else {
										if (StringUtil.isEmpty(pay_order_no)) {
											is_buy = "01";
										} else {
											if (StringUtil.isEmpty(buy_ids)) {
												is_buy = "01";
											}
										}
									}
	
								}
							}
							this.payJson(order_no, open_id, null, allprice + "", dataMap,
									slotId, onePrice, is_buy, msg, "account", null, null);
							return;
						}else{
							actually_paid_money = allprice.subtract(new BigDecimal(user
									.get("amount_money").toString()));
						}
					}
				}
			}else{
				is_buy = "02";
				msg += "请至招行网点办理异兽卡,成为会员";
			}
		}else if(!StringUtil.isEmpty(mortgage_amount) && !StringUtil.isEmpty(ticket_id)){
			//判断卡卷面值,卡劵id是否为空，不为空不进入抵押金额	
			if (new BigDecimal(mortgage_amount).compareTo(actually_paid_money) >=0
					) {
				dataMap.put("type", type);
				dataMap.put("commodity_id", commodity_id);
				dataMap.put("countNum", countNum);
				dataMap.put("address_id", address_id);
				dataMap.put("version_id", version_id);
				dataMap.put("send_cost", send_cost);
				dataMap.put("buy_ids", buy_ids);
				dataMap.put("total_price",actually_paid_money.toString());
				dataMap.put("commodity_type", commodity_type);
				dataMap.put("total_id", total_id);
				dataMap.put("pay_order_no", pay_order_no);
				dataMap.put("commodity_total_price", actually_paid_money
						.subtract(new BigDecimal(send_cost)).toString());
				if (StringUtil.isEqual(is_buy, "01")) {
					if (allprice.doubleValue() > 0) {
						if (!StringUtil.isEmpty(slotId)) {
							PurchaseTimeSlot timeSlot = PurchaseTimeSlot.dao
									.findById(slotId);
							if (timeSlot.getInt("surplus_count") >= Integer
									.parseInt(countNum)) {
								if (StringUtil.isEmpty(pay_order_no)) {
									is_buy = "01";
								}
							} else {
								is_buy = "02"; // 不可买
								msg += "剩余购买次数不够";
							}
						} else {
							if (!StringUtil.isEmpty(version_id)) {
								PurchaseCommodityVersion checkVersion = PurchaseCommodityVersion.dao
										.findById(version_id);
								int version_stock = 0;
								if (!StringUtil.isEmpty(checkVersion.getInt(
										"version_stock").toString())) {
									version_stock = checkVersion
											.getInt("version_stock");
								}
								if (version_stock >= Integer.parseInt(countNum)) {
									is_buy = "01";
								} else {
									is_buy = "02";
									msg += checkVersion.getStr("version_type") + "库存不足";
								}
							} else {
								if (StringUtil.isEmpty(pay_order_no)) {
									is_buy = "01";
								} else {
									if (StringUtil.isEmpty(buy_ids)) {
										is_buy = "01";
									}
								}
							}

						}

					} else {
						is_buy = "02"; // 不可买
					}
				}
				
				this.payJson(order_no, open_id, null, allprice + "", dataMap,
						slotId, onePrice, is_buy, msg, "account", ticket_id, null);

				return;
			}else{
				actually_paid_money = allprice.subtract(new BigDecimal(mortgage_amount.toString()));
			}
		}else if(user.getStr("vip_identify").equals("02")){
				// 查询会员折扣
				if(membership_discount!=null){
					is_membership="01";
					//-1表示小于,0是等于,1是大于.
					BigDecimal discount_price = actually_paid_money.multiply(new BigDecimal(membership_discount.toString()).divide(new BigDecimal(10)));
					if ((int)(discount_price.doubleValue() * 100)<= 0 
							) {
						dataMap.put("type", type);
						dataMap.put("commodity_id", commodity_id);
						dataMap.put("countNum", countNum);
						dataMap.put("address_id", address_id);
						dataMap.put("version_id", version_id);
						dataMap.put("send_cost", send_cost);
						dataMap.put("buy_ids", buy_ids);
						dataMap.put("total_price",actually_paid_money.toString());
						dataMap.put("commodity_type", commodity_type);
						dataMap.put("total_id", total_id);
						dataMap.put("pay_order_no", pay_order_no);
						dataMap.put("commodity_total_price", actually_paid_money
								.subtract(new BigDecimal(send_cost)).toString());
						if (StringUtil.isEqual(is_buy, "01")) {
							if (allprice.doubleValue() > 0) {
								if (!StringUtil.isEmpty(slotId)) {
									PurchaseTimeSlot timeSlot = PurchaseTimeSlot.dao
											.findById(slotId);
									if (timeSlot.getInt("surplus_count") >= Integer
											.parseInt(countNum)) {
										if (StringUtil.isEmpty(pay_order_no)) {
											is_buy = "01";
										}
									} else {
										is_buy = "02"; // 不可买
										msg += "剩余购买次数不够";
									}
								} else {
									if (!StringUtil.isEmpty(version_id)) {
										PurchaseCommodityVersion checkVersion = PurchaseCommodityVersion.dao
												.findById(version_id);
										int version_stock = 0;
										if (!StringUtil.isEmpty(checkVersion.getInt(
												"version_stock").toString())) {
											version_stock = checkVersion
													.getInt("version_stock");
										}
										if (version_stock >= Integer.parseInt(countNum)) {
											is_buy = "01";
										} else {
											is_buy = "02";
											msg += checkVersion.getStr("version_type") + "库存不足";
										}
									} else {
										if (StringUtil.isEmpty(pay_order_no)) {
											is_buy = "01";
										} else {
											if (StringUtil.isEmpty(buy_ids)) {
												is_buy = "01";
											}
										}
									}

								}

							} else {
								is_buy = "02"; // 不可买
							}
						}
						
						this.payJson(order_no, open_id, null, allprice + "", dataMap,
								slotId, onePrice, is_buy, msg, "account", null, membership_discount);

						return;
					} else {
						actually_paid_money = discount_price;
					}
			}
		}else{
			// 查询用户余额
			if (null != user.get("amount_money")) {
	                //-1表示小于,0是等于,1是大于.
				if (user.getBigDecimal("amount_money").compareTo(actually_paid_money) >=0
						) {
					dataMap.put("type", type);
					dataMap.put("commodity_id", commodity_id);
					dataMap.put("countNum", countNum);
					dataMap.put("address_id", address_id);
					dataMap.put("version_id", version_id);
					dataMap.put("send_cost", send_cost);
					dataMap.put("buy_ids", buy_ids);
					dataMap.put("total_price",actually_paid_money.toString());
					dataMap.put("commodity_type", commodity_type);
					dataMap.put("total_id", total_id);
					dataMap.put("pay_order_no", pay_order_no);
					dataMap.put("commodity_total_price", actually_paid_money
							.subtract(new BigDecimal(send_cost)).toString());
					if (StringUtil.isEqual(is_buy, "01")) {
						if (allprice.doubleValue() > 0) {
							if (!StringUtil.isEmpty(slotId)) {
								PurchaseTimeSlot timeSlot = PurchaseTimeSlot.dao
										.findById(slotId);
								if (timeSlot.getInt("surplus_count") >= Integer
										.parseInt(countNum)) {
									if (StringUtil.isEmpty(pay_order_no)) {
										is_buy = "01";
									}
								} else {
									is_buy = "02"; // 不可买
									msg += "剩余购买次数不够";
								}
							} else {
								if (!StringUtil.isEmpty(version_id)) {
									PurchaseCommodityVersion checkVersion = PurchaseCommodityVersion.dao
											.findById(version_id);
									int version_stock = 0;
									if (!StringUtil.isEmpty(checkVersion.getInt(
											"version_stock").toString())) {
										version_stock = checkVersion
												.getInt("version_stock");
									}
									if (version_stock >= Integer.parseInt(countNum)) {
										is_buy = "01";
									} else {
										is_buy = "02";
										msg += checkVersion.getStr("version_type") + "库存不足";
									}
								} else {
									if (StringUtil.isEmpty(pay_order_no)) {
										is_buy = "01";
									} else {
										if (StringUtil.isEmpty(buy_ids)) {
											is_buy = "01";
										}
									}
								}

							}

						} else {
							is_buy = "02"; // 不可买
						}
					}
					
					this.payJson(order_no, open_id, null, allprice + "", dataMap,
							slotId, onePrice, is_buy, msg, "account", null , null);

					return;
				} else {
					actually_paid_money = allprice.subtract(new BigDecimal(user
							.get("amount_money").toString()));
				}
			}
		}
		

		BigDecimal commodity_total_price = allprice.subtract(new BigDecimal(
				send_cost));
		double fprice = allprice.doubleValue();
		/*System.out.println(actually_paid_money.multiply(new BigDecimal(100)));
		System.out.println(actually_paid_money.multiply(new BigDecimal(100)).intValue());
		int total_fee = (int) (actually_paid_money.doubleValue() * 100);*/
		int total_fee = actually_paid_money.multiply(new BigDecimal(100)).intValue();
		if (StringUtil.isEmpty(open_id)) {
			open_id = getPara("from_user");
		}

		// int total_fee = 1; //维修订单支付为1分钱
		System.out.println("total_fee---->" + total_fee);
		dataMap.put("type", type);
		dataMap.put("commodity_id", commodity_id);
		dataMap.put("countNum", countNum);
		dataMap.put("address_id", address_id);
		dataMap.put("version_id", version_id);
		dataMap.put("send_cost", send_cost);
		dataMap.put("buy_ids", buy_ids);
		dataMap.put("commodity_type", commodity_type);
		dataMap.put("total_price", Double.toString(fprice));
		dataMap.put("total_id", total_id);
		dataMap.put("pay_order_no", pay_order_no);
		dataMap.put("commodity_total_price", commodity_total_price.toString());
		// 参数
		Map<String, String> params = new HashMap<String, String>();
		System.out.println("orderid------>" + order_no);
		String nonceStr = StringUtil.getRoundNum(8); // 8位随机数
		params.put("appid", PropKit.get("appId")); // 公众号id
		params.put("mch_id", PropKit.get("partnerId")); // 商户id
		params.put("openid", open_id); // 用户的openid。
		// params.put("openid", "oOppcuHy3biBj9UyRN-N-v-Cr2Sw"); //用户的openid。
		params.put("device_info", "WEB"); // 公众号内部请求 或web端请求 填WEB
		params.put("nonce_str", nonceStr); // 随机数 少于32位即可
		params.put("body", "商品订单支付"); // 商品描述。
		params.put("attach", "一元购"); // 附加信息 可不填
		params.put("out_trade_no", order_no); // 商户系统内部的订单号
		params.put("total_fee", total_fee + ""); // 订单总金额 为整形 单位（分）
		params.put("spbill_create_ip", IpKit.getRealIp(getRequest())); // ip
		params.put("trade_type", "JSAPI"); // 支付方式 JSAPI
		params.put("notify_url", PropKit.get("notify_url")); // 统一下单成功后回调url
		String packagestring = Pay.setSign(params, PropKit.get("paySignKey")); // 构造签名
		System.out.println("sign-------->" + packagestring);

		String orderUrl = "appid=" + PropKit.get("appId") + "&mch_id="
				+ PropKit.get("partnerId") + "&openid=" + open_id;
		orderUrl += "&device_info=WEB&nonce_str=" + nonceStr + "&sign="
				+ packagestring + "&body=" + "商品订单支付"
				+ "&attach=一元购&out_trade_no=" + order_no;
		orderUrl += "&total_fee=" + total_fee + "&spbill_create_ip="
				+ IpKit.getRealIp(getRequest())
				+ "&trade_type=JSAPI&notify_url=" + PropKit.get("notify_url");
		System.out.println(orderUrl);
		// 将键值对转换成xml字符串
		orderUrl = Pay.keyvalue2XML(orderUrl);
		System.out.println(orderUrl);
		// 统一下单 注：一定要用post请求
		String result = "";
		PurchaseCommodityVersion pcv = PurchaseCommodityVersion.dao
				.findFirst("select * from purchase_commodity_version where commodity_id='"
						+ commodity_id + "' and id='" + version_id + "' ");
		System.out.println(allprice.doubleValue() > 0);
		if (StringUtil.isEqual(is_buy, "01")) {
			if (allprice.doubleValue() > 0) {
				if (!StringUtil.isEmpty(slotId)) {
					PurchaseTimeSlot timeSlot = PurchaseTimeSlot.dao
							.findById(slotId);
					if (timeSlot.getInt("surplus_count") >= Integer
							.parseInt(countNum)) {
						if (StringUtil.isEmpty(pay_order_no)) {
							if(total_fee > 0){
							result = HttpKit
									.post("https://api.mch.weixin.qq.com/pay/unifiedorder",
											orderUrl);
							}
						}
					} else {
						is_buy = "02"; // 不可买
						msg += "剩余购买次数不够";
					}
				} else {
					if (!StringUtil.isEmpty(version_id)) {
						PurchaseCommodityVersion checkVersion = PurchaseCommodityVersion.dao
								.findById(version_id);
						int version_stock = 0;
						if (!StringUtil.isEmpty(checkVersion.getInt(
								"version_stock").toString())) {
							version_stock = checkVersion
									.getInt("version_stock");
						}
						if (version_stock >= Integer.parseInt(countNum)) {
							if(total_fee > 0){
							result = HttpKit
									.post("https://api.mch.weixin.qq.com/pay/unifiedorder",
											orderUrl);
							}
						} else {
							is_buy = "02";
							msg += checkVersion.getStr("version_type") + "库存不足";
						}
					} else {
						if (StringUtil.isEmpty(pay_order_no) || !StringUtil.isEmpty(pay_order_no)) {
							if(total_fee > 0){
							result = HttpKit
									.post("https://api.mch.weixin.qq.com/pay/unifiedorder",
											orderUrl);
							is_buy = "01";
							}
						} else {
							if (StringUtil.isEmpty(buy_ids)) {
								if(total_fee > 0){
								result = HttpKit
										.post("https://api.mch.weixin.qq.com/pay/unifiedorder",
												orderUrl);
								is_buy = "01";
								}
							}
						}
					}

				}

			} else {
				is_buy = "02"; // 不可买
			}
		}
		System.out.println("order result ----->" + result);
		if(is_membership.equals("02")){
			this.payJson(order_no, open_id, result, actually_paid_money + "", dataMap, slotId,
					onePrice, is_buy, msg, null, ticket_id, null);
		}else{
			this.payJson(order_no, open_id, result, actually_paid_money + "", dataMap, slotId,
					onePrice, is_buy, msg, null, ticket_id, membership_discount);
		}
		
	}else{
		render("error.html");
	}
	}

	// 将下单后返回信息的 生成支付所需的签名以及各参数
	public void payJson(String order_id, String from_user, String result,
			String pay_money, Map<String, String> dataMap, String slotId,
			String onePrice, String is_buy, String msg, String account, String ticket_id,String membership_discount)
			throws Exception {
		ApiConfigKit.setThreadLocalApiConfig(this.getApiConfig());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		DateFormat df1 = new SimpleDateFormat("HHmmssSSS");
		BigDecimal agent_total_revenue = new BigDecimal("0.00"); //代理商总分成
		String prepay_id = dataMap.get("pay_order_no");
		Map res = new HashMap();
		if(!StringUtil.isEmpty(from_user)){ 
		if (StringUtil.isEqual(is_buy, "01")) {
			//if (StringUtil.isEmpty(dataMap.get("total_id")) && null == account) {
				if (StringUtil.isEmpty(result)) {
					res.put("code", -1);
					res.put("msg", "无效的参数！");
				} else {
					result = URLDecoder.decode(result, "utf-8");
					Map<String, String> map = Pay.xml2Map(result);
					String return_code = map.get("return_code");
					if ("FAIL".equals(return_code)) {
						res.put("code", -1);
						res.put("msg", "支付认证失败！");
					} else {
						prepay_id = map.get("prepay_id");
					}
				}
			//}
			System.out.println("prepay_id---------------" + prepay_id);
			// package 参数------------------------------ //
			Map<String, String> params = new HashMap<String, String>();
			// 参数
			if (null == account) {
				String timeStamp = System.currentTimeMillis() + ""; // 时间戳
				System.out.println("timeStamp---------------" + timeStamp);
				String nonceStr = StringUtil.getRoundNum(8); // 8位随机数
				params.put("appId", PropKit.get("appId")); // appId
				params.put("timeStamp", timeStamp);
				params.put("nonceStr", nonceStr); // 随机数 小于32位即可
				params.put("package", "prepay_id=" + prepay_id); // 重要参数
				params.put("signType", "MD5"); // 加密方式
				String paySign = Pay.setSign(params, PropKit.get("paySignKey")); // 构造签名
				params.put("paySign", paySign); // 加密方式
				params.put("packageStr", "prepay_id=" + prepay_id); // 加密方式
			}
			OnePurchaseUser user = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id='"
							+ from_user + "'");
			PurchaseCommodity commodity = PurchaseCommodity.dao
					.findFirst("select * from purchase_commodity where id='"
							+ dataMap.get("commodity_id") + "'");
			PurchaseCommodityVersion commodityVersion = PurchaseCommodityVersion.dao
					.findById(dataMap.get("version_id"));
			String areas = "";
			String receiver_name = "";
			String receiver_telephone = "";

			if (!StringUtil.isEmpty(dataMap.get("address_id"))) {
				PurchaseSentAddress sentAddress = PurchaseSentAddress.dao
						.findById(dataMap.get("address_id"));
				SysAreas province = SysAreas.dao.findById(sentAddress
						.get("province_code"));
				SysAreas city = SysAreas.dao.findById(sentAddress
						.get("city_code"));
				SysAreas county = SysAreas.dao.findById(sentAddress
						.get("county_code"));
				receiver_name = sentAddress.get("receiver_name");
				receiver_telephone = sentAddress.get("telephone");
				if(province!= null){
					areas += province.get("area_name").toString();
				}
				if(city != null){
					areas += city.get("area_name").toString();
				}
				if(county != null){
					areas += county.get("area_name").toString();
				}
				areas += sentAddress.get("geography_location");
			}
			PurchaseCommodityBuy commodityBuy = new PurchaseCommodityBuy();
			PurchaseOnepurchaseBuy oneCommodityBuy = new PurchaseOnepurchaseBuy();
			String buy_id = dataMap.get("buy_ids");
			String total_id = StringUtil.getUUID();
			if (!StringUtil.isEmpty(dataMap.get("total_id"))) {
				total_id = dataMap.get("total_id");
			}
			if (StringUtil.isEmpty(dataMap.get("total_id"))) {
				if (StringUtil.isEmpty(buy_id)) {
					buy_id = StringUtil.getUUID();
					if (StringUtil.isEqual(dataMap.get("commodity_type"), "02")) {
						int num = Integer.parseInt(dataMap.get("countNum"));
						String newId = "";
						BigDecimal per_price = new BigDecimal(onePrice)
								.divide(new BigDecimal(num));
						for (int i = 0; i < num; i++) {
							String id = StringUtil.getUUID();
							newId += id + ",";
							oneCommodityBuy
									.set("id", id)
									.set("order_no", order_id)
									.set("total_id", total_id)
									.set("pay_order_no", prepay_id)
									.set("user_id", user.get("id"))
									.set("commodity_id",
											dataMap.get("commodity_id"))
									.set("commodity_name",
											commodity.get("commodity_name"))
									.set("commodity_logo",
											commodity.get("commodity_logo"))
									.set("version_id",
											dataMap.get("version_id"))
									.set("version_type",
											commodityVersion
													.get("version_type"))
									.set("buy_date", new Date())
									.set("time_slot", slotId)
									.set("per_price",
											commodity.get("commodity_price"))
									.set("total_price", per_price.toString())
									.set("buy_num", 1)
									.set("order_type", dataMap.get("type"))
									// .set("commodity_type",
									// commodity.get("commodity_type"))
									.set("order_status", "01")
									.set("pay_status", "01")
									.set("is_win", "01")
									// .set("send_address_id",
									// dataMap.get("address_id"))
									.set("create_by", user.get("id"))
									.set("create_name", user.get("nick_name"))
									.set("create_date", new Date())
									.set("update_by", user.get("id"))
									.set("update_name", user.get("nick_name"))
									.set("update_date", new Date())
									.set("remarks", "直接下单");
							oneCommodityBuy.save();
						}
						PurchaseOnepurchaseBuyTotal oneCommodityBuyTotal = PurchaseOnepurchaseBuyTotal.dao
								.set("id", total_id)
								.set("order_no", order_id)
								.set("pay_order_no", prepay_id)
								.set("user_id", user.get("id"))
								.set("total_price", dataMap.get("total_price"))
								.set("commodity_total_price",
										dataMap.get("commodity_total_price"))
								.set("buy_num", dataMap.get("countNum"))
								.set("order_type", dataMap.get("type"))
								.set("order_status", "01")
								.set("pay_status", "01")
								.set("send_cost", dataMap.get("send_cost"))
								.set("dispatch_mode", "01")
								.set("send_address", areas)
								.set("is_add_addr", "01")
								.set("send_address_id",
										dataMap.get("address_id"))
								.set("receiver_name", receiver_name)
								.set("receiver_telephone", receiver_telephone)
								.set("create_by", user.get("id"))
								.set("create_name", user.get("nick_name"))
								.set("create_date", new Date())
								.set("update_by", user.get("id"))
								.set("update_name", user.get("nick_name"))
								.set("update_date", new Date())
								.set("remarks", "直接下单");
						oneCommodityBuyTotal.save();
						buy_id = newId.substring(0, newId.lastIndexOf(","));
						buy_id = StringUtil.replace(buy_id, ",", "','");

					} else {
						BigDecimal agent__revenue = new BigDecimal("0.00"); //代理商分成
						BigDecimal per_revenue = new BigDecimal("0.00"); //商品分成
                        String trade_name=""; //代理商名称
                        String trade_id=""; //代理商id
                        String revenue_type=""; //分成类型
						if(!StringUtil.isEmpty(user.getStr("agent_code"))){
						    	TradeMarketUser marketUser=TradeMarketUser.dao.findFirst("select * from trade_market_user where id='"+user.getStr("agent_code")+"'");
					    	    if(marketUser != null ){
						    		trade_name=marketUser.getStr("trade_name");
					    	    	trade_id=marketUser.getStr("id");
					    	    	revenue_type=marketUser.getStr("trade_type");
						    		if(StringUtil.isEqual(marketUser.getStr("trade_type"), "01")){
					    	    		if(commodity.getBigDecimal("personal_revenue") != null ){
					    	    		agent__revenue=commodity.getBigDecimal("personal_revenue");
					    	    		per_revenue=commodity.getBigDecimal("personal_revenue");
					    	    		}
					    	    	}
					    	    	if(StringUtil.isEqual(marketUser.getStr("trade_type"), "02")){
					    	    		if(commodity.getBigDecimal("company_revenue") != null ){
					    	    		agent__revenue=commodity.getBigDecimal("company_revenue");
					    	    		per_revenue=commodity.getBigDecimal("company_revenue");
					    	    		}
					    	    	}
				    	    		agent__revenue=agent__revenue.multiply(new BigDecimal(dataMap.get("countNum")));
				    	    		agent_total_revenue=agent_total_revenue.add(agent__revenue);
						    	  }
						    }
						commodityBuy
								.set("id", buy_id)
								.set("order_no", order_id)
								.set("total_id", total_id)
								.set("pay_order_no", prepay_id)
								.set("user_id", user.get("id"))
								.set("commodity_id",
										dataMap.get("commodity_id"))
								.set("commodity_name",
										commodity.get("commodity_name"))
								.set("commodity_logo",
										commodity.get("commodity_logo"))
								.set("version_id", dataMap.get("version_id"))
								.set("version_type",
										commodityVersion.get("version_type"))
								.set("buy_date", new Date())
								.set("per_price",
										commodity.get("commodity_price"))
								.set("total_price",
										dataMap.get("commodity_total_price"))
								.set("buy_num", dataMap.get("countNum"))
								.set("order_type", dataMap.get("type"))
								.set("agent_id", trade_id)
								.set("agent_name", trade_name)
								.set("per_revenue", per_revenue.toString())
								.set("agent_revenue", agent__revenue.toString())
								.set("revenue_type", revenue_type)
								.set("commodity_type",
										commodity.get("commodity_type"))
								.set("order_status", "01")
								.set("pay_status", "01")
								.set("send_address_id",
										dataMap.get("address_id"))
								.set("export_status", "01")
								.set("create_by", user.get("id"))
								.set("create_name", user.get("nick_name"))
								.set("create_date", new Date())
								.set("update_by", user.get("id"))
								.set("update_name", user.get("nick_name"))
								.set("update_date", new Date())
								.set("remarks", "直接下单");
						commodityBuy.save();
						PurchaseCommodityBuyTotal commodityBuyTotal = PurchaseCommodityBuyTotal.dao
								.set("id", total_id)
								.set("order_no", order_id)
								.set("pay_order_no", prepay_id)
								.set("user_id", user.get("id"))
								.set("agent_id", trade_id)
								.set("agent_name", trade_name)
								.set("agent_total_revenue", agent_total_revenue.toString())
								.set("membership_discount", membership_discount)
								.set("ticket_id", ticket_id)//卡卷id
								.set("total_price", dataMap.get("total_price"))
								.set("commodity_total_price",
										dataMap.get("commodity_total_price"))
								.set("buy_num", dataMap.get("countNum"))
								.set("order_type", dataMap.get("type"))
								.set("order_status", "01")
								.set("pay_status", "01")
								.set("send_cost", dataMap.get("send_cost"))
								.set("commodity_total_price",
										dataMap.get("commodity_total_price"))
								.set("dispatch_mode", "01")
								.set("send_address_id",
										dataMap.get("address_id"))
								.set("send_address", areas)
								.set("receiver_name", receiver_name)
								.set("receiver_telephone", receiver_telephone)
								.set("export_status", "01")
								.set("create_by", user.get("id"))
								.set("create_name", user.get("nick_name"))
								.set("create_date", new Date())
								.set("update_by", user.get("id"))
								.set("update_name", user.get("nick_name"))
								.set("update_date", new Date())
								.set("remarks", "直接下单");
						commodityBuyTotal.save();
						if(!StringUtil.isEmpty(ticket_id)){
							Db.update("update purchase_user_ticket set ticket_status='03' where id='"+ticket_id+"' ");
						}
					}

				} else {

					if (StringUtil.isEqual(dataMap.get("type"), "01")) {
						if (StringUtil.isEqual(dataMap.get("commodity_type"),
								"01")) {
							PurchaseCommodityCar commodityCar = PurchaseCommodityCar.dao
									.findFirst("select sum(buy_num) as 'count' from purchase_commodity_car where id in ('"
											+ buy_id + "')");
							String buyIds = StringUtil.replace(buy_id, "','",
									",");
							String[] ids = buyIds.split(",");
							String newIds = "";
							String trade_name=""; //代理商名称
	                        String trade_id=""; //代理商id
							for (int i = 0; i < ids.length; i++) {
								PurchaseCommodityCar catData = PurchaseCommodityCar.dao
										.findById(ids[i]);
								commodity=PurchaseCommodity.dao.findById(catData.get("commodity_id"));
								PurchaseCommodityBuy insertBuyData = new PurchaseCommodityBuy();
								String new_buy_id = StringUtil.getUUID();
								newIds += new_buy_id + ",";
								BigDecimal agent__revenue = new BigDecimal("0.00"); //代理商分成
								BigDecimal per_revenue = new BigDecimal("0.00"); //商品分成
								String revenue_type=""; //分成类型
								if(!StringUtil.isEmpty(user.getStr("agent_code"))){
								    	TradeMarketUser marketUser=TradeMarketUser.dao.findFirst("select * from trade_market_user where id='"+user.getStr("agent_code")+"'");
							    	    if(marketUser != null ){
								    		trade_name=marketUser.getStr("trade_name");
							    	    	trade_id=marketUser.getStr("id");
							    	    	revenue_type=marketUser.getStr("trade_type");
								    		if(StringUtil.isEqual(marketUser.getStr("trade_type"), "01")){
							    	    		if(commodity.getBigDecimal("personal_revenue") != null ){
							    	    		agent__revenue=commodity.getBigDecimal("personal_revenue");
							    	    		per_revenue=commodity.getBigDecimal("personal_revenue");
							    	    		}
							    	    	}
							    	    	if(StringUtil.isEqual(marketUser.getStr("trade_type"), "02")){
							    	    		if(commodity.getBigDecimal("company_revenue") != null ){
							    	    		agent__revenue=commodity.getBigDecimal("company_revenue");
							    	    		per_revenue=commodity.getBigDecimal("company_revenue");
							    	    		}
							    	    	}
						    	    		agent__revenue=agent__revenue.multiply(new BigDecimal(catData.getInt("buy_num")));
						    	    		agent_total_revenue=agent_total_revenue.add(agent__revenue);
								    	  }
							    	    }
								insertBuyData
										.set("id", new_buy_id)
										.set("order_no", order_id)
										.set("total_id", total_id)
										.set("pay_order_no", prepay_id)
										.set("user_id", user.get("id"))
										.set("commodity_id",
												catData.get("commodity_id"))
										.set("commodity_name",
												catData.get("commodity_name"))
										.set("commodity_logo",
												catData.get("commodity_logo"))
										.set("version_id",
												catData.get("version_id"))
										.set("version_type",
												catData.get("version_type"))
										.set("buy_date", new Date())
										.set("per_price",
												catData.get("per_price"))
										.set("total_price",
												catData.get("total_price"))
										.set("buy_num", catData.get("buy_num"))
										.set("agent_id", trade_id)
								        .set("agent_name", trade_name)
								        .set("per_revenue", per_revenue.toString())
										.set("agent_revenue", agent__revenue.toString())
								        .set("revenue_type", revenue_type)
										.set("order_type", "02")
										.set("commodity_type",
												catData.get("commodity_type"))
										.set("order_status", "01")
										.set("pay_status", "01")
										.set("send_address_id",
												dataMap.get("address_id"))
										.set("export_status", "01")
										.set("create_by", user.get("id"))
										.set("create_name",
												user.get("nick_name"))
										.set("create_date", new Date())
										.set("update_by", user.get("id"))
										.set("update_name",
												user.get("nick_name"))
										.set("update_date", new Date())
										.set("remarks", "直接下单");
								insertBuyData.save();
								PurchaseCommodityCar.dao.deleteById(ids[i]);
							}
							PurchaseCommodityBuyTotal commodityBuyTotal = PurchaseCommodityBuyTotal.dao
									.set("id", total_id)
									.set("order_no", order_id)
									.set("pay_order_no", prepay_id)
									.set("user_id", user.get("id"))
									.set("agent_id", trade_id)
								    .set("agent_name", trade_name)
								    .set("agent_total_revenue", agent_total_revenue.toString())
								    .set("membership_discount", membership_discount)
									.set("total_price", dataMap.get("total_price"))
									.set("ticket_id", ticket_id)//卡卷id
									.set("commodity_total_price",
											dataMap.get("commodity_total_price"))
									.set("buy_num", commodityCar.get("count"))
									.set("order_type", "02")
									.set("order_status", "01")
									.set("pay_status", "01")
									.set("send_cost", dataMap.get("send_cost"))
									.set("dispatch_mode", "01")
									.set("send_address", areas)
									.set("send_address_id",
											dataMap.get("address_id"))
									.set("receiver_name", receiver_name)
									.set("receiver_telephone",
											receiver_telephone)
									.set("export_status", "01")
									.set("create_by", user.get("id"))
									.set("create_name", user.get("nick_name"))
									.set("create_date", new Date())
									.set("update_by", user.get("id"))
									.set("update_name", user.get("nick_name"))
									.set("update_date", new Date())
									.set("remarks", "购物车下单");
							commodityBuyTotal.save();
							if(!StringUtil.isEmpty(ticket_id)){
								Db.update("update purchase_user_ticket set ticket_status='03' where id='"+ticket_id+"' ");
							}
							buy_id = newIds.substring(0,
									newIds.lastIndexOf(","));
							buy_id = StringUtil.replace(buy_id, ",", "','");
						} else {
							// 一元购购物车下单
							PurchaseCommodityCar commodityCar = PurchaseCommodityCar.dao
									.findFirst("select sum(buy_num) as 'count' from purchase_commodity_car where id in ('"
											+ buy_id
											+ "') and order_status <> '07' ");
							PurchaseOnepurchaseBuyTotal commodityBuyTotal = PurchaseOnepurchaseBuyTotal.dao
									.set("id", total_id)
									.set("order_no", order_id)
									.set("pay_order_no", prepay_id)
									.set("user_id", user.get("id"))
									.set("total_price", dataMap.get("total_price"))
									.set("commodity_total_price",
											dataMap.get("commodity_total_price"))
									.set("buy_num", commodityCar.get("count"))
									.set("order_type", "02")
									.set("order_status", "01")
									.set("pay_status", "01")
									.set("send_cost", dataMap.get("send_cost"))
									.set("dispatch_mode", "01")
									.set("send_address", areas)
									.set("commodity_total_price",
											dataMap.get("commodity_total_price"))
									.set("is_add_addr", "01")
									.set("send_address_id",
											dataMap.get("address_id"))
									.set("receiver_name", receiver_name)
									.set("receiver_telephone",
											receiver_telephone)
									.set("create_by", user.get("id"))
									.set("create_name", user.get("nick_name"))
									.set("create_date", new Date())
									.set("update_by", user.get("id"))
									.set("update_name", user.get("nick_name"))
									.set("update_date", new Date())
									.set("remarks", "购物车下单");
							commodityBuyTotal.save();

							String buyIds = StringUtil.replace(buy_id, "','",
									",");
							String[] ids = buyIds.split(",");
							String newIds = "";
							for (int i = 0; i < ids.length; i++) {
								PurchaseCommodityCar catData = PurchaseCommodityCar.dao
										.findById(ids[i]);
								PurchaseOnepurchaseBuy insertBuyData = new PurchaseOnepurchaseBuy();
								String new_buy_id = StringUtil.getUUID();
								newIds += new_buy_id + ",";
								if (catData != null) {
									insertBuyData
											.set("id", new_buy_id)
											.set("order_no", order_id)
											.set("total_id", total_id)
											.set("pay_order_no", prepay_id)
											.set("user_id", user.get("id"))
											.set("commodity_id",
													catData.get("commodity_id"))
											.set("commodity_name",
													catData.get("commodity_name"))
											.set("commodity_logo",
													catData.get("commodity_logo"))
											.set("version_id",
													catData.get("version_id"))
											.set("version_type",
													catData.get("version_type"))
											.set("buy_date", new Date())
											.set("per_price",
													catData.get("per_price"))
											.set("total_price",
													catData.get("total_price"))
											.set("buy_num",
													catData.get("buy_num"))
											.set("order_type", "02")
											// .set("commodity_type",
											// catData.get("commodity_type"))
											.set("order_status",
													catData.get("order_status"))
											.set("pay_status", "01")
											.set("is_win", "01")
											.set("time_slot",
													catData.get("time_slot"))
											.set("create_by", user.get("id"))
											.set("create_name",
													user.get("nick_name"))
											.set("create_date", new Date())
											.set("update_by", user.get("id"))
											.set("update_name",
													user.get("nick_name"))
											.set("update_date", new Date())
											.set("remarks", "购物车下单");
									insertBuyData.save();
									PurchaseCommodityCar.dao.deleteById(ids[i]);
								}

							}
							buy_id = newIds.substring(0,
									newIds.lastIndexOf(","));
							buy_id = StringUtil.replace(buy_id, ",", "','");
						}

					}
				}
			} else {
				if (StringUtil.isEqual(dataMap.get("commodity_type"), "01")) {
					PurchaseCommodityBuyTotal buyTotal = new PurchaseCommodityBuyTotal()
							.set("id", total_id).set("send_address_id",
									dataMap.get("address_id"))
									.set("order_no", order_id)
									.set("pay_order_no", prepay_id);
					buyTotal.update();
				} else {
					PurchaseOnepurchaseBuyTotal buyTotal = new PurchaseOnepurchaseBuyTotal()
							.set("id", total_id).set("send_address_id",
									dataMap.get("address_id"));
					buyTotal.update();
				}
			}

			if (null == account) {
				res.put("order_id", order_id);
				res.put("from_user", from_user);
				res.put("code", 0);
				res.put("msg", msg);
				res.put("params", params);
				res.put("pay_money", dataMap.get("total_price"));
				res.put("commodity_id", dataMap.get("commodity_id"));
				res.put("buy_id", buy_id);
				res.put("total_id", total_id);
				res.put("commodity_type", dataMap.get("commodity_type"));
			} else {
				
				res.put("code", "3");
				res.put("commodity_type", this.payOrder(order_id, from_user, pay_money, ticket_id, membership_discount));
			}

		} else {
			res.put("code", 1);
			res.put("msg", msg);
			if (StringUtil.isEqual(getPara("commodity_type"), "01")) {
				res.put("msg", msg);
			}
		}

		log.debug(res.toString());
		renderJson(res);
		}else{
			render("error.html");
		}
	}

	public String payOrder(String order_id, String fromUser, String pay_money, String ticket_id, String membership_discount) {
		Map<String, String> res = new HashMap<String, String>();
		try {

			String from_user = fromUser;
			String out_trade_no = order_id;
			String pay_order_no = DateUtil.formatDate(new Date(),
					"yyyyMMddHHmmssSSS")
					+ DateUtil.formatDate(new Date(), "ddSSSssmmHHMMyyyy");
			String result_code = "SUCCESS";
			String return_code = "SUCCESS";
			BigDecimal cash_fee = new BigDecimal(pay_money);
			String commodity_id = getPara("commodity_id");
			String id = getPara("id");
			String commodity_type = "01";
			if (!StringUtil.isEmpty(out_trade_no)) {
				String type = out_trade_no.substring(0, 2);
				if (StringUtil.isEqual(type, "op")) {
					commodity_type = "02";
				}
			}
			String mortgage_type="03";
			String pay_type="04";
			if(!StringUtil.isEmpty(ticket_id)){
				mortgage_type="04";
				pay_type="05";
			}
			if(!StringUtil.isEmpty(membership_discount)){
				mortgage_type="05";
				pay_type="06";
			}
			if(!StringUtil.isEmpty(from_user)){ 
			OnePurchaseUser user = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id='"
							+ from_user + "'");
			String str = "";
			if (StringUtil.isEqual(result_code, "SUCCESS")
					&& StringUtil.isEqual(return_code, "SUCCESS")) {
				if (StringUtil.isEqual(commodity_type, "01")) {
					PurchaseCommodityBuyTotal buyTotal = PurchaseCommodityBuyTotal.dao
							.findFirst("select * from purchase_commodity_buy_total where order_no='"
									+ out_trade_no + "' and order_status='01' ");
					if (buyTotal != null) {
						PurchaseCommodityBuyTotal commodityBuyTotal = new PurchaseCommodityBuyTotal();
						// 抵押金
						BigDecimal mortgage_amount = cash_fee;
						commodityBuyTotal.set("id", buyTotal.get("id"))
								.set("pay_type", pay_type).set("pay_status", "02")
								.set("pay_order_no", pay_order_no)
								.set("pay_user_id", user.get("id"))
								.set("order_status", "02")
								.set("mortgage_amount", mortgage_amount)
								.set("mortgage_type", mortgage_type)
								.set("actually_paid_money", 0)
								.set("pay_date", new Date())
								.set("update_by", user.get("id"))
								.set("update_name", user.get("nick_name"))
								.set("update_date", new Date());
						commodityBuyTotal.update();
                        if(StringUtil.isEqual(mortgage_type, "03")){
						// 减用户余额
						Db.update("update purchase_users set amount_money = amount_money-"
								+ mortgage_amount
								+ " where wx_id = '"
								+ from_user + "'");
                        }
						List<PurchaseCommodityBuy> commodityBuyList = PurchaseCommodityBuy.dao
								.find("select * from purchase_commodity_buy where total_id='"
										+ buyTotal.get("id") + "'");
						for (PurchaseCommodityBuy buy : commodityBuyList) {
							PurchaseCommodityBuy commodityBuy = new PurchaseCommodityBuy();
							commodityBuy.set("id", buy.get("id"))
									.set("pay_type", pay_type)
									.set("pay_order_no", pay_order_no)
									.set("pay_status", "02")
									.set("pay_user_id", user.get("id"))
									.set("order_status", "02")
									.set("pay_date", new Date())
									.set("update_by", user.get("id"))
									.set("update_name", user.get("nick_name"))
									.set("update_date", new Date());
							commodityBuy.update();
							Db.update("update purchase_commodity set sales_volume=sales_volume+"
									+ buy.getInt("buy_num")
									+ " where id ='"
									+ buy.get("commodity_id") + "'");
							Db.update("update purchase_commodity_version set version_stock=version_stock-"
									+ buy.getInt("buy_num")
									+ " where id ='"
									+ buy.get("version_id") + "'");
							PurchaseCommodityVersion commodityVersion = PurchaseCommodityVersion.dao
									.findFirst("select sum(version_stock) as 'total_sum' from purchase_commodity_version where  commodity_id='"
											+ buy.get("commodity_id") + "'");
							if (StringUtil
									.isEqual(
											commodityVersion.getBigDecimal(
													"total_sum").toString(),
											"0")) {
								PurchaseCommodity commodity = new PurchaseCommodity()
										.set("id", buy.get("commodity_id"))
										.set("commodity_status", "02");
								commodity.update();
							}
						}
						PurchaseCommodityPay commodityPay = PurchaseCommodityPay.dao
								.findFirst("select * from purchase_commodity_pay where order_id='"
										+ buyTotal.get("id") + "'");
						if (commodityPay == null) {
							PurchaseCommodityPay commodity_pay = new PurchaseCommodityPay()
									.set("id", StringUtil.getUUID())
									.set("order_id", buyTotal.get("id"))
									.set("pay_order_no", pay_order_no)
									.set("paid_amount",
											buyTotal.get("total_price"))
									.set("source_type", "01")
									.set("user_id", user.get("id"))
									.set("status", "02").set("pay_type", pay_type)
									.set("commodity_type", commodity_type)
									.set("create_by", user.get("id"))
									.set("create_name", user.get("nick_name"))
									.set("create_date", new Date())
									.set("update_by", user.get("id"))
									.set("update_name", user.get("nick_name"))
									.set("update_date", new Date())
									.set("remarks", "入账");
							commodity_pay.save();
						}
						
					}
				} else {
					PurchaseOnepurchaseBuyTotal buyTotal = PurchaseOnepurchaseBuyTotal.dao
							.findFirst("select * from purchase_onepurchase_buy_total where order_no='"
									+ out_trade_no + "' and order_status='01' ");
					if (buyTotal != null) {
						PurchaseOnepurchaseBuyTotal commodityBuyTotal = new PurchaseOnepurchaseBuyTotal();
						// 抵押金
						BigDecimal mortgage_amount = cash_fee;
						
						commodityBuyTotal.set("id", buyTotal.get("id"))
								.set("pay_type", pay_type).set("pay_status", "02")
								.set("pay_order_no", pay_order_no)
								.set("pay_user_id", user.get("id"))
								.set("actually_paid_money", 0)
								.set("order_status", "09")
								.set("mortgage_amount", mortgage_amount)
								.set("mortgage_type", mortgage_type)
								.set("pay_date", new Date())
								.set("update_by", user.get("id"))
								.set("update_name", user.get("nick_name"))
								.set("update_date", new Date());
						commodityBuyTotal.update();
						// 减用户余额
						if(StringUtil.isEqual(mortgage_type, "03")){
						Db.update("update purchase_users set amount_money = amount_money-"
								+ mortgage_amount
								+ " where wx_id = '"
								+ from_user + "'");
						}
						List<PurchaseOnepurchaseBuy> commodityBuyList = PurchaseOnepurchaseBuy.dao
								.find("select * from purchase_onepurchase_buy where total_id='"
										+ buyTotal.get("id") + "'");
						for (PurchaseOnepurchaseBuy buy : commodityBuyList) {
							int lucky_num = 10000000;
							String random_num = "";
							PurchaseOnepurchaseBuy commodityBuyNum = PurchaseOnepurchaseBuy.dao
									.findById(buy.get("id"));
							PurchaseRandomNum randomNum = PurchaseRandomNum.dao
									.findFirst("select * from purchase_random_num where time_solt_id='"
											+ commodityBuyNum.get("time_slot")
											+ "' ORDER BY RAND() LIMIT 1");
							if (randomNum != null) {
								random_num = randomNum.get("random_num");
							}
							lucky_num = lucky_num
									+ Integer.parseInt(random_num);
							Date date = new Date();
							PurchaseOnepurchaseBuy commodityBuy = new PurchaseOnepurchaseBuy();
							commodityBuy.set("id", buy.get("id"))
									.set("pay_type", pay_type)
									.set("pay_order_no", pay_order_no)
									.set("pay_status", "02")
									.set("pay_user_id", user.get("id"))
									.set("order_status", "09")
									.set("pay_date", new Date())
									.set("update_by", user.get("id"))
									.set("update_name", user.get("nick_name"))
									.set("update_date", new Date())
									.set("timeNum", df1.format(date))
									.set("indiana_time", df.format(date))
									.set("lucky_num", lucky_num);
							commodityBuy.update();
							Date lotter_time = DateUtil.dateAdd(12, 10,
									new Date());
							Db.update("update purchase_time_slot set surplus_count=surplus_count-"
									+ buy.get("buy_num")
									+ " where id ='"
									+ buy.get("time_slot") + "'");
							PurchaseTimeSlot timeSlot = PurchaseTimeSlot.dao
									.findById(buy.get("time_slot"));
							if (timeSlot.getInt("surplus_count") == 0) {
								Db.update("update purchase_time_slot set is_over='1',status='02',lottery_time='"
										+ df.format(lotter_time)
										+ "' where id ='"
										+ commodityBuyNum.get("time_slot")
										+ "' and is_over='0'");
							}

							Db.update("delete from purchase_random_num  where id ='"
									+ randomNum.get("id") + "'");
						}
						PurchaseCommodityPay commodityPay = PurchaseCommodityPay.dao
								.findFirst("select * from purchase_commodity_pay where order_id='"
										+ buyTotal.get("id") + "'");
						if (commodityPay == null) {
							PurchaseCommodityPay commodity_pay = new PurchaseCommodityPay()
									.set("id", StringUtil.getUUID())
									.set("order_id", buyTotal.get("id"))
									.set("pay_order_no", pay_order_no)
									.set("paid_amount",
											buyTotal.get("total_price"))
									.set("source_type", "01")
									.set("user_id", user.get("id"))
									.set("status", "02").set("pay_type", pay_type)
									.set("commodity_type", commodity_type)
									.set("create_by", user.get("id"))
									.set("create_name", user.get("nick_name"))
									.set("create_date", new Date())
									.set("update_by", user.get("id"))
									.set("update_name", user.get("nick_name"))
									.set("update_date", new Date())
									.set("remarks", "入账");
							commodity_pay.save();
						}
					}
				}
			}
			return commodity_type;
			//renderJson(res);
			// renderNull();
			}else{
				render("error.html");
			}
		} catch (Exception e) {
			System.out.println(e);
			renderNull();
		}
		return null;
	
	}

	@Clear
	public void WXPayCallback() {
		HttpServletRequest request = getRequest();
		Map<String, String> map = new HashMap<String, String>();
		InputStream inputStream;
		try {
			inputStream = request.getInputStream();
			// 读取输入流
			SAXReader reader = new SAXReader();
			Document document = reader.read(inputStream);
			// 得到xml根元素
			Element root = document.getRootElement();
			// 得到根元素的所有子节点
			List<Element> elementList = root.elements();
			// 遍历所有子节点
			for (Element e : elementList)
				map.put(e.getName(), e.getText());
			// 释放资源
			inputStream.close();
			inputStream = null;
			String from_user = map.get("openid");
			String out_trade_no = map.get("out_trade_no");
			String pay_order_no = map.get("transaction_id");
			String result_code = map.get("result_code");
			String return_code = map.get("return_code");
			System.out.println(map.get("cash_fee"));
			BigDecimal cash_fee = new BigDecimal(map.get("cash_fee").toString())
					.divide(new BigDecimal("100"));
			String commodity_id = getPara("commodity_id");
			String id = getPara("id");
			if (StringUtil.isEmpty(from_user)) {
				from_user = getPara("from_user");
			}
			String commodity_type = "01";
			if (!StringUtil.isEmpty(out_trade_no)) {
				String type = out_trade_no.substring(0, 2);
				if (StringUtil.isEqual(type, "op")) {
					commodity_type = "02";
				}
			}
			OnePurchaseUser user = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id='"
							+ from_user + "'");
			String str = "";
			if (StringUtil.isEqual(result_code, "SUCCESS")
					&& StringUtil.isEqual(return_code, "SUCCESS")) {
				if (StringUtil.isEqual(commodity_type, "01")) {
					PurchaseCommodityBuyTotal buyTotal = PurchaseCommodityBuyTotal.dao
							.findFirst("select * from purchase_commodity_buy_total where order_no='"
									+ out_trade_no + "' and order_status='01' ");
					if (buyTotal != null) {
						PurchaseCommodityBuyTotal commodityBuyTotal = new PurchaseCommodityBuyTotal();
						// 抵押金
						BigDecimal mortgage_amount = new BigDecimal(buyTotal
								.get("total_price").toString())
								.subtract(cash_fee);
						String mortgage_type="03";
						String pay_type="04";
						if(!StringUtil.isEmpty(buyTotal.getStr("ticket_id"))){
							mortgage_type="04";
							pay_type="05";
						}
						if(!StringUtil.isEmpty(buyTotal.getStr("membership_discount"))){
							mortgage_type="05";
							pay_type="06";
						}
						commodityBuyTotal.set("id", buyTotal.get("id"))
								.set("pay_type", "01").set("pay_status", "02")
								.set("pay_order_no", pay_order_no)
								.set("pay_user_id", user.get("id"))
								.set("order_status", "02")
								.set("mortgage_amount", mortgage_amount)
								.set("actually_paid_money", cash_fee)
								.set("mortgage_type", mortgage_type)
								.set("pay_date", new Date())
								.set("update_by", user.get("id"))
								.set("update_name", user.get("nick_name"))
								.set("update_date", new Date());
						commodityBuyTotal.update();
                        if(StringUtil.isEqual(mortgage_type, "03")){
						// 减用户余额
						Db.update("update purchase_users set amount_money = amount_money-"
								+ mortgage_amount
								+ " where wx_id = '"
								+ from_user + "'");
                        }
						List<PurchaseCommodityBuy> commodityBuyList = PurchaseCommodityBuy.dao
								.find("select * from purchase_commodity_buy where total_id='"
										+ buyTotal.get("id") + "'");
						for (PurchaseCommodityBuy buy : commodityBuyList) {
							PurchaseCommodityBuy commodityBuy = new PurchaseCommodityBuy();
							commodityBuy.set("id", buy.get("id"))
									.set("pay_type", "01")
									.set("pay_order_no", pay_order_no)
									.set("pay_status", "02")
									.set("pay_user_id", user.get("id"))
									.set("order_status", "02")
									.set("pay_date", new Date())
									.set("update_by", user.get("id"))
									.set("update_name", user.get("nick_name"))
									.set("update_date", new Date());
							commodityBuy.update();
							Db.update("update purchase_commodity set sales_volume=sales_volume+"
									+ buy.getInt("buy_num")
									+ " where id ='"
									+ buy.get("commodity_id") + "'");
							Db.update("update purchase_commodity_version set version_stock=version_stock-"
									+ buy.getInt("buy_num")
									+ " where id ='"
									+ buy.get("version_id") + "'");
							PurchaseCommodityVersion commodityVersion = PurchaseCommodityVersion.dao
									.findFirst("select sum(version_stock) as 'total_sum' from purchase_commodity_version where  commodity_id='"
											+ buy.get("commodity_id") + "'");
							if (StringUtil
									.isEqual(
											commodityVersion.getBigDecimal(
													"total_sum").toString(),
											"0")) {
								PurchaseCommodity commodity = new PurchaseCommodity()
										.set("id", buy.get("commodity_id"))
										.set("commodity_status", "02");
								commodity.update();
							}
						}
						PurchaseCommodityPay commodityPay = PurchaseCommodityPay.dao
								.findFirst("select * from purchase_commodity_pay where order_id='"
										+ buyTotal.get("id") + "'");
						
						PurchaseCommodityBuyTotal buyTotalOrder = PurchaseCommodityBuyTotal.dao
								.findFirst("select * from purchase_commodity_buy_total where id='"+buyTotal.get("id") + "'");
						if (commodityPay == null) {
							PurchaseCommodityPay commodity_pay = new PurchaseCommodityPay()
									.set("id", StringUtil.getUUID())
									.set("order_id", buyTotal.get("id"))
									.set("pay_order_no", pay_order_no)
									.set("paid_amount",
											cash_fee)
									.set("source_type", "01")
									.set("user_id", user.get("id"))
									.set("status", "02").set("pay_type", "01")
									.set("commodity_type", commodity_type)
									.set("create_by", user.get("id"))
									.set("create_name", user.get("nick_name"))
									.set("create_date", new Date())
									.set("update_by", user.get("id"))
									.set("update_name", user.get("nick_name"))
									.set("update_date", new Date())
									.set("remarks", "入账");
							commodity_pay.save();
							
							if(null != buyTotalOrder.get("mortgage_amount")||buyTotalOrder.getDouble("mortgage_amount") > 0){
								PurchaseCommodityPay commodity_pay_mortgage = new PurchaseCommodityPay()
								.set("id", StringUtil.getUUID())
								.set("order_id", buyTotal.get("id"))
								.set("pay_order_no", pay_order_no)
								.set("paid_amount",
										buyTotalOrder.get("mortgage_amount"))
								.set("source_type", "01")
								.set("user_id", user.get("id"))
								.set("status", "02").set("pay_type", pay_type)
								.set("commodity_type", commodity_type)
								.set("create_by", user.get("id"))
								.set("create_name", user.get("nick_name"))
								.set("create_date", new Date())
								.set("update_by", user.get("id"))
								.set("update_name", user.get("nick_name"))
								.set("update_date", new Date())
								.set("remarks", "入账");
								commodity_pay_mortgage.save();
							}
						}
						//调用存储过程赠送商城卡劵
						PurchaseMarketing purchaseMarketing=PurchaseMarketing.dao.findFirst("select * from purchase_marketing where execute_type='02' and marketing_status='02'");
                        if(purchaseMarketing != null ){
						PurchaseTicket pt = PurchaseTicket.dao.findFirst("select * from purchase_ticket where 1=1 and ticket_type='01' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ORDER BY rand() limit 0,1 ");
						if(pt!=null){
							PurchaseTicketRandom ptr = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_status='01' and ticket_id='"+pt.getStr("id")+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ORDER BY rand() limit 0,1 ");
							if(ptr!=null){
								  int result=StoredProcedureService.trackresult(ptr.getStr("id"),from_user,user.getStr("id"),user.getStr("nick_name"),buyTotal.getBigDecimal("total_price"),"03",2,3,"01");
							}
						  }
                        }
					}
				} else {
					PurchaseOnepurchaseBuyTotal buyTotal = PurchaseOnepurchaseBuyTotal.dao
							.findFirst("select * from purchase_onepurchase_buy_total where order_no='"
									+ out_trade_no + "' and order_status='01' ");
					if (buyTotal != null) {
						PurchaseOnepurchaseBuyTotal commodityBuyTotal = new PurchaseOnepurchaseBuyTotal();
						// 抵押金
						BigDecimal mortgage_amount = new BigDecimal(buyTotal
								.get("total_price").toString())
								.subtract(cash_fee);
						commodityBuyTotal.set("id", buyTotal.get("id"))
								.set("pay_type", "01").set("pay_status", "02")
								.set("pay_order_no", pay_order_no)
								.set("pay_user_id", user.get("id"))
								.set("actually_paid_money", cash_fee)
								.set("order_status", "09")
								.set("mortgage_amount", mortgage_amount)
								.set("mortgage_type", "03")
								.set("pay_date", new Date())
								.set("update_by", user.get("id"))
								.set("update_name", user.get("nick_name"))
								.set("update_date", new Date());
						commodityBuyTotal.update();
						// 减用户余额
						Db.update("update purchase_users set amount_money = amount_money-"
								+ mortgage_amount
								+ " where wx_id = '"
								+ from_user + "'");

						List<PurchaseOnepurchaseBuy> commodityBuyList = PurchaseOnepurchaseBuy.dao
								.find("select * from purchase_onepurchase_buy where total_id='"
										+ buyTotal.get("id") + "'");
						for (PurchaseOnepurchaseBuy buy : commodityBuyList) {
							int lucky_num = 10000000;
							String random_num = "";
							PurchaseOnepurchaseBuy commodityBuyNum = PurchaseOnepurchaseBuy.dao
									.findById(buy.get("id"));
							PurchaseRandomNum randomNum = PurchaseRandomNum.dao
									.findFirst("select * from purchase_random_num where time_solt_id='"
											+ commodityBuyNum.get("time_slot")
											+ "' ORDER BY RAND() LIMIT 1");
							if (randomNum != null) {
								random_num = randomNum.get("random_num");
							}
							lucky_num = lucky_num
									+ Integer.parseInt(random_num);
							Date date = new Date();
							PurchaseOnepurchaseBuy commodityBuy = new PurchaseOnepurchaseBuy();
							commodityBuy.set("id", buy.get("id"))
									.set("pay_type", "01")
									.set("pay_order_no", pay_order_no)
									.set("pay_status", "02")
									.set("pay_user_id", user.get("id"))
									.set("order_status", "09")
									.set("pay_date", new Date())
									.set("update_by", user.get("id"))
									.set("update_name", user.get("nick_name"))
									.set("update_date", new Date())
									.set("timeNum", df1.format(date))
									.set("indiana_time", df.format(date))
									.set("lucky_num", lucky_num);
							commodityBuy.update();
							Date lotter_time = DateUtil.dateAdd(12, 10,
									new Date());
							Db.update("update purchase_time_slot set surplus_count=surplus_count-"
									+ buy.get("buy_num")
									+ " where id ='"
									+ buy.get("time_slot") + "'");
							PurchaseTimeSlot timeSlot = PurchaseTimeSlot.dao
									.findById(buy.get("time_slot"));
							if (timeSlot.getInt("surplus_count") == 0) {
								Db.update("update purchase_time_slot set is_over='1',status='02',lottery_time='"
										+ df.format(lotter_time)
										+ "' where id ='"
										+ commodityBuyNum.get("time_slot")
										+ "' and is_over='0'");
							}

							Db.update("delete from purchase_random_num  where id ='"
									+ randomNum.get("id") + "'");
						}
						PurchaseCommodityPay commodityPay = PurchaseCommodityPay.dao
								.findFirst("select * from purchase_commodity_pay where order_id='"
										+ buyTotal.get("id") + "'");
						
						PurchaseOnepurchaseBuyTotal buyTotalOrder = PurchaseOnepurchaseBuyTotal.dao
								.findFirst("select * from purchase_onepurchase_buy_total where id='"+ buyTotal.get("id") + "'");
						
						
						if (commodityPay == null) {
							PurchaseCommodityPay commodity_pay = new PurchaseCommodityPay()
									.set("id", StringUtil.getUUID())
									.set("order_id", buyTotal.get("id"))
									.set("pay_order_no", pay_order_no)
									.set("paid_amount",
											cash_fee)
									.set("source_type", "01")
									.set("user_id", user.get("id"))
									.set("status", "02").set("pay_type", "01")
									.set("commodity_type", commodity_type)
									.set("create_by", user.get("id"))
									.set("create_name", user.get("nick_name"))
									.set("create_date", new Date())
									.set("update_by", user.get("id"))
									.set("update_name", user.get("nick_name"))
									.set("update_date", new Date())
									.set("remarks", "入账");
							commodity_pay.save();
							
							if(null != buyTotalOrder.get("mortgage_amount")||buyTotalOrder.getDouble("mortgage_amount") > 0){
								PurchaseCommodityPay commodity_pay_mortgage = new PurchaseCommodityPay()
								.set("id", StringUtil.getUUID())
								.set("order_id", buyTotal.get("id"))
								.set("pay_order_no", pay_order_no)
								.set("paid_amount",
										buyTotalOrder.get("mortgage_amount"))
								.set("source_type", "01")
								.set("user_id", user.get("id"))
								.set("status", "02").set("pay_type", "04")
								.set("commodity_type", commodity_type)
								.set("create_by", user.get("id"))
								.set("create_name", user.get("nick_name"))
								.set("create_date", new Date())
								.set("update_by", user.get("id"))
								.set("update_name", user.get("nick_name"))
								.set("update_date", new Date())
								.set("remarks", "入账");
								commodity_pay_mortgage.save();
							}
						}
					}
				}
			}
			renderJson("SUCCESS");
		} catch (Exception e) {
			System.out.println(e);
			renderNull();
		}

	}

	// 支付完成后修改支付状态
	public void subWeiXinPay() throws Exception {
		String from_user = getCookie("wx_openid");
		String commodity_type = getPara("commodity_type");
		Map returnMap = new HashMap();
		returnMap.put("code", "0");
		returnMap.put("order_type", commodity_type);
		renderJson(returnMap);
	}

	// 一元购支付成功跳转页面
	public void paySucess() {
		render("pay_sucess.html");
	}
}
