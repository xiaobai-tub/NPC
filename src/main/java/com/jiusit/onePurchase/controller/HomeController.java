package com.jiusit.onePurchase.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.jfinal.ApiController;
import com.jiusit.common.utils.DateUtil;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.interceptor.UserInterceptor;
import com.jiusit.onePurchase.model.PurchaseAdvertisement;
import com.jiusit.onePurchase.model.PurchaseCommodity;
import com.jiusit.onePurchase.model.PurchaseCommodityCar;
import com.jiusit.onePurchase.model.PurchaseCommodityCollect;
import com.jiusit.onePurchase.model.PurchaseCommodityContent;
import com.jiusit.onePurchase.model.PurchaseCommodityRecord;
import com.jiusit.onePurchase.model.PurchaseCommodityVersion;
import com.jiusit.onePurchase.model.PurchaseShareTicket;
import com.jiusit.onePurchase.model.PurchaseTicket;
import com.jiusit.onePurchase.model.PurchaseTicketRandom;
import com.jiusit.onePurchase.model.PurchaseUserTicket;
import com.jiusit.onePurchase.model.PurchaseVersionExt;
import com.jiusit.onePurchase.model.OnePurchaseUser;
import com.jiusit.onePurchase.model.PurchaseZhUser;
import com.jiusit.onePurchase.model.TradeMarketShare;
import com.jiusit.onePurchase.model.WbVar;
import com.jiusit.onePurchase.service.CommonStoredProcedureService;
import com.jiusit.onePurchase.service.StoredProcedureService;
@ControllerBind(controllerKey="/home",viewPath="/pages")
@Before(UserInterceptor.class)


/*
 * 首页
 * */
public class HomeController extends ApiController{
	private static final Logger log = Logger
			.getLogger(HomeController.class);

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
   //首页
	
