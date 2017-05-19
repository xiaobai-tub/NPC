package com.jiusit.onePurchase.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.jfinal.ApiController;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.interceptor.UserInterceptor;
import com.jiusit.onePurchase.model.Areas;
import com.jiusit.onePurchase.model.PurchaseCommodity;
import com.jiusit.onePurchase.model.PurchaseCommodityBuy;
import com.jiusit.onePurchase.model.PurchaseCommodityBuyTotal;
import com.jiusit.onePurchase.model.PurchaseCommodityCar;
import com.jiusit.onePurchase.model.PurchaseCommodityCollect;
import com.jiusit.onePurchase.model.PurchaseCommodityContent;
import com.jiusit.onePurchase.model.PurchaseCommodityPay;
import com.jiusit.onePurchase.model.PurchaseCommodityRecord;
import com.jiusit.onePurchase.model.PurchaseCommodityVersion;
import com.jiusit.onePurchase.model.PurchaseOnepurchaseBuy;
import com.jiusit.onePurchase.model.PurchaseSentAddress;
import com.jiusit.onePurchase.model.OnePurchaseUser;
import com.jiusit.onePurchase.model.PurchaseTicketRandom;
import com.jiusit.onePurchase.model.PurchaseTimeSlot;
import com.jiusit.onePurchase.model.PurchaseUserTicket;
import com.jiusit.onePurchase.model.TradeMarketProfit;
import com.jiusit.onePurchase.model.TradeMarketShare;

@ControllerBind(controllerKey = "/mine", viewPath = "/pages")
/*
 * 一元购商品
 */
public class MineController extends ApiController {
	private static final Logger log = Logger.getLogger(MineController.class);

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