	public void homePage(){
		String from_user=getCookie("wx_openid");
		String is_h5=getPara("h5"); 
		System.out.println("code--------------"+getPara("code"));
		if(!StringUtil.isEmpty(from_user)){
		OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id='"+from_user+"'");
		List <PurchaseAdvertisement> topAdvertisementList=PurchaseAdvertisement.dao.find("select * from purchase_advertisement where top_show='1' and is_enable='1' order by sort ");
		PurchaseAdvertisement advertisement=PurchaseAdvertisement.dao.findFirst("select * from purchase_advertisement where top_show='0' and is_enable='1' order by sort");
		Page <PurchaseCommodity> commodityPage=PurchaseCommodity.dao.paginate(1,16,"select a.* "," from purchase_commodity a where a.commodity_category!='04' and a.commodity_type='01' and a.commodity_status='01' and (select count(*) from purchase_commodity_version where commodity_id=a.id and version_stock > 0 ) >0 order by a.sort");
		Page <PurchaseCommodity> purchasePage=PurchaseCommodity.dao.paginate(1,10,"select b.commodity_name,b.commodity_logo,a.total_count,a.surplus_count,a.commodity_id,a.id,(a.total_count-a.surplus_count)*100/a.total_count as 'per' ","from purchase_time_slot a left join purchase_commodity b on b.id=a.commodity_id  where b.commodity_type='02' and b.commodity_status='01' and a.is_over='0' and status ='01' and NOW() BETWEEN a.begin_date and a.end_date  order by a.sort, a.surplus_count ");
		setAttr("topAdvertisementList", topAdvertisementList);
		setAttr("advertisement", advertisement);
		setAttr("commodityList", commodityPage.getList());
		setAttr("commodityTotalPage", commodityPage.getTotalPage());
		setAttr("commodityPageNumber", commodityPage.getPageNumber());
		setAttr("purchaseList", purchasePage.getList());
		setAttr("purchaseTotalPage", purchasePage.getTotalPage());
		setAttr("purchasePageNumber", purchasePage.getPageNumber());
		setAttr("login_num", user.getInt("login_num"));
		setAttr("is_h5", is_h5);
		setAttr("server_uri", PropKit.get("server_uri"));
		render("index.html");
		}else{
			String url=getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
			render("error.html");
		}
	}
	// 跳转到领取卡卷页面
	public void forwardTicket() {
		String from_user=getCookie("wx_openid");
		OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id='"+from_user+"'");
		if (user != null) {
			WbVar wv = WbVar.dao.findFirst("select * from wb_var where  VAR_NAME='ticket_id' ");
			if(wv!=null){
				PurchaseTicketRandom ptr = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_id='"+wv.getStr("VAR_VALUE")+"' and ticket_status='01' AND ticket_type='01' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ");
				setAttr("ptr", ptr);
				PurchaseTicket pt = PurchaseTicket.dao
						.findFirst("SELECT a.*,b.key_value AS 'card_type_s' FROM purchase_ticket a LEFT JOIN wb_key b ON a.card_type=b.key_name WHERE a.id='"+wv.getStr("VAR_VALUE")+"' AND a.STATUS='1' AND a.ticket_type='01' AND b.key_type='purchase_ticket.card_type' AND NOT EXISTS (SELECT * FROM purchase_user_ticket WHERE wx_id='"+from_user+"' AND ticket_id='"+wv.getStr("VAR_VALUE")+"' and get_type ='01' )");
				setAttr("pt", pt);
				PurchaseUserTicket purchaseUserTicket = PurchaseUserTicket.dao
						.findFirst("SELECT a.*,b.key_value AS 'card_type_s' FROM purchase_user_ticket a LEFT JOIN wb_key b ON a.card_type=b.key_name WHERE a.ticket_id='"+wv.getStr("VAR_VALUE")+"' and a.wx_id='"+from_user+"' and a.get_type='01'  ");
				setAttr("purchaseUserTicket", purchaseUserTicket);
			}
			render("ticket_page.html");
		}else{
			String url=getRequest().getRequestURI();
			System.out.println(url);
			setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
			render("error.html");
		}
	}
	// 跳转到招行活动卡卷页面
	public void forwardZhTicket() {
		String from_user=getCookie("wx_openid");
		OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select a.*,url_decode(a.nick_name) as 'nick_name_df' from purchase_users a where a.wx_id='"+from_user+"'");
		if (user != null) {
			setAttr("user", user);
			PurchaseTicketRandom ptr = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_status='01' AND ticket_type='01' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ");
			setAttr("ptr", ptr);
			PurchaseUserTicket puTicket = PurchaseUserTicket.dao
					.findFirst("SELECT a.*,b.key_value AS 'card_type_s' FROM purchase_user_ticket a LEFT JOIN wb_key b ON a.card_type=b.key_name WHERE a.wx_id='"+from_user+"' and a.get_type='04' ");
			if(puTicket==null){
				PurchaseTicket pt = PurchaseTicket.dao
						.findFirst("SELECT a.*,b.key_value AS 'card_type_s' FROM purchase_ticket a LEFT JOIN wb_key b ON a.card_type=b.key_name WHERE a.STATUS='1' AND a.ticket_type='01' AND a.card_category='06' AND b.key_type='purchase_ticket.card_type' AND NOT EXISTS (SELECT * FROM purchase_user_ticket WHERE wx_id='"+from_user+"' AND get_type='04' )  ORDER BY rand() limit 0,1");
				setAttr("pt", pt);
				if(pt!=null){
					PurchaseTicketRandom purchaseTicketRandom = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_status='01' and ticket_id='"+pt.getStr("id")+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ORDER BY rand() limit 0,1 ");
					if(purchaseTicketRandom!=null){
						  StoredProcedureService.trackresult(purchaseTicketRandom.getStr("id"),from_user,user.getStr("id"),user.getStr("nick_name"),new BigDecimal("0"),"04",2,3,"01");
						  PurchaseUserTicket purchaseUserTicket = PurchaseUserTicket.dao
									.findFirst("SELECT a.*,b.key_value AS 'card_type_s' FROM purchase_user_ticket a LEFT JOIN wb_key b ON a.card_type=b.key_name WHERE a.wx_id='"+from_user+"' and a.get_type='04' ");
							setAttr("purchaseUserTicket", purchaseUserTicket);
					}
				}
			}
			setAttr("puTicket", puTicket);
			render("zh_ticket_page.html");
		}else{
			String url=getRequest().getRequestURI();
			System.out.println(url);
			setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
			render("error.html");
		}
	}	
	// 用户领取卡卷
	public void toTicket() {
		Map map=new HashMap();
		String from_user=getCookie("wx_openid");
		String ticket_id = getPara("ticket_id");
		OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id='"+from_user+"'");
		if (user != null) {
			PurchaseTicket pt = PurchaseTicket.dao.findFirst("select * from purchase_ticket where id='"+ticket_id+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date)");
			if(pt!=null){
				PurchaseTicketRandom ptr = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_status='01' and ticket_id='"+ticket_id+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ORDER BY rand() limit 0,1 ");
				if(ptr!=null){
					  int result=StoredProcedureService.trackresult(ptr.getStr("id"),from_user,user.getStr("id"),user.getStr("nick_name"),new BigDecimal("0"),"01",2,3,"01");
					if(result >= 1){
						map.put("code", 0);
						map.put("msg", "领取成功");
					}else{
						map.put("code", 0);
						map.put("msg", "系统繁忙，稍后再试");
					}
					
				}else{
					map.put("code", -1);
					map.put("msg", "卡券已领完");
				}
			}else{
				map.put("code", -1);
				map.put("msg", "卡券已过期");
			}
			renderJson(map);
		}else{
			String url=getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
			render("error.html");
		}
	}
	// 用户首页领取卡卷
	public void toIndexTicket() {
		Map map=new HashMap();
		String from_user=getCookie("wx_openid");
		OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id='"+from_user+"'");
		if (user != null) {
			PurchaseUserTicket puTicket = PurchaseUserTicket.dao.findFirst("select * from purchase_user_ticket where ticket_type='01' and wx_id='"+from_user+"' and get_type='04' and card_category='08' ");
			if(puTicket==null){
				int money = 0;
				int rate0  = 5;
				int rate1  = 10;
				int rate2  = 25;
				int rate3  = 60;
				int tempInt  = RandomUtils.nextInt(1, 100+1);
				if (tempInt  > 0 && tempInt <= rate0){
					money = 100;
			    }else if (tempInt > rate0 && tempInt <= rate0+rate1) {
			    	money = 10;
			    }else if (tempInt > rate1 && tempInt <= rate0+rate1+rate2) {
			    	money = 5;
			    }else if(tempInt > rate2 && tempInt <= rate0+rate1+rate2+rate3) {
			    	money = 2;
			    } 
				PurchaseTicket pt = PurchaseTicket.dao.findFirst("select * from purchase_ticket where ticket_type='01' and card_category='08' and status='1' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) and mortgage_amount="+money+" ");
				if(pt!=null){
					PurchaseTicketRandom ptr = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_status='01' and ticket_id='"+pt.getStr("id")+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ORDER BY rand() limit 0,1 ");
					if(ptr!=null){
						int result=CommonStoredProcedureService.trackresult(ptr.getStr("id"),from_user,user.getStr("id"),user.getStr("nick_name"),new BigDecimal("0"),"04","01");
						if(result >= 1){
							map.put("code", 0);
							map.put("mortgage_amount", ptr.getBigDecimal("mortgage_amount"));
						}else{
							map.put("code", -2);
							map.put("msg", "系统繁忙，稍后再试");
						}
					}else{
						map.put("code", -1);
						map.put("msg", "卡券已领完");
					}
				}else{
					map.put("code", -1);
					map.put("msg", "暂无卡券可领取");
				}
			}else{
				map.put("code", -1);
				map.put("msg", "已领取卡券");
			}
			renderJson(map);	
		}else{
			String url=getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
			render("error.html");
		}
	}	
	//首页领取页面
	public void indexTicketPage(){
		String from_user=getCookie("wx_openid");
		String share_id = getPara("share_id");
		OnePurchaseUser shareUser=OnePurchaseUser.dao.findFirst("select *,url_decode(nick_name) as 'nick_name_d' from purchase_users where wx_id='"+share_id+"'");
		setAttr("shareUser", shareUser);
		OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select *,url_decode(nick_name) as 'nick_name_d' from purchase_users where wx_id='"+from_user+"'");
		setAttr("user", user);
		if (user != null) {
			if(from_user.equals(share_id)){
				PurchaseUserTicket puTicket = PurchaseUserTicket.dao.findFirst("select * from purchase_user_ticket where ticket_type='01' and wx_id='"+share_id+"' and get_type='04' and card_category='08' ");
				setAttr("puTicket", puTicket);
			}else{
				PurchaseUserTicket checkpuTicket = PurchaseUserTicket.dao.findFirst("select * from purchase_user_ticket where ticket_type='01' and wx_id='"+from_user+"' and get_type='07' and card_category='08' ");
				setAttr("checkpuTicket", checkpuTicket);
				if(checkpuTicket==null){
					PurchaseShareTicket pst = PurchaseShareTicket.dao.findFirst("select count(*) as share_num from purchase_share_ticket where user_id='"+share_id+"' ");
					if(pst.getLong("share_num")<10){
						PurchaseShareTicket checkUser = PurchaseShareTicket.dao.findFirst("select * from purchase_share_ticket where user_id='"+share_id+"' and wx_id='"+from_user+"' ");
						if(checkUser==null){
							int money = 0;
							int rate0  = 5;
							int rate1  = 10;
							int rate2  = 25;
							int rate3  = 60;
							int tempInt  = RandomUtils.nextInt(1, 100+1);
							if (tempInt  > 0 && tempInt <= rate0){
								money = 100;
						    }else if (tempInt > rate0 && tempInt <= rate0+rate1) {
						    	money = 10;
						    }else if (tempInt > rate1 && tempInt <= rate0+rate1+rate2) {
						    	money = 5;
						    }else if(tempInt > rate2 && tempInt <= rate0+rate1+rate2+rate3) {
						    	money = 2;
						    } 
							PurchaseTicket pt = PurchaseTicket.dao.findFirst("select * from purchase_ticket where ticket_type='01' and card_category='08' and status='1' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) and mortgage_amount="+money+" ");
							PurchaseTicketRandom ptr = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_status='01' and ticket_id='"+pt.getStr("id")+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ORDER BY rand() limit 0,1 ");
							setAttr("ptr",ptr);
							if(ptr!=null){
								int result=CommonStoredProcedureService.trackresult(ptr.getStr("id"),from_user,user.getStr("id"),user.getStr("nick_name"),new BigDecimal("0"),"07","01");
								if(result>0){
									PurchaseShareTicket pstInsert = PurchaseShareTicket.dao
											.set("id", StringUtil.getUUID())
											.set("user_id", share_id)
											.set("wx_id", from_user)
											.set("create_by", user.get("id"))
											.set("create_name", user.get("nick_name"))
											.set("create_date", new Date())
											.set("update_by", user.get("id"))
											.set("update_name", user.get("nick_name"))
											.set("update_date", new Date())
											.set("remarks","获取朋友分享卡券");
									pstInsert.save();
								}else{
									render("error.html");
								}
							}else{
								setAttr("shareMsg", "活动已结束");
							}
						}else{
							PurchaseUserTicket userTicket = PurchaseUserTicket.dao.findFirst("select * from purchase_user_ticket where ticket_type='01' and wx_id='"+from_user+"' and get_type='04' and card_category='08' ");
							setAttr("userTicket", userTicket);
						}
					}else{
						setAttr("shareMsg", "卡券已领完");
					}
				}
					
			}
		}else{
			String url=getRequest().getRequestURI();
			System.out.println(url);
			setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
			render("error.html");
		}
		render("index_ticket_page.html");
		
	}
	//普通商品分页
	@Clear
   public void commodityListPage(){
	    String page=getPara("page");
	    if(StringUtil.isEmpty(page)){
		   page="1";
	    }
		Page <PurchaseCommodity> commodityPage=PurchaseCommodity.dao.paginate(Integer.parseInt(page),16,"select a.* "," from purchase_commodity a where a.commodity_category!='04' and a.commodity_type='01' and a.commodity_status='01' and (select count(*) from purchase_commodity_version where commodity_id=a.id and version_stock > 0 ) >0 order by a.sort");
		setAttr("commodityPage", commodityPage);
		setAttr("server_uri", PropKit.get("server_uri"));
		renderJson(commodityPage);
	}
   //普通商品明细
	
   public void commodityDetail(){
		String id=getPara("id"); //商品id
		String agent_num=getPara("agent_num"); //代理商分享code
		String agent_id="";
		if(!StringUtil.isEmpty(agent_num)){
			String[] agentNums=agent_num.split(",");
			agent_id=agentNums[0];
			if(agentNums.length>=2){
			if(StringUtil.isEmpty(id)){
				id=agentNums[1];
			}
			}
		}
		System.out.println(id);
		String from_user=getCookie("wx_openid");
		if(!StringUtil.isEmpty(from_user)){
		PurchaseCommodity checkCommodity=PurchaseCommodity.dao.findById(id);
		if(checkCommodity != null){
		OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id='"+from_user+"'");
		PurchaseCommodityCollect commodityCollect=PurchaseCommodityCollect.dao.findFirst("select count(*) as 'count' from purchase_commodity_collect where commodity_id='"+id+"' and user_id='"+user.get("id")+"'");
		Map map=new HashMap();
		setAttr("count", commodityCollect.getLong("count"));
		PurchaseCommodity commodity=PurchaseCommodity.dao.findById(id);
		List<PurchaseCommodityVersion> commodityVersionList=PurchaseCommodityVersion.dao.find("select * from purchase_commodity_version where commodity_id='"+id+"' and version_stock > 0 order by version_sort ");
		List<PurchaseVersionExt> versionExtList = new ArrayList<PurchaseVersionExt>();
		if(commodityVersionList.size()>0){
		   versionExtList=PurchaseVersionExt.dao.find("select * from purchase_version_ext where version_id='"+commodityVersionList.get(0).get("id")+"' order by ext_sort");
		   setAttr("version_stock", commodityVersionList.get(0).get("version_stock"));
		}
		Page<PurchaseCommodityContent> commodityContent=PurchaseCommodityContent.dao.paginate(1, 10, "select a.*", " from purchase_commodity_content a  where a.commodity_id='"+id+"'");
		List<PurchaseCommodityContent> commodityContentList=PurchaseCommodityContent.dao.find( "select a.* from purchase_commodity_content a  where a.commodity_id='"+id+"'");
		List<PurchaseCommodityCar> commodityCarlist = PurchaseCommodityCar.dao.find("select * from purchase_commodity_car where user_id = '"+user.getStr("id")+"' and order_type='01' and commodity_type = '01' order by create_date desc ");
		setAttr("commodity", commodity);
		setAttr("commodityVersionList", commodityVersionList);
		setAttr("commodityContentList", commodityContent.getList());
		setAttr("commodityContentTotalPage", commodityContent.getTotalPage());
		setAttr("commodityContentPageNumber", commodityContent.getPageNumber());
		setAttr("total_num", commodityContentList.size());
		setAttr("versionExtList", versionExtList);
		setAttr("car_size", commodityCarlist.size());
		setAttr("server_uri", PropKit.get("server_uri"));
		render("product_detail.html");
			}else{
				redirect("/home/homePage");
			}
		}else{
			String url=getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x")+1, url.length())+"?id="+id);
			render("error.html");
		}
		
	}
   