	public void MineInformation() {
		String from_user = getCookie("wx_openid");
		if (!StringUtil.isEmpty(from_user)) {
			// 用户基本信息
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			if (opUser != null) {
				setAttr("opUser", opUser);
			}
			// 商品收藏
			PurchaseCommodityCollect pcc = PurchaseCommodityCollect.dao
					.findFirst("select count(*) as 'countpcc' from purchase_commodity_collect where user_id = '"
							+ opUser.get("id") + "'");
			setAttr("countpcc", pcc.get("countpcc"));
			// 浏览记录
			PurchaseCommodityRecord pcr = PurchaseCommodityRecord.dao
					.findFirst("select count(*) as 'countpcr' from purchase_commodity_record where user_id = '"
							+ opUser.get("id") + "'");
			setAttr("countpcr", pcr.get("countpcr"));
			// 我的订单
			// 代付款
			PurchaseCommodityBuyTotal commodityDfk = PurchaseCommodityBuyTotal.dao
					.findFirst("select count(*) as 'countDfk' from purchase_commodity_buy_total where order_status='01' and order_type='02' and user_id = '"
							+ opUser.get("id") + "'");
			setAttr("countDfk", commodityDfk.get("countDfk"));
			// 待发货
			PurchaseCommodityBuyTotal commodityDfh = PurchaseCommodityBuyTotal.dao
					.findFirst("select count(*) as 'countDfh' from purchase_commodity_buy_total where order_status='02' and order_type='02' and user_id = '"
							+ opUser.get("id") + "'");
			setAttr("countDfh", commodityDfh.get("countDfh"));
			/*
			 * int j = 0; for(PurchaseCommodityBuy dfh:pcbTwo){
			 * if(dfh.get("order_status").equals("02")){ j=j+1; } }
			 */
			// 待收款
			PurchaseCommodityBuyTotal commodityDsk = PurchaseCommodityBuyTotal.dao
					.findFirst("select count(*) as 'countDsk' from purchase_commodity_buy_total where order_status='03' and order_type='02' and user_id = '"
							+ opUser.get("id") + "'");
			setAttr("countDsk", commodityDsk.get("countDsk"));
			// 待评价
			PurchaseCommodityBuyTotal commodityDpj = PurchaseCommodityBuyTotal.dao
					.findFirst("select count(*) as 'countDpj' from purchase_commodity_buy_total where order_status='08' and order_type='02' and user_id = '"
							+ opUser.get("id") + "'");
			setAttr("countDpj", commodityDpj.get("countDpj"));
			// 我的一元购
			// 夺宝记录
			List<PurchaseOnepurchaseBuy> indiana = PurchaseOnepurchaseBuy.dao
					.find("SELECT COUNT(*) as 'countIndiana' FROM purchase_onepurchase_buy a LEFT JOIN purchase_users b on b.id=a.user_id where a.user_id='"
							+ opUser.get("id")
							+ "' and a.pay_status='02' GROUP BY a.time_slot");
			setAttr("countIndiana", indiana.size());
			// 中奖记录
			List<PurchaseOnepurchaseBuy> reward = PurchaseOnepurchaseBuy.dao
					.find("SELECT COUNT(*) as 'countReward' FROM purchase_onepurchase_buy a LEFT JOIN purchase_users b on b.id=a.user_id where a.is_win='02' and a.user_id='"
							+ opUser.get("id")
							+ "' and a.pay_status='02' GROUP BY a.time_slot");
			setAttr("countReward", reward.size());
			// 进行中
			List<PurchaseTimeSlot> conduct = PurchaseTimeSlot.dao
					.find("SELECT COUNT(*) as 'countConduct' FROM purchase_onepurchase_buy a LEFT JOIN purchase_users b on b.id=a.user_id LEFT JOIN purchase_time_slot c on c.id=a.time_slot where c.status='01' and a.order_status='09' and a.user_id='"
							+ opUser.get("id")
							+ "' and a.pay_status='02' GROUP BY a.time_slot");
			setAttr("countConduct", conduct.size());
			// 待揭晓
			List<PurchaseTimeSlot> countNoAnnounced = PurchaseTimeSlot.dao
					.find("SELECT COUNT(*) as 'countNoAnnounced' FROM purchase_onepurchase_buy a LEFT JOIN purchase_users b on b.id=a.user_id LEFT JOIN purchase_time_slot c on c.id=a.time_slot where c.status='02' and a.user_id='"
							+ opUser.get("id")
							+ "' and a.pay_status='02' GROUP BY a.time_slot");
			setAttr("countNoAnnounced", countNoAnnounced.size());
			// 已揭晓
			List<PurchaseTimeSlot> announced = PurchaseTimeSlot.dao
					.find("SELECT COUNT(*) as 'countAnnounced' FROM purchase_onepurchase_buy a LEFT JOIN purchase_users b on b.id=a.user_id LEFT JOIN purchase_time_slot c on c.id=a.time_slot where c.status='03' and a.user_id='"
							+ opUser.get("id")
							+ "' and a.pay_status='02' GROUP BY a.time_slot");
			setAttr("countAnnounced", announced.size());
			// 地址管理
			List<PurchaseSentAddress> psa = PurchaseSentAddress.dao
					.find("select * from purchase_sent_address where user_id = '"
							+ opUser.get("id") + "' order by is_default desc");
			if (psa != null) {
				setAttr("psa", psa);
			}
			// 联系客服
			render("my.html");
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 用户发送地址显示数据
	public void getAddr() {
		String from_user = getCookie("wx_openid");
		if (!StringUtil.isEmpty(from_user)) {
			String is_order = getPara("is_order");
			if (StringUtil.isEmpty(is_order)) {
				is_order = "02";
			}
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			if (opUser != null) {
				setAttr("opUser", opUser);
			}
			// 地址
			Page<PurchaseSentAddress> psa = PurchaseSentAddress.dao
					.paginate(
							1,
							10,
							"select a.*,b.area_name as 'province_name',c.area_name as 'city_name',d.area_name as 'country_name' ",
							"from purchase_sent_address a left join sys_areas b on b.area_code=a.province_code left join sys_areas c on c.area_code=a.city_code left join sys_areas d on d.area_code=a.county_code   where user_id='"
									+ opUser.get("id")
									+ "' order by is_default desc");
			setAttr("addressList", psa.getList());
			setAttr("pageNumber", psa.getPageNumber());
			setAttr("totalPage", psa.getTotalPage());
			setAttr("price", getPara("price"));
			setAttr("type", getPara("type"));
			setAttr("version_id", getPara("version_id"));
			setAttr("countNum", getPara("countNum"));
			setAttr("buy_ids", getPara("buy_id"));
			setAttr("buyId", getPara("buyId"));
			setAttr("id", getPara("id"));
			setAttr("commodity_type", getPara("commodity_type"));
			setAttr("forward_type", getPara("forward_type"));
			setAttr("is_order", is_order);
			setAttr("slotId", getPara("slotId"));
			render("addr_list.html");
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 查询省市县
	public void pccAddr() {
		String from_user = getCookie("wx_openid");
		String address_id = getPara("address_id");
		String send_address_id = getPara("send_address_id");
		String addOrEdit = "01";
		// 判断是否为空 address_id=“”；
		if (StringUtil.isEqual(address_id, "undefined")) {
			address_id = "";
			addOrEdit = "02";
		}
		if (!StringUtil.isEmpty(from_user)) {
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			if (opUser != null) {
				// 根据address_id 查找送货地址的信息
				PurchaseSentAddress purchase = PurchaseSentAddress.dao
						.findFirst("select * from purchase_sent_address where id = '"
								+ address_id + "' order by is_default desc");
				setAttr("purchase", purchase);
				List<Areas> pro = Areas.dao
						.find("select area_name,area_code from sys_areas where area_type=1  order by area_code desc");
				setAttr("pro", pro);
				List<Areas> city = Areas.dao
						.find("select area_name,area_code from sys_areas where area_type=2  order by area_code desc");
				setAttr("city", city);
				List<Areas> country = Areas.dao
						.find("select area_name,area_code from sys_areas where area_type=3  order by area_code desc");
				setAttr("country", country);
				if (purchase != null) {
					if (purchase.get("province_code") != null) {
						// 查询市
						city = Areas.dao
								.find("select area_name,area_code from sys_areas where area_type=2 and area_parent='"
										+ purchase.get("province_code")
										+ "' order by area_code desc");
						setAttr("city", city);
					}
					if (purchase.get("city_code") != null) {
						// 查询县
						country = Areas.dao
								.find("select area_name,area_code from sys_areas where area_type=3 and area_parent='"
										+ purchase.get("city_code")
										+ "' order by area_code desc");
						setAttr("country", country);
					}
				}
				setAttr("addOrEdit", addOrEdit);
				setAttr("send_address_id", send_address_id);
				setAttr("price", getPara("price"));
				setAttr("type", getPara("type"));
				setAttr("version_id", getPara("version_id"));
				setAttr("countNum", getPara("countNum"));
				setAttr("buy_ids", getPara("buy_id"));
				setAttr("buyId", getPara("buyId"));
				setAttr("id", getPara("id"));
				setAttr("commodity_type", getPara("commodity_type"));
				setAttr("forward_type", getPara("forward_type"));
				setAttr("is_order", getPara("is_order"));
				setAttr("slotId", getPara("slotId"));
				render("addr_edit.html");
			}
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 删除地址
	public void deleteAddr() {
		String from_user = getCookie("wx_openid");
		String remove_id = getPara("remove_id");
		Map map = new HashMap();
		if (!StringUtil.isEmpty(from_user)) {
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			if (opUser != null) {
				PurchaseSentAddress.dao.deleteById(remove_id);
				map.put("code", "0");
				map.put("msg", "删除成功");
			} else {
				map.put("code", "1");
				map.put("msg", "失败");
			}
			renderJson(map);
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 找到地址
	public void findAreas() {
		String area_type = getPara("area_type");
		String area_parent = getPara("area_parent");
		List<Areas> areaList = Areas.dao
				.find("select * from sys_areas where area_type='" + area_type
						+ "' and area_parent='" + area_parent
						+ "' order by area_code");
		renderJson(areaList);
	}

	// 新增地址、修改地址
	public void addAddress() {
		String from_user = getCookie("wx_openid");
		String is_default = getPara("r");// 是否默认
		String address_id = getPara("address_id");// ID
		String receiver_name = getPara("receiver_name");// 收货人
		String telephone = getPara("telephone");// 电话
		String province_code = getPara("province_code");// 省
		String city_code = getPara("city_code");// 市
		String county_code = getPara("county_code");// 县
		String address = getPara("address");// 详细地址
		String postCode = getPara("postCode");// 邮政编码
		PurchaseSentAddress sendAddress = new PurchaseSentAddress();
		Map map = new HashMap();
		if (!StringUtil.isEmpty(from_user)) {
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			if (opUser != null) {
				if (StringUtil.isEqual(is_default, "true")) {
					is_default = "1";
					Db.update("update purchase_sent_address set is_default = '0' where user_id = '"
							+ opUser.get("id") + "'");
				} else {
					is_default = "0";
				}
				if (StringUtil.isEmpty(address_id)) {
					PurchaseSentAddress add = PurchaseSentAddress.dao
							.findFirst("select count(*) as 'addcount' from purchase_sent_address where user_id = '"
									+ opUser.get("id") + "'");
					if (add.getLong("addcount") < 5) {
						sendAddress.set("id", StringUtil.getUUID())
								.set("user_id", opUser.get("id"))
								.set("province_code", province_code)
								.set("city_code", city_code)
								.set("county_code", county_code)
								.set("geography_location", address)
								.set("is_default", is_default)
								.set("zip_code", postCode)
								.set("telephone", telephone)
								.set("receiver_name", receiver_name)
								.set("create_by", opUser.get("id"))
								.set("create_name", opUser.get("nick_name"))
								.set("create_date", new Date())
								.set("remarks", "新增地址");
						sendAddress.save();
						map.put("code", "0");
						map.put("msg", "新增成功");
					} else {
						map.put("code", "1");
						map.put("msg", "送货地址最多5个");
					}

				} else {
					sendAddress.set("id", address_id)
							.set("user_id", opUser.get("id"))
							.set("province_code", province_code)
							.set("city_code", city_code)
							.set("county_code", county_code)
							.set("geography_location", address)
							.set("is_default", is_default)
							.set("zip_code", postCode)
							.set("telephone", telephone)
							.set("receiver_name", receiver_name)
							.set("update_by", opUser.get("id"))
							.set("update_name", opUser.get("nick_name"))
							.set("update_date", new Date())
							.set("remarks", "修改地址");
					sendAddress.update();
					map.put("code", "0");
					map.put("msg", "修改成功");
				}

			} else {
				map.put("code", "1");
				map.put("msg", "失败");
			}
			setAttr("slotId", getPara("slotId"));
			renderJson(map);
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}
	// 我的卡卷
	public void myTicket() {
		String from_user = getCookie("wx_openid");
		if (!StringUtil.isEmpty(from_user)) {
			//待使用
			Page<PurchaseUserTicket> ptr = PurchaseUserTicket.dao.paginate(1, 10, "select a.*,date_format(a.begin_date,'%Y.%m.%d') as 'df_begin_date',date_format(a.end_date,'%Y.%m.%d') as 'df_end_date' ", 
					"from purchase_user_ticket a where a.ticket_status='02' and a.wx_id='"+from_user+"' and TO_DAYS(NOW()) BETWEEN TO_DAYS(a.begin_date) AND TO_DAYS(a.end_date) order by a.get_date desc ");
			//已使用
			Page<PurchaseUserTicket> isptr = PurchaseUserTicket.dao.paginate(1, 10, "select a.*,date_format(a.begin_date,'%Y.%m.%d') as 'df_begin_date',date_format(a.end_date,'%Y.%m.%d') as 'df_end_date' ", "from purchase_user_ticket a where a.ticket_status='03' and a.wx_id='"+from_user+"' order by a.get_date desc");
			setAttr("ptrList", ptr.getList());
			setAttr("pageNumber", ptr.getPageNumber());
			setAttr("totalPage", ptr.getTotalPage());
			setAttr("isptrList", isptr.getList());
			setAttr("ispageNumber", isptr.getPageNumber());
			setAttr("istotalPage", isptr.getTotalPage());
			setAttr("server_uri", PropKit.get("server_uri"));
			render("ticket_list.html");
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}	
	}
	// 待使用卡卷分页
	public void ticketPage() {
		String from_user = getCookie("wx_openid");
		String page=getPara("page");
		if(StringUtil.isEmpty(page)){
			page="1";
		}
		if (!StringUtil.isEmpty(from_user)) {
			//我的卡卷
			Page<PurchaseUserTicket> ptrList = PurchaseUserTicket.dao.paginate(Integer.parseInt(page), 10, "select a.*,date_format(a.begin_date,'%Y.%m.%d') as 'df_begin_date',date_format(a.end_date,'%Y.%m.%d') as 'df_end_date' ", "from purchase_user_ticket a where a.ticket_status='02' and a.wx_id='"+from_user+"' order by a.get_date desc ");
			setAttr("server_uri", PropKit.get("server_uri"));
			renderJson(ptrList);
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}		
	}
	// 已使用卡卷分页
	public void isticketPage() {
		String from_user = getCookie("wx_openid");
		String page=getPara("page");
		if(StringUtil.isEmpty(page)){
			page="1";
		}
		if (!StringUtil.isEmpty(from_user)) {
			//我的卡卷
			Page<PurchaseUserTicket> isptrList = PurchaseUserTicket.dao.paginate(Integer.parseInt(page), 10, "select a.*,date_format(a.begin_date,'%Y.%m.%d') as 'df_begin_date',date_format(a.end_date,'%Y.%m.%d') as 'df_end_date' ", "from purchase_user_ticket a where a.ticket_status='03' and a.wx_id='"+from_user+"' order by a.get_date desc ");
			setAttr("server_uri", PropKit.get("server_uri"));
			renderJson(isptrList);
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}		
	}
	// 商品收藏
	public void commodityCollection() {
		String from_user = getCookie("wx_openid");
		if (!StringUtil.isEmpty(from_user)) {
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			if (opUser != null) {
				setAttr("opUser", opUser);
			}
			Page<PurchaseCommodity> commodityColle = PurchaseCommodity.dao
					.paginate(
							1,
							10,
							"select a.*,b.id as 'collect_id',b.commodity_name,b.commodity_logo,b.commodity_price,b.del_flag ",
							" from purchase_commodity_collect a left join purchase_commodity b on a.commodity_id=b.id where a.user_id='"
									+ opUser.get("id") + "' order by sort desc");
			setAttr("collectList", commodityColle.getList());
			setAttr("pageNumber", commodityColle.getPageNumber());
			setAttr("totalPage", commodityColle.getTotalPage());
			setAttr("server_uri", PropKit.get("server_uri"));
			render("favorite.html");
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 浏览记录
	public void browseRecord() {
		String from_user = getCookie("wx_openid");
		if (!StringUtil.isEmpty(from_user)) {
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			if (opUser != null) {
				setAttr("opUser", opUser);
			}
			Page<PurchaseCommodity> commodityColle = PurchaseCommodity.dao
					.paginate(
							1,
							10,
							"select a.*,b.id as 'record_id',b.commodity_name,b.commodity_logo,b.commodity_price,b.del_flag ",
							" from purchase_commodity_record a left join purchase_commodity b on a.commodity_id=b.id where a.user_id='"
									+ opUser.get("id")
									+ "' order by a.record_date desc");
			setAttr("recordList", commodityColle.getList());
			setAttr("pageNumber", commodityColle.getPageNumber());
			setAttr("totalPage", commodityColle.getTotalPage());
			setAttr("server_uri", PropKit.get("server_uri"));
			render("overview.html");
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}
	/**
	 * 浏览记录分页
	 */
	public void recordPage(){
		String from_user = getCookie("wx_openid");
		String page=getPara("page");
		if(StringUtil.isEmpty(page)){
			page="1";
		}
		if (!StringUtil.isEmpty(from_user)) {
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			Page<PurchaseCommodity> recordsList = PurchaseCommodity.dao.paginate(Integer.parseInt(page), 10, 
					"select a.*,b.id as 'record_id',b.commodity_name,b.commodity_logo,b.commodity_price,b.del_flag ",
					" from purchase_commodity_record a left join purchase_commodity b on a.commodity_id=b.id where a.user_id='"
							+ opUser.get("id")
							+ "' order by a.record_date desc");
			setAttr("server_uri", PropKit.get("server_uri"));
			renderJson(recordsList); 
		}else{
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}   
	// 删除记录
	public void deleteRecord() {
		String from_user = getCookie("wx_openid");
		String record_id = getPara("record_id");
		Map map = new HashMap();
		if (!StringUtil.isEmpty(from_user)) {
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			if (opUser != null) {
				PurchaseCommodityRecord.dao.deleteById(record_id);
				map.put("code", "0");
				map.put("msg", "删除成功");
			} else {
				map.put("code", "1");
				map.put("msg", "失败");
			}
			renderJson(map);
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 清空记录
	public void emptyRecord() {
		String from_user = getCookie("wx_openid");
		Map map = new HashMap();
		if (!StringUtil.isEmpty(from_user)) {
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			if (opUser != null) {
				List<PurchaseCommodityRecord> commodityRecord = PurchaseCommodityRecord.dao
						.find("select * from purchase_commodity_record where user_id='"
								+ opUser.get("id") + "'");
				for (int i = 0; i < commodityRecord.size(); i++) {
					PurchaseCommodityRecord.dao.deleteById(commodityRecord.get(
							i).get("id"));
				}
				map.put("code", "0");
				map.put("msg", "完成清空");
			} else {
				map.put("code", "1");
				map.put("msg", "失败");
			}
			renderJson(map);
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 订单详情
	public void orderDetails() {
		String from_user = getCookie("wx_openid");
		String order_status = getPara("order_status");
		String type = getPara("type");
		String queryStr = getPara("seek");
		String seekSql = "";
		String page = getPara("page");

		if (StringUtil.isEmpty(page)) {
			page = "1";
		}
		if (!StringUtil.isEmpty(queryStr)) {
			seekSql = "and t.order_no like '%" + queryStr + "%' ";
		}
		if (!StringUtil.isEmpty(from_user)) {
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			if (opUser != null) {
				setAttr("opUser", opUser);
			}
			String sql = " from purchase_commodity_buy_total t left join wb_key k on t.order_status = k.key_name where key_type = 'purchase_commodity_buy.order_status' and t.user_id ='"
					+ opUser.get("id") + "' ";

			if (!StringUtil.isEmpty(order_status)) {
				sql += " and t.order_status='" + order_status + "' " + seekSql
						+ " order by t.create_date desc";
			} else {
				sql += " " + seekSql
						+ " order by t.order_status asc , t.create_date desc";
			}
			Page<PurchaseCommodityBuyTotal> commodityOrder = PurchaseCommodityBuyTotal.dao
					.paginate(Integer.parseInt(page), 10,
							"select t.*,k.key_value ", sql);

			if (StringUtil.isEqual(type, "1")) {
				List<PurchaseCommodityBuyTotal> allStandByPay = PurchaseCommodityBuyTotal.dao
						.find("select *from purchase_commodity_buy_total t where  t.user_id ='"
								+ opUser.get("id")
								+ "' and t.order_status = '01'");
				List<PurchaseCommodityBuyTotal> allDelivery = PurchaseCommodityBuyTotal.dao
						.find("select * from purchase_commodity_buy_total t where  t.user_id ='"
								+ opUser.get("id")
								+ "' and t.order_status = '02'");
				List<PurchaseCommodityBuyTotal> allStandByCollect = PurchaseCommodityBuyTotal.dao
						.find("select * from purchase_commodity_buy_total t   where  t.user_id ='"
								+ opUser.get("id")
								+ "' and t.order_status = '03'");
				List<PurchaseCommodityBuyTotal> allEvaluate = PurchaseCommodityBuyTotal.dao
						.find("select * from purchase_commodity_buy_total t   where  t.user_id ='"
								+ opUser.get("id")
								+ "' and t.order_status = '08'");
				System.out.println(commodityOrder.getList());
				setAttr("orderList", commodityOrder.getList());
				setAttr("pageNumber", commodityOrder.getPageNumber());
				setAttr("totalPage", commodityOrder.getTotalPage());
				setAttr("order_status", order_status);
				setAttr("allStandByPay", allStandByPay.size());
				setAttr("allDelivery", allDelivery.size());
				setAttr("allStandByCollect", allStandByCollect.size());
				setAttr("allEvaluate", allEvaluate.size());
				setAttr("order_status", order_status);
				render("order_list.html");
			} else {
				Map<Object, Object> map = new HashMap<Object, Object>();
				map.put("orderList", commodityOrder.getList());
				map.put("pageNumber", commodityOrder.getPageNumber());
				map.put("totalPage", commodityOrder.getTotalPage());
				setAttr("order_status", order_status);
				renderJson(commodityOrder);
			}
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 我的一元购
	public void MyOneBuy() {
		String from_user = getCookie("wx_openid");
		String is_win = getPara("is_win");
		String status = getPara("status");
		String type = getPara("type");
		String page = getPara("page");
		String check_type = "";

		if (StringUtil.isEmpty(page)) {
			page = "1";
		}
		if (!StringUtil.isEmpty(from_user)) {
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			if (opUser != null) {
				setAttr("opUser", opUser);
			}
			String sql = "from (select m.* from (select a.*,b.nick_name,c.time_slot as 'time_slot_num',c.lucky_num as 'win_num',c.total_count,c.surplus_count,c.status,d.is_add_addr,d.send_address,d.receiver_name,d.receiver_telephone,d.order_status as 'total_order_status',d.express_company,d.express_no FROM `purchase_onepurchase_buy` a LEFT JOIN purchase_users b on b.id=a.user_id LEFT JOIN purchase_time_slot c on c.id=a.time_slot LEFT JOIN purchase_onepurchase_buy_total d on a.total_id=d.id where a.user_id='"
					+ opUser.get("id") + "' and a.pay_status='02' ";
			if (StringUtil.isEqual(is_win, "")
					&& StringUtil.isEqual(status, "")) {
				check_type = "";
			} else if (StringUtil.isEqual(is_win, "02")
					&& StringUtil.isEqual(status, "")) {
				sql += " and a.is_win='02'";
				check_type = "01";
			} else if (StringUtil.isEqual(is_win, "")
					&& StringUtil.isEqual(status, "01")) {
				sql += " and c.status='01' and a.order_status='09'";
				check_type = "02";
			} else if (StringUtil.isEqual(is_win, "")
					&& StringUtil.isEqual(status, "03")) {
				sql += " and c.status='03'";
				check_type = "03";
			} else if (StringUtil.isEqual(is_win, "")
					&& StringUtil.isEqual(status, "02")) {
				sql += " and c.status='02'";
				check_type = "04";
			}
			sql += "order by a.is_win desc,d.is_add_addr desc) m GROUP BY m.time_slot,m.user_id )  t ORDER BY t.buy_date DESC ";
			Page<PurchaseOnepurchaseBuy> oneBuy = PurchaseOnepurchaseBuy.dao
					.paginate(
							Integer.parseInt(page),
							10,
							"SELECT t.*,(select GROUP_CONCAT(lucky_num)  from purchase_onepurchase_buy where user_id=t.user_id and time_slot=t.time_slot and pay_status='02') as 'luckyNum',(select count(*)  from purchase_onepurchase_buy where user_id=t.user_id and time_slot=t.time_slot and pay_status='02' ) as 'countPart',(select GROUP_CONCAT(distinct version_type)  from purchase_onepurchase_buy where user_id=t.user_id and time_slot=t.time_slot and pay_status='02') as 'versionType'",
							sql);
			if (StringUtil.isEqual(type, "1")) {
				List<PurchaseOnepurchaseBuy> indianaRecord = PurchaseOnepurchaseBuy.dao
						.find("select b.*,u.nick_name from purchase_onepurchase_buy b left join purchase_users u on u.id = b.user_id where b.user_id = '"
								+ opUser.get("id")
								+ "' and b.pay_status='02' GROUP BY time_slot ");
				List<PurchaseOnepurchaseBuy> rewardRecord = PurchaseOnepurchaseBuy.dao
						.find("select b.*,u.nick_name from purchase_onepurchase_buy b left join purchase_users u on u.id = b.user_id where b.is_win='02' and b.user_id = '"
								+ opUser.get("id")
								+ "' and b.pay_status='02' GROUP BY time_slot");
				List<PurchaseTimeSlot> Conduct = PurchaseTimeSlot.dao
						.find("SELECT a.*,b.nick_name,c.total_count,c.surplus_count FROM purchase_onepurchase_buy a LEFT JOIN purchase_users b on b.id=a.user_id LEFT JOIN purchase_time_slot c on c.id=a.time_slot where c.status='01' and a.order_status='09' and a.user_id='"
								+ opUser.get("id")
								+ "' and a.pay_status='02' GROUP BY a.time_slot");
				List<PurchaseTimeSlot> Announced = PurchaseTimeSlot.dao
						.find("SELECT a.*,b.nick_name,c.total_count,c.surplus_count FROM purchase_onepurchase_buy a LEFT JOIN purchase_users b on b.id=a.user_id LEFT JOIN purchase_time_slot c on c.id=a.time_slot where c.status='03' and a.user_id='"
								+ opUser.get("id")
								+ "' and a.pay_status='02' GROUP BY a.time_slot");
				List<PurchaseTimeSlot> noAnnounced = PurchaseTimeSlot.dao
						.find("SELECT a.*,b.nick_name,c.total_count,c.surplus_count FROM purchase_onepurchase_buy a LEFT JOIN purchase_users b on b.id=a.user_id LEFT JOIN purchase_time_slot c on c.id=a.time_slot where c.status='02' and a.user_id='"
								+ opUser.get("id")
								+ "' and a.pay_status='02' GROUP BY a.time_slot");
				setAttr("oneBuyList", oneBuy.getList());
				setAttr("pageNumber", oneBuy.getPageNumber());
				setAttr("totalPage", oneBuy.getTotalPage());
				setAttr("indianaRecord", indianaRecord.size());
				setAttr("rewardRecord", rewardRecord.size());
				setAttr("Conduct", Conduct.size());
				setAttr("Announced", Announced.size());
				setAttr("noAnnounced", noAnnounced.size());
				setAttr("server_uri", PropKit.get("server_uri"));
				setAttr("check_type", check_type);
				setAttr("is_win", is_win);
				setAttr("status", status);
				render("one_buy.html");
			} else {
				Map<Object, Object> map = new HashMap<Object, Object>();
				map.put("oneBuyList", oneBuy.getList());
				map.put("pageNumber", oneBuy.getPageNumber());
				map.put("totalPage", oneBuy.getTotalPage());
				map.put("check_type", check_type);
				renderJson(oneBuy);
			}
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 我的宝贝评论
	public void myBabycontent() {
		String from_user = getCookie("wx_openid");
		String mId = getPara("mId");
		String totalId = getPara("totalId");
		String id = getPara("id");
		if (!StringUtil.isEmpty(from_user)) {
			setAttr("mId", mId);
			setAttr("totalId", totalId);
			setAttr("id", id);
			setAttr("order_status", getPara("order_status"));
			render("order_content.html");
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 商品评价
	public void submitCommodityContent() {
		String from_user = getCookie("wx_openid");
		String mId = getPara("commodity_id");
		String totalId = getPara("total_id");
		String id = getPara("id");
		String commodity_content = getPara("commodity_content");
		Map map = new HashMap();
		// PurchaseCommodityBuy commodityBuy = new PurchaseCommodityBuy();
		if (!StringUtil.isEmpty(from_user)) {
			OnePurchaseUser opUser = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id = '"
							+ from_user + "'");
			if (opUser != null) {
				setAttr("opUser", opUser);
			}
			try {
				Map<String, Object> babyMap = new HashMap<String, Object>();
				PurchaseCommodityBuy commodityBuy = PurchaseCommodityBuy.dao
						.findById(id);
				String baby_id = StringUtil.getUUID();
				babyMap.put("id", baby_id);
				babyMap.put("commodity_id", mId);
				babyMap.put("commodity_logo",
						commodityBuy.get("commodity_logo"));
				babyMap.put("version_type", commodityBuy.get("version_type"));
				babyMap.put("user_id", opUser.get("id"));
				babyMap.put("user_name", opUser.get("nick_name"));
				babyMap.put("user_logo", opUser.get("head_path"));
				babyMap.put("commodity_content", commodity_content);
				babyMap.put("content_date", new Date());
				PurchaseCommodityContent.dao.setAttrs(babyMap);
				PurchaseCommodityContent.dao.save();
				// 修改明细表状态 1.先根据总表id和商品id 去明细表查出明细表主键id
				PurchaseCommodityBuy commodityBuyNew = new PurchaseCommodityBuy();
				commodityBuyNew.set("id", id).set("order_status", "04")
						.set("update_by", opUser.get("id"))
						.set("update_name", opUser.get("nick_name"))
						.set("update_date", new Date())
						.set("remarks", "修改订单状态");
				commodityBuyNew.update();
				// 修改总表状态1.先根据总表id和商品id 去明细表查出明细表 sql total_id=总表id
				// 但是commodity_id <> 商品id
				List<PurchaseCommodityBuy> commodityB = PurchaseCommodityBuy.dao
						.find("select * from purchase_commodity_buy where  total_id='"
								+ totalId + "' and order_status='08'");
				if (commodityB.size() == 0) {
					PurchaseCommodityBuyTotal commodityBuyTotalNew = new PurchaseCommodityBuyTotal();
					commodityBuyTotalNew.set("id", totalId)
							.set("order_status", "04")
							.set("update_by", opUser.get("id"))
							.set("update_name", opUser.get("nick_name"))
							.set("update_date", new Date())
							.set("remarks", "修改订单状态");
					commodityBuyTotalNew.update();
				}
				map.put("code", "0");
				map.put("msg", "提交成功");
			} catch (Exception e) {
				System.out.println(e);
				map.put("code", "-1");
				map.put("msg", "提交失败");
			}
			renderJson(map);
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 订单详情列表
	public void orderDetailList() {
		String from_user = getCookie("wx_openid");
		String total_id = getPara("total_id"); // 总表id
		String commodity_type = getPara("commodity_type"); // 商品类型
		// String countNum=getPara("countNum"); // 购买数量
		if (!StringUtil.isEmpty(from_user)) {
			OnePurchaseUser user = OnePurchaseUser.dao
					.findFirst("select * from purchase_users where wx_id='"
							+ from_user + "'");
			PurchaseCommodityBuyTotal commodityBuyTotal = PurchaseCommodityBuyTotal.dao
					.findById(total_id);
			List<PurchaseCommodityBuy> commodityList = PurchaseCommodityBuy.dao
					.find("select * from purchase_commodity_buy where total_id='"
							+ total_id + "'");
			PurchaseCommodityBuy commodityBuy = PurchaseCommodityBuy.dao
					.findFirst("select sum(total_price) as 'total_price' from purchase_commodity_buy where total_id = '"
							+ total_id + "'");
			String sql = "select t.*,k.key_value from purchase_commodity_buy_total t left join wb_key k on t.order_status = k.key_name where key_type = 'purchase_commodity_buy.order_status' and t.id ='"
					+ total_id + "'";
			PurchaseCommodityBuyTotal commodityTotal = PurchaseCommodityBuyTotal.dao
					.findFirst(sql);
			// String
			// sql="select a.*,b.area_name as 'province_name',c.area_name as 'city_name',d.area_name as 'country_name' from purchase_sent_address a left join sys_areas b on b.area_code=a.province_code left join sys_areas c on c.area_code=a.city_code  left join sys_areas d on d.area_code=a.county_code where a.user_id='"+user.get("id")+"' ";
			PurchaseSentAddress sendAddress = PurchaseSentAddress.dao
					.findFirst("select a.*,t.send_address from purchase_sent_address a left join purchase_commodity_buy_total t on a.id=t.send_address_id where t.id = '"
							+ total_id + "'");
			for (int i = 0; i < commodityList.size(); i++) {
				setAttr("per_price", commodityList.get(i).get("per_price"));
				setAttr("total_price", commodityList.get(i).get("total_price"));
			}
			setAttr("server_uri", PropKit.get("server_uri"));
			setAttr("key_value", commodityTotal.get("key_value"));
			setAttr("express_company", commodityTotal.get("express_company"));
			setAttr("express_no", commodityTotal.get("express_no"));
			setAttr("commodityList", commodityList);
			setAttr("sendAddress", sendAddress);
			setAttr("buy_ids", total_id);
			setAttr("commodity_type", commodity_type);
			setAttr("order_status", getPara("order_status"));
			setAttr("commodityBuyTotal", commodityBuyTotal);
			render("commodity_detail.html");
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 确认收获
	public void harvest() {
		String from_user = getCookie("wx_openid");
		String totalId = getPara("totalId");
		Map map = new HashMap();
		if (!StringUtil.isEmpty(from_user)) {
			PurchaseCommodityBuyTotal checkBuyTotal = PurchaseCommodityBuyTotal.dao
					.findFirst("select * from purchase_commodity_buy_total where id='"
							+ totalId + "' and order_status='03' ");
			if (checkBuyTotal != null) {
				PurchaseCommodityBuyTotal commodityBuyTotal = new PurchaseCommodityBuyTotal();
				OnePurchaseUser opUser = OnePurchaseUser.dao
						.findFirst("select * from purchase_users where wx_id = '"
								+ from_user + "'");
				if (opUser != null) {
					commodityBuyTotal.set("id", totalId)
							.set("order_status", "08")
							.set("receipt_date", new Date())
							.set("update_by", opUser.get("id"))
							.set("update_name", opUser.get("nick_name"))
							.set("update_date", new Date())
							.set("receipt_date", new Date())
							.set("order_remark", "手动收货")
							.set("remarks", "修改订单状态");
					commodityBuyTotal.update();
					Db.update("update purchase_commodity_buy set order_status='08' where total_id='"
							+ totalId + "'");
					if (!StringUtil.isEmpty(checkBuyTotal.getStr("agent_id"))) {
						TradeMarketProfit checkMarketProfit = TradeMarketProfit.dao
								.findFirst("select * from trade_market_profit where agent_id='"
										+ checkBuyTotal.getStr("agent_id")
										+ "' and total_id='" + totalId + "'");
						if (checkMarketProfit == null) { 
							//-1表示小于,0是等于,1是大于.
							if(checkBuyTotal
									.getBigDecimal("agent_total_revenue").compareTo(new BigDecimal(0)) == 1){
							/*Db.update("update trade_market_user set trade_profit=trade_profit+"
									+ checkBuyTotal
											.getBigDecimal("agent_total_revenue")
									+ ",account_balance=account_balance+"+checkBuyTotal
									.getBigDecimal("agent_total_revenue")+" where id= '"
									+ checkBuyTotal.getStr("agent_id") + "' ");*/
							GregorianCalendar g = new GregorianCalendar();
							int year = g.get(GregorianCalendar.YEAR);
							int month = g.get(GregorianCalendar.MONTH) + 1;
							TradeMarketProfit marketProfit = TradeMarketProfit.dao
									.set("id", StringUtil.getUUID())
									.set("agent_id",
											checkBuyTotal.getStr("agent_id"))
									.set("total_id", totalId)
									.set("agent_profit",
											checkBuyTotal
													.getBigDecimal("agent_total_revenue"))
									.set("transfer_date", new Date())
									.set("transfer_year", year)
									.set("transfer_month", month)
									.set("create_by", opUser.get("id"))
									.set("create_name", opUser.get("nick_name"))
									.set("create_date", new Date())
									.set("update_by", opUser.get("id"))
									.set("update_name", opUser.get("nick_name"))
									.set("update_date", new Date())
									.set("remarks", "下放代理商收益");
							marketProfit.save();
							}
						}
					}
					map.put("code", "0");
					map.put("msg", "成功");

				} else {
					map.put("code", "1");
					map.put("msg", "失败");
				}
			} else {
				map.put("code", "1");
				map.put("msg", "失败");
			}
			renderJson(map);

		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 我的宝贝退款理由
	public void myBabyrefund() {
		String from_user = getCookie("wx_openid");
		String totalId = getPara("totalId");
		if (!StringUtil.isEmpty(from_user)) {
			setAttr("totalId", totalId);
			setAttr("order_status", getPara("order_status"));
			render("order_refund.html");
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

	// 退款理由
	public void submitCommodityRefund() {
		String from_user = getCookie("wx_openid");
		String totalId = getPara("id");
		String commodity_refund = getPara("commodity_refund");
		Map map = new HashMap();
		if (!StringUtil.isEmpty(from_user)) {
			PurchaseCommodityBuyTotal selBuyTotal = PurchaseCommodityBuyTotal.dao
					.findById(totalId);
			try {
				OnePurchaseUser opUser = OnePurchaseUser.dao
						.findFirst("select * from purchase_users where wx_id = '"
								+ from_user + "'");
				if (opUser != null) {
					setAttr("opUser", opUser);
				}
				PurchaseCommodityBuyTotal checkBuyTotal=PurchaseCommodityBuyTotal.dao.findFirst("select * from purchase_commodity_buy_total where id='"+totalId+"' ");
				if(checkBuyTotal != null ){
					if(StringUtil.isEqual(checkBuyTotal.getStr("order_status"), "02") || StringUtil.isEqual(checkBuyTotal.getStr("order_status"), "03")){
				PurchaseCommodityBuyTotal commodityBuyTotal = new PurchaseCommodityBuyTotal()
						.set("id", totalId).set("order_status", "05")
						.set("commodity_refund", commodity_refund)
						.set("update_by", opUser.get("id"))
						.set("update_name", opUser.get("nick_name"))
						.set("update_date", new Date())
						.set("remarks", "修改订单状态")
						.set("refund_price", checkBuyTotal.get("actually_paid_money"))
						;
				commodityBuyTotal.update();
				Db.update("update purchase_commodity_buy set order_status='05' where total_id='"
						+ totalId + "'");
				map.put("code", "0");
				map.put("msg", "提交成功");
				}else{
					map.put("code", "-2");
					map.put("msg", "订单状态已发生变化,无法退款");
				}
				}else{
					map.put("code", "-3");
					map.put("msg", "订单不存在");
				}
			} catch (Exception e) {
				map.put("code", "-1");
				map.put("msg", "提交失败");
			}
			renderJson(map);
		} else {
			String url = getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x") + 1, url.length()));
			render("error.html");
		}
	}

}