   //型号轮播图片切换
	@Clear
   public void changeVersionPic(){
		String id=getPara("id");
		String version_id=getPara("version_id");
		List<PurchaseVersionExt> versionExtList=PurchaseVersionExt.dao.find("select * from purchase_version_ext where version_id='"+version_id+"' order by ext_sort");
		Map map=new HashMap();
		map.put("versionExtList", versionExtList);
		map.put("server_uri", PropKit.get("server_uri"));
		renderJson(map);
	}
   //一元购商品分页
	@Clear
   public void onePurchaseListPage(){
	   String page=getPara("page");
	   if(StringUtil.isEmpty(page)){
		   page="1";
	   }
		Page <PurchaseCommodity> purchasePage=PurchaseCommodity.dao.paginate(Integer.parseInt(page),10,"select b.commodity_name,b.commodity_logo,a.total_count,a.surplus_count,a.commodity_id,a.id,(a.total_count-a.surplus_count)*100/a.total_count as 'per' ","from purchase_time_slot a left join purchase_commodity b on b.id=a.commodity_id  where b.commodity_type='02' and b.commodity_status='01' and a.is_over='0' and status ='01' and NOW() BETWEEN a.begin_date and a.end_date  order by a.sort, a.surplus_count ");
		setAttr("purchasePage", purchasePage);
		setAttr("server_uri", PropKit.get("server_uri"));
		renderJson(purchasePage);
	}
		 //是否收藏
	    @Clear
		public void isCollect(){
	    	String from_user=getCookie("wx_openid");
			String id = getPara("id");
			if(!StringUtil.isEmpty(from_user)){
			OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id='"+from_user+"'");
			PurchaseCommodityCollect commodityCollect=PurchaseCommodityCollect.dao.findFirst("select count(*) as 'count' from purchase_commodity_collect where commodity_id='"+id+"' and user_id='"+user.get("id")+"'");
			Map map=new HashMap();
			map.put("count", commodityCollect.getLong("count"));
			renderJson(map);
			}else{
				String url=getRequest().getRequestURI();
				setAttr("url", url.substring(url.indexOf("x")+1, url.length())+"?id="+id);
				render("error.html");
			}
		}
		
		 //收藏
		public void collectCommodity(){
			String from_user=getCookie("wx_openid");
			String id = getPara("id"); //商品id
			String commodity_type = getPara("commodity_type"); //商品类型
			if(!StringUtil.isEmpty(from_user)){
			OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id='"+from_user+"'");
			PurchaseCommodityCollect commodityCollect=PurchaseCommodityCollect.dao.findFirst("select id, count(*) as 'count' from purchase_commodity_collect where commodity_id='"+id+"' and user_id='"+user.get("id")+"'");
			Map map=new HashMap();
			if(commodityCollect.getLong("count") >0){
				PurchaseCommodityCollect.dao.deleteById(commodityCollect.get("id"));
				map.put("code", "1");
				map.put("msg", "成功取消收藏");
				
			}else{
				PurchaseCommodityCollect collect=new PurchaseCommodityCollect().set("id", StringUtil.getUUID())
						.set("commodity_id", id)
						.set("user_id", user.get("id"))
						.set("collect_date", new Date())
						.set("commodity_type", commodity_type)
						.set("create_by", user.get("id"))
						.set("create_name", user.get("nick_name"))
						.set("create_date", new Date())
						.set("update_by", user.get("id"))
						.set("update_name", user.get("nick_name"))
						.set("update_date", new Date())
						.set("remarks","收藏商品")
						;
				collect.save();
				map.put("code", "0");
				map.put("msg", "收藏成功");
			}
			
			renderJson(map);
			}else{
				String url=getRequest().getRequestURI();
				setAttr("url", url.substring(url.indexOf("x")+1, url.length())+"?id="+id);
				render("error.html");
			}
		}
		
		//浏览
				public void browseCommodity(){
					String id=getPara("id");
					String from_user=getCookie("wx_openid");
					String commodity_type=getPara("commodity_type");
					if(!StringUtil.isEmpty(from_user)){
					OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id='"+from_user+"'");
					PurchaseCommodityRecord checkRecord=PurchaseCommodityRecord.dao.findFirst("select * from purchase_commodity_record where user_id='"+user.get("id")+"' and commodity_id='"+id+"'");
					if(checkRecord == null){
					PurchaseCommodityRecord commodityRecord=new PurchaseCommodityRecord().set("id", StringUtil.getUUID())
							.set("commodity_id", id)
							.set("user_id", user.get("id"))
							.set("record_date", new Date())
							.set("commodity_type", commodity_type)
							.set("create_by", user.get("id"))
							.set("create_name", user.get("nick_name"))
							.set("create_date", new Date())
							.set("update_by", user.get("id"))
							.set("update_name", user.get("nick_name"))
							.set("update_date", new Date())
							.set("remarks", "商品浏览记录")
							;
					commodityRecord.save();
					}else{
						PurchaseCommodityRecord updateRecord=new PurchaseCommodityRecord().set("id", checkRecord.get("id"))
								.set("record_date", new Date())
								;
						updateRecord.update();
					}
					if(!StringUtil.isEmpty(user.getStr("agent_code"))){
					TradeMarketShare checkMarketShare=TradeMarketShare.dao.findFirst("select * from trade_market_share where agent_id='"
							+user.getStr("agent_code")+"' and commodity_id='"+id+"' ");
					if(checkMarketShare == null ){
						TradeMarketShare marketShare=TradeMarketShare.dao
								.set("id", StringUtil.getUUID())
								.set("agent_id", user.getStr("agent_code"))
								.set("commodity_id", id)
								.set("agent_code", user.getStr("agent_code"))
								.set("access_num", 0)
								.set("create_by", user.getStr("id"))
								.set("create_name", user.getStr("nick_name"))
								.set("create_date", new Date())
								.set("update_by", user.getStr("id"))
								.set("update_name", user.getStr("nick_name"))
								.set("update_date", new Date())
								.set("remarks", "浏览商品");
						marketShare.save();
					}else{
						Db.update("update trade_market_share set access_num=access_num+1 where id='"+checkMarketShare.getStr("id")+"'");
					}
					}
					renderNull();
				}else{
					String url=getRequest().getRequestURI();
					setAttr("url", url.substring(url.indexOf("x")+1, url.length())+"?id="+id);
					render("error.html");
				}
				}
				
				//评论分页
				@Clear
			   public void commodityContentPage(){
				   String page=getPara("page");
				   String commodity_id=getPara("commodity_id");
				   if(StringUtil.isEmpty(page)){
					   page="1";
				   }
				   Page<PurchaseCommodityContent> commodityContent=PurchaseCommodityContent.dao.paginate(Integer.parseInt(page), 10, "select a.*", " from purchase_commodity_content a  where a.commodity_id='"+commodity_id+"'");
				   setAttr("commodityPage", commodityContent);
					setAttr("server_uri", PropKit.get("server_uri"));
					renderJson(commodityContent);
				}
				//一元购介绍页
				@Clear
			   public void introduceOnePurchase(){
				   
				   render("rule.html");
				}
				//清除缓存
				@Clear
			   public void clearCookie(){
					setCookie("wx_openid", null, 0, "/");
					renderNull();
				}
				//分享回调处理
				@Clear
			   public void shareAdd(){
					String from_user=getCookie("wx_openid");
					Db.update("update purchase_users set is_share='02' where wx_id='"+from_user+"'");
					renderNull();
				}
				//微信卡劵处理
				@Clear
			   public void WXTicket(){
					String openid=getPara("openid");
					String card_id=getPara("card_id");
					OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id='"+openid+"'");
					if (user != null) {
					PurchaseTicket pt = PurchaseTicket.dao.findFirst("select * from purchase_ticket where card_id='"+card_id+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date)");
					if(pt!=null){
						PurchaseUserTicket checkTicketRandom=PurchaseUserTicket.dao.findFirst("select * from purchase_user_ticket where card_id='"+card_id+"' and wx_id = '"+openid+"'");
						if(checkTicketRandom == null ){
						PurchaseTicketRandom ptr = PurchaseTicketRandom.dao.findFirst("select * from purchase_ticket_random where ticket_status='01' and card_id='"+card_id+"' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ORDER BY rand() limit 0,1 ");
						if(ptr!=null){
							int result=StoredProcedureService.trackresult(ptr.getStr("id"),openid,user.getStr("id"),user.getStr("nick_name"),new BigDecimal("0"),"01",2,1,"02");
						  }
						}
					  }
					}
					redirect("/home/homePage");
				}
				//微信卡劵处理
				
			   public void forWardWXTicket(){
					String from_user=getCookie("wx_openid");
					if(from_user != null ){
						render("wx_ticket.html");
					}else{
						String url=getRequest().getRequestURI();
						setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
						render("error.html");
					}
				}
			   public void forwardtest(){
				   render("test.html");
			   }
		// 跳转到招行异兽卡活动页面
		public void forwardZhActivity() {
			String from_user=getCookie("wx_openid");
			if(!StringUtil.isEmpty(from_user)){
				OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select a.*,url_decode(a.nick_name) as 'nick_name_df' from purchase_users a where a.wx_id='"+from_user+"'");
				if (user != null) {
					if(user.getStr("vip_identify").equals("02")){
						redirect("/home/forwardCommodityPage");
					}else{
						render("checkvip.html");
					}
				}
			}else{
				String url=getRequest().getRequestURI();
				setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
				render("error.html");
			}
		}
		/*判断是否会员*/
		public void judgeVip(){
			Map map=new HashMap();
		    String from_user=getCookie("wx_openid");
		    String telephone = getPara("telephone");
			if(!StringUtil.isEmpty(from_user)){
				OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select a.*,url_decode(a.nick_name) as 'nick_name_df' from purchase_users a where a.wx_id='"+from_user+"'");
				if (user != null) {
				    PurchaseZhUser pzu = PurchaseZhUser.dao.findFirst("select * from purchase_zh_user where telephone ='"+telephone+"'");
				    if(pzu!=null){
				    	if(StringUtil.isEmpty(pzu.getStr("zh_card_no"))){
				    		map.put("code", -2);
					    	map.put("msg", "您的手机号还未注册，请至招行网点办理异兽卡");
				    	}else{
				    		OnePurchaseUser checkVip = OnePurchaseUser.dao.findFirst("select * from purchase_users where user_telephone ='"+telephone+"'");
				    		if(checkVip==null){
				    			Db.update("update purchase_users set vip_identify='02',user_telephone='"+telephone+"' where id = '"+user.getStr("id")+"' ");
				    			map.put("code", 0);
						    	map.put("msg", "验证成功");
				    		}else{
				    			map.put("code", -3);
						    	map.put("msg", "手机号已注册");
				    		}
				    	}
				    }else{
				    	map.put("code", -1);
				    	map.put("msg", "您的手机号还未注册，请至招行网点办理异兽卡");
				    }
					renderJson(map);
				}
			}else{
				String url=getRequest().getRequestURI();
				setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
				render("error.html");
			}
		}
		/*会员活动商品页面*/
		public void forwardCommodityPage(){
			Page <PurchaseCommodity> commodityPage=PurchaseCommodity.dao.paginate(1,4,"select * "," from purchase_commodity where commodity_category='04' and commodity_type='01' and commodity_status='01' order by sort");
			setAttr("commodityList", commodityPage.getList());
			setAttr("commodityTotalPage", commodityPage.getTotalPage());
			setAttr("commodityPageNumber", commodityPage.getPageNumber());
			List<PurchaseTicket> ptList = PurchaseTicket.dao.find("SELECT * FROM purchase_ticket WHERE card_category IN('02','03','04','05') and status='0' and ticket_type='01' order by sort");
			setAttr("ptList", ptList);
			setAttr("server_uri", PropKit.get("server_uri"));
			render("monstervip.html");
		}
		/*活动商品分页*/
		@Clear
	   public void activityCommodityPage(){
		    String page=getPara("page");
		    if(StringUtil.isEmpty(page)){
			   page="1";
		    }
			Page <PurchaseCommodity> commodityPage=PurchaseCommodity.dao.paginate(Integer.parseInt(page),4,"select * ","from purchase_commodity where commodity_category='04' and commodity_type='01' and commodity_status='01' order by sort");
			setAttr("commodityPage", commodityPage);
			setAttr("server_uri", PropKit.get("server_uri"));
			renderJson(commodityPage);
		}
		/*活动商品详细*/
		public void zhCommodityDetail(){
			String id=getPara("id"); //商品id
			String from_user=getCookie("wx_openid");
			if(!StringUtil.isEmpty(from_user)){
			PurchaseCommodity checkCommodity=PurchaseCommodity.dao.findById(id);
			if(checkCommodity != null){
			OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id='"+from_user+"'");
			PurchaseCommodityCollect commodityCollect=PurchaseCommodityCollect.dao.findFirst("select count(*) as 'count' from purchase_commodity_collect where commodity_id='"+id+"' and user_id='"+user.get("id")+"'");
			Map map=new HashMap();
			setAttr("count", commodityCollect.getLong("count"));
			PurchaseCommodity commodity=PurchaseCommodity.dao.findById(id);
			List<PurchaseCommodityVersion> commodityVersionList=PurchaseCommodityVersion.dao.find("select * from purchase_commodity_version where commodity_id='"+id+"' order by version_sort ");
			List<PurchaseVersionExt> versionExtList = new ArrayList<PurchaseVersionExt>();
			if(commodityVersionList.size()>0){
			   versionExtList=PurchaseVersionExt.dao.find("select * from purchase_version_ext where version_id='"+commodityVersionList.get(0).get("id")+"' order by ext_sort");
			   setAttr("version_stock", commodityVersionList.get(0).get("version_stock"));
			}
			Page<PurchaseCommodityContent> commodityContent=PurchaseCommodityContent.dao.paginate(1, 10, "select a.*", " from purchase_commodity_content a  where a.commodity_id='"+id+"'");
			List<PurchaseCommodityContent> commodityContentList=PurchaseCommodityContent.dao.find( "select a.* from purchase_commodity_content a  where a.commodity_id='"+id+"'");
			List<PurchaseCommodityCar> commodityCarlist = PurchaseCommodityCar.dao.find("select * from purchase_commodity_car where user_id = '"+user.getStr("id")+"' and order_type='01' and commodity_type = '01' order by create_date desc ");
			setAttr("commodity", commodity);
			setAttr("commodityVersionList", commodityVersionList);
			setAttr("commodityContentList", commodityContent.getList());
			setAttr("commodityContentTotalPage", commodityContent.getTotalPage());
			setAttr("commodityContentPageNumber", commodityContent.getPageNumber());
			setAttr("total_num", commodityContentList.size());
			setAttr("versionExtList", versionExtList);
			setAttr("car_size", commodityCarlist.size());
			setAttr("server_uri", PropKit.get("server_uri"));
			render("product_detail.html");
				}else{
					redirect("/home/homePage");
				}
			}else{
				String url=getRequest().getRequestURI();
				setAttr("url", url.substring(url.indexOf("x")+1, url.length())+"?id="+id);
				render("error.html");
			}
			
		}
		/*领取卡券*/
		public void receiveTicket() {
			Map map=new HashMap();
			String from_user = getCookie("wx_openid");
			String ticket_id = getPara("ticket_id");
			String receive_num = getPara("receive_num");
			int index = Integer.parseInt(receive_num);
			if(!StringUtil.isEmpty(from_user)){
				OnePurchaseUser user=OnePurchaseUser.dao.findFirst("select a.*,url_decode(a.nick_name) as 'nick_name_df' from purchase_users a where a.wx_id='"+from_user+"'");
				if (user != null) {
					if(user.getStr("vip_identify").equals("02")){
						PurchaseUserTicket puTicket = PurchaseUserTicket.dao.findFirst("select * from purchase_user_ticket where wx_id='"+from_user+"' and ticket_id='"+ticket_id+"' ");
						if(puTicket==null){
							PurchaseTicket pt = PurchaseTicket.dao.findFirst("select * from purchase_ticket where id='"+ticket_id+"' AND ticket_type='01' AND TO_DAYS(NOW()) BETWEEN TO_DAYS(begin_date) AND TO_DAYS(end_date) ");
							if(pt!=null){
								for(int i=0;i<index;i++){
									String code_prefix = pt.getStr("code_prefix");
									String timestamp = DateUtil.getTimeStr();
									String card_code = code_prefix + timestamp + i + StringUtil.getRoundNum(2)+"";
									PurchaseUserTicket put =PurchaseUserTicket.dao
											.set("id", StringUtil.getUUID())
											.set("ticket_id", ticket_id).set("brand_name", pt.getStr("brand_name"))
											.set("ticket_period", pt.getStr("ticket_period")).set("ticket_logo", pt.getStr("ticket_logo"))
											.set("ticket_title", pt.getStr("ticket_title")).set("ticket_sub_title", pt.getStr("ticket_sub_title"))
											.set("ticket_desc", pt.getStr("ticket_desc")).set("minimum_threshold_amount", pt.getBigDecimal("minimum_threshold_amount"))
											.set("mortgage_amount", pt.getBigDecimal("mortgage_amount")).set("begin_date", DateUtil.formatDate(new Date(), "yyyy-MM-dd"))
											.set("end_date", pt.getDate("end_date")).set("ticket_type", pt.getStr("ticket_type"))
											.set("code_type", pt.getStr("code_type")).set("card_type", pt.getStr("card_type"))
											.set("ticket_color", pt.getStr("ticket_color")).set("ticket_notice", pt.getStr("ticket_notice"))
											.set("fixed_term", pt.getInt("fixed_term")).set("fixed_begin_term", pt.getInt("fixed_begin_term"))
											.set("ticket_status", "02").set("card_id", pt.getStr("card_id"))
											.set("wx_id", from_user).set("card_code", card_code)
											.set("get_date", new Date()).set("get_type", "08")
											.set("card_category", pt.getStr("card_category")).set("create_by", user.getStr("id"))
											.set("create_name", user.getStr("nick_name")).set("create_date", new Date())
											.set("update_by", user.getStr("id")).set("update_name", user.getStr("nick_name"))
											.set("update_date", new Date()).set("remarks", "异兽卡活动");
									put.save();
								}
								map.put("code", 0);
								map.put("msg", "领取成功");
							}else{
								map.put("code", -3);
								map.put("msg", "活动已结束");
							}
						}else{
							map.put("code", -2);
							map.put("msg", "已领取过卡券");
						}
					}else{
						map.put("code", -1);
						map.put("msg", "请先成为会员");
					}
					renderJson(map);
				}
			}else{
				String url=getRequest().getRequestURI();
				setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
				render("error.html");
			}
			
		}
		/*跳转到壁纸下载页面*/
		public void forwardBiZhi(){
			render("wallpaper.html");
		}
		/*跳转到表情包下载页面*/
		public void forwardBiaoQingBao(){
			render("emoji.html");
		}
}
