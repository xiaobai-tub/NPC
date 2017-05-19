package com.jiusit.onePurchase.controller;

import java.util.ArrayList;
import java.util.Date;
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
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.jfinal.ApiController;
import com.jiusit.common.utils.DateUtil;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.interceptor.UserInterceptor;
import com.jiusit.onePurchase.model.OnePurchaseUser;
import com.jiusit.onePurchase.model.PurchaseCommodity;
import com.jiusit.onePurchase.model.PurchaseCommodityVersion;
import com.jiusit.onePurchase.model.PurchaseOnepurchaseBuy;
import com.jiusit.onePurchase.model.PurchaseOnepurchaseBuyTotal;
import com.jiusit.onePurchase.model.PurchaseSentAddress;
import com.jiusit.onePurchase.model.PurchaseTimeSlot;
import com.jiusit.onePurchase.model.PurchaseVersionExt;
@ControllerBind(controllerKey="/one",viewPath="/pages")
@Before(UserInterceptor.class)
/*
 * 一元购商品
 * */
public class OnePurchaseController extends ApiController{
	private static final Logger log = Logger
			.getLogger(OnePurchaseController.class);

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


	//一元购商品详细
	public void getOnePurchase(){
		String id = getPara("id");
		String from_user=getCookie("wx_openid");
		if(!StringUtil.isEmpty(from_user)){
		PurchaseTimeSlot slot = PurchaseTimeSlot.dao.findFirst("select c.*,s.time_slot,s.status,TIMESTAMPDIFF(SECOND,now(),s.lottery_time) as 'lottery_second',s.lucky_num,s.version_id,s.commodity_id,s.begin_date from purchase_time_slot s left join purchase_commodity c on s.commodity_id = c.id where s.id = '"+id+"'");
		
		PurchaseCommodity purchase=PurchaseCommodity.dao.findFirst("select b.commodity_name,b.commodity_logo,a.total_count,a.surplus_count,a.commodity_id,a.id,(a.total_count-a.surplus_count)*100/a.total_count as 'per' from purchase_time_slot a left join purchase_commodity b on b.id=a.commodity_id  where b.commodity_type='02' and b.commodity_status='01'  and a.id = '"+id+"' order by b.sort,a.sort,surplus_count");
		//查询商品型号表
		List<PurchaseCommodityVersion> commodityVersionList=PurchaseCommodityVersion.dao.find("select * from purchase_commodity_version where commodity_id='"+slot.get("commodity_id")+"' and version_stock > 0 order by version_sort ");
		List<PurchaseVersionExt> versionExtList = new ArrayList<PurchaseVersionExt>();
		if(commodityVersionList.size()>0){
		   versionExtList=PurchaseVersionExt.dao.find("select * from purchase_version_ext where version_id='"+commodityVersionList.get(0).get("id")+"' order by ext_sort");
		}
		//是否参与
		OnePurchaseUser opUser = OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id = '"+from_user+"'");
		List<PurchaseOnepurchaseBuy> onePurchaseList =new ArrayList<PurchaseOnepurchaseBuy>();
		
		if(null != opUser){
			onePurchaseList  = PurchaseOnepurchaseBuy.dao.find("select * from purchase_onepurchase_buy  where time_slot = '"+id+"'  and user_id='"+opUser.get("id")+"' and pay_status = '02'");
		}
		PurchaseOnepurchaseBuy lunckNum= PurchaseOnepurchaseBuy.dao.findFirst("select GROUP_CONCAT(lucky_num) as 'lucky_num' from purchase_onepurchase_buy  where time_slot = '"+id+"'  and user_id='"+opUser.get("id")+"' and pay_status = '02'");
		//参与记录AND b.`user_id` != '"+opUser.get("id")+"'
		List<OnePurchaseUser> partakeUser = OnePurchaseUser.dao.find("select * ,count(c.user_id) as 'countNum' from (select a.*,b.nick_name,b.head_path from purchase_onepurchase_buy a LEFT JOIN purchase_users b on b.id=a.user_id  where a.time_slot='"+id+"' and a.pay_status='02' ORDER BY a.buy_date desc ) as c  GROUP BY c.user_id order by buy_date desc");
		//已中奖公布结果
		PurchaseTimeSlot winner = PurchaseTimeSlot.dao.findFirst("select s.lottery_time,(SELECT COUNT(*) FROM purchase_onepurchase_buy WHERE user_id=b.user_id AND time_slot=b.time_slot) as countNum,u.nick_name,u.head_path from purchase_time_slot s left join purchase_onepurchase_buy b on s.id=b.time_slot left join purchase_users u on b.user_id=u.id  where s.id='"+id+"' and s.status='03' and b.is_win='02' and b.pay_status = '02' ");
		setAttr("winner",winner);
		setAttr("id", slot.get("id"));
		setAttr("slotId",id);
		setAttr("commodityVersionList",commodityVersionList);
		setAttr("version_id", slot.get("version_id"));
		setAttr("commodity_name", slot .get("commodity_name"));
		setAttr("commodity_title", slot.get("commodity_title"));
		setAttr("time_slot", slot.get("time_slot"));
		setAttr("versionExtList", versionExtList);
		setAttr("commodity_desc", slot.get("commodity_desc"));
		setAttr("slot", slot);
		setAttr("purchase", purchase);
		setAttr("onePurchaseList",onePurchaseList);
		setAttr("lunckNum",lunckNum);
		setAttr("partakeUser",partakeUser);
		setAttr("begin_date",slot.get("begin_date"));
		setAttr("server_uri", PropKit.get("server_uri"));
		render("one_detail.html");
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
		//一元购订单详情
		public void oneOrderDetails(){
			String from_user=getCookie("wx_openid");
			String order_status = getPara("order_status");
			String type = getPara("type");
			String page = getPara("page");
			
			if(StringUtil.isEmpty(page)){
				page="1";
			}
			if(!StringUtil.isEmpty(from_user)){
			OnePurchaseUser opUser = OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id = '"+from_user+"'");
			String sql=" from purchase_onepurchase_buy_total t left join wb_key k on t.order_status = k.key_name  where k.key_type = 'purchase_commodity_buy.order_status' and t.user_id ='"+opUser.get("id")+"'";
			if(StringUtil.isEqual(order_status, "02")){
				sql+=" and t.is_add_addr='02'";
			}
			if(!StringUtil.isEmpty(order_status)){
				sql+=" and t.order_status='"+order_status+"' order by t.create_date desc";
			}else{
				sql+=" order by t.order_status asc,t.create_date desc";
			}
			
			Page<PurchaseOnepurchaseBuyTotal> onepurchaseOrder = PurchaseOnepurchaseBuyTotal.dao.paginate(Integer.parseInt(page), 10,"select t.*,k.key_value ", sql);
			
			if(StringUtil.isEqual(type, "1")){
				List<PurchaseOnepurchaseBuyTotal> allStandByPay = PurchaseOnepurchaseBuyTotal.dao.find("select * from purchase_onepurchase_buy_total where user_id ='"+opUser.get("id")+"' and order_status = '01'");
				List<PurchaseOnepurchaseBuyTotal> allDelivery = PurchaseOnepurchaseBuyTotal.dao.find("select * from purchase_onepurchase_buy_total where user_id ='"+opUser.get("id")+"' and order_status = '02' and is_add_addr = '02' ");
				List<PurchaseOnepurchaseBuyTotal> allStandByCollect = PurchaseOnepurchaseBuyTotal.dao.find("select * from purchase_onepurchase_buy_total where user_id ='"+opUser.get("id")+"' and order_status = '03'");
				List<PurchaseOnepurchaseBuyTotal> allUnveileCollect = PurchaseOnepurchaseBuyTotal.dao.find("select * from purchase_onepurchase_buy_total where user_id ='"+opUser.get("id")+"' and order_status = '09'");
				setAttr("oneRecord",onepurchaseOrder.getList());
				setAttr("pageNumber", onepurchaseOrder.getPageNumber());
				setAttr("totalPage", onepurchaseOrder.getTotalPage());
				setAttr("order_status", order_status);
				setAttr("allStandByPay",allStandByPay.size());
				setAttr("allDelivery",allDelivery.size());
				setAttr("allUnveile",allUnveileCollect.size());
				setAttr("allStandByCollect",allStandByCollect.size());
				render("one_record.html");
			}else{
				renderJson(onepurchaseOrder);
			}
			}else{
				String url=getRequest().getRequestURI();
				setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
				render("error.html");
			}
		}
		 //一元购订单详情
	   public void onePurchaseOrderDetail(){
		   String from_user=getCookie("wx_openid");
		   String total_id = getPara("total_id");
		   if(!StringUtil.isEmpty(from_user)){
		   List<PurchaseOnepurchaseBuy> pobList = PurchaseOnepurchaseBuy.dao.find("select a.*,b.time_slot,b.status from purchase_onepurchase_buy a left join purchase_time_slot b on a.time_slot=b.id where a.total_id = '"+total_id+"' order by a.create_date desc ");
		   setAttr("pobList",pobList);
		   setAttr("server_uri", PropKit.get("server_uri"));
		   render("one_order_detail.html");
	       }else{
	    	   String url=getRequest().getRequestURI();
	    		setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
	    		render("error.html");
		   }
		}
		public void getOnePurchaseFormula(){
			String from_user=getCookie("wx_openid");
		    String id = getPara("id");
		    String currenDate=DateUtil.formatDate(new Date(), "yyyyMMdd");
		    if(!StringUtil.isEmpty(from_user)){
			PurchaseOnepurchaseBuy onepurchaseBuy = PurchaseOnepurchaseBuy.dao.findFirst("SELECT SUM(timeNum) as 'total_sum' FROM `purchase_onepurchase_buy` where time_slot = '"+id+"' and pay_status='02' LIMIT 0,20");
			PurchaseTimeSlot timeSlot=PurchaseTimeSlot.dao.findById(id);
		    List<PurchaseOnepurchaseBuy> onepurchaseBuyList = PurchaseOnepurchaseBuy.dao.find("SELECT a.*,b.nick_name FROM `purchase_onepurchase_buy` a left join purchase_users b on b.id=a.user_id where a.time_slot = '"+id+"' and a.pay_status='02' order by  a.indiana_time desc LIMIT 0,20");
		    setAttr("total_time_sum",onepurchaseBuy.get("total_sum"));
		    setAttr("onepurchaseBuyList",onepurchaseBuyList);
		    setAttr("timeSlot",timeSlot);
		    setAttr("currenDate",currenDate);
		    render("formula.html");
		    }else{
		    	String url=getRequest().getRequestURI();
		    	setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
		    	render("error.html");
			}
		}
		//一元购收货地址
		public void oneAddAddress(){
			String from_user=getCookie("wx_openid");
			String address_id = getPara("address_id");
			String total_id = getPara("total_id");
			Map ret = new HashMap<>();
			if(!StringUtil.isEmpty(from_user)){
			PurchaseSentAddress psa = PurchaseSentAddress.dao.findFirst("select a.*,b.area_name as 'province_name',c.area_name as 'city_name',d.area_name as 'country_name' from purchase_sent_address a left join sys_areas b on a.province_code=b.area_code left join sys_areas c on a.city_code=c.area_code left join sys_areas d on a.county_code=d.area_code where a.id='"+address_id+"'");
			String add_str=psa.getStr("province_name")+psa.getStr("city_name")+psa.getStr("country_name")+psa.getStr("geography_location");
			int i = Db.update("update purchase_onepurchase_buy_total set is_add_addr='02',order_status='02',send_address_id='"+address_id+"',send_address='"+add_str+"',receiver_name='"+psa.getStr("receiver_name")+"',receiver_telephone='"+psa.getStr("telephone")+"' where id='"+total_id+"' ");
			int j = Db.update("update purchase_onepurchase_buy set order_status='02' where total_id='"+total_id+"' ");
			if(i>0 && j>0){
				ret.put("code", "0");
				ret.put("msg", "修改成功");
			}else{
				ret.put("code", "0");
				ret.put("msg", "填写收货地址不成功");
			}
			renderJson(ret);
		}else{
			String url=getRequest().getRequestURI();
			setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
			render("error.html");
		}
		}
		public void confirmReceipt(){
			String from_user=getCookie("wx_openid");
			String total_id = getPara("total_id");
			String is_win = getPara("is_win");
			String status = getPara("status");
			String type = getPara("type");
			String page = getPara("page");
			String check_type = "";
			
			if(StringUtil.isEmpty(page)){
				page="1";
			}
			if(!StringUtil.isEmpty(from_user)){
			OnePurchaseUser opUser = OnePurchaseUser.dao.findFirst("select * from purchase_users where wx_id = '"+from_user+"'");
			if(opUser!=null){
				setAttr("opUser", opUser);
			}
			Db.update("update purchase_onepurchase_buy_total set order_status='04',receipt_date=now(),order_remark='手动收货' where id='"+total_id+"' ");
			Db.update("update purchase_onepurchase_buy set order_status='04' where total_id='"+total_id+"' ");
			String sql ="from (select m.* from (select a.*,b.nick_name,c.time_slot as 'time_slot_num',c.lucky_num as 'win_num',c.total_count,c.surplus_count,c.status,d.is_add_addr,d.send_address,d.receiver_name,d.receiver_telephone,d.order_status as 'total_order_status',d.express_company,d.express_no FROM `purchase_onepurchase_buy` a LEFT JOIN purchase_users b on b.id=a.user_id LEFT JOIN purchase_time_slot c on c.id=a.time_slot LEFT JOIN purchase_onepurchase_buy_total d on a.total_id=d.id where a.user_id='"+opUser.get("id")+"' and a.pay_status='02' ";
			if(StringUtil.isEqual(is_win,"")&&StringUtil.isEqual(status,"")){
				check_type="";
			}else if(StringUtil.isEqual(is_win,"02")&&StringUtil.isEqual(status,"")){
				sql+=" and a.is_win='02'";
				check_type="01";
			}else if(StringUtil.isEqual(is_win,"")&&StringUtil.isEqual(status,"01")){
				sql+=" and c.status='01' and a.order_status='09'";
				check_type="02";
			}else if(StringUtil.isEqual(is_win,"")&&StringUtil.isEqual(status,"03")){
				sql+=" and c.status='03'";
				check_type="03";
			}else if(StringUtil.isEqual(is_win,"")&&StringUtil.isEqual(status,"02")){
				sql+=" and c.status='02'";
				check_type="04";
			}
			sql+="order by a.is_win desc,d.is_add_addr desc) m GROUP BY m.time_slot,m.user_id )  t ORDER BY t.buy_date DESC ";
			Page<PurchaseOnepurchaseBuy> oneBuy = PurchaseOnepurchaseBuy.dao.paginate(Integer.parseInt(page), 10, "SELECT t.*,(select GROUP_CONCAT(lucky_num)  from purchase_onepurchase_buy where user_id=t.user_id and time_slot=t.time_slot and pay_status='02') as 'luckyNum',(select count(*)  from purchase_onepurchase_buy where user_id=t.user_id and time_slot=t.time_slot and pay_status='02' ) as 'countPart',(select GROUP_CONCAT(distinct version_type)  from purchase_onepurchase_buy where user_id=t.user_id and time_slot=t.time_slot and pay_status='02') as 'versionType'",sql);
			if(StringUtil.isEqual(type, "1")){
				List<PurchaseOnepurchaseBuy> indianaRecord = PurchaseOnepurchaseBuy.dao.find("select b.*,u.nick_name from purchase_onepurchase_buy b left join purchase_users u on u.id = b.user_id where b.user_id = '"+opUser.get("id")+"' and b.pay_status='02' GROUP BY time_slot ");
				List<PurchaseOnepurchaseBuy> rewardRecord = PurchaseOnepurchaseBuy.dao.find("select b.*,u.nick_name from purchase_onepurchase_buy b left join purchase_users u on u.id = b.user_id where b.is_win='02' and b.user_id = '"+opUser.get("id")+"' and b.pay_status='02' GROUP BY time_slot");
				List<PurchaseTimeSlot> Conduct = PurchaseTimeSlot.dao.find("SELECT a.*,b.nick_name,c.total_count,c.surplus_count FROM purchase_onepurchase_buy a LEFT JOIN purchase_users b on b.id=a.user_id LEFT JOIN purchase_time_slot c on c.id=a.time_slot where c.status='01' and a.order_status='09' and a.user_id='"+opUser.get("id")+"' and a.pay_status='02' GROUP BY a.time_slot");
				List<PurchaseTimeSlot> Announced = PurchaseTimeSlot.dao.find("SELECT a.*,b.nick_name,c.total_count,c.surplus_count FROM purchase_onepurchase_buy a LEFT JOIN purchase_users b on b.id=a.user_id LEFT JOIN purchase_time_slot c on c.id=a.time_slot where c.status='03' and a.user_id='"+opUser.get("id")+"' and a.pay_status='02' GROUP BY a.time_slot");
				List<PurchaseTimeSlot> noAnnounced = PurchaseTimeSlot.dao.find("SELECT a.*,b.nick_name,c.total_count,c.surplus_count FROM purchase_onepurchase_buy a LEFT JOIN purchase_users b on b.id=a.user_id LEFT JOIN purchase_time_slot c on c.id=a.time_slot where c.status='02' and a.user_id='"+opUser.get("id")+"' and a.pay_status='02' GROUP BY a.time_slot");
				setAttr("oneBuyList",oneBuy.getList());
				setAttr("pageNumber", oneBuy.getPageNumber());
				setAttr("totalPage", oneBuy.getTotalPage());
				setAttr("indianaRecord",indianaRecord.size());
				setAttr("rewardRecord",rewardRecord.size());
				setAttr("Conduct",Conduct.size());
				setAttr("Announced",Announced.size());
				setAttr("noAnnounced",noAnnounced.size());
				setAttr("server_uri", PropKit.get("server_uri"));
				setAttr("from_user",from_user);
				setAttr("check_type",check_type);
				setAttr("is_win",is_win);
				setAttr("status",status);
				render("one_buy.html");
			}else{
				Map<Object,Object> map=new HashMap<Object,Object>();
				map.put("oneBuyList", oneBuy.getList());
				map.put("pageNumber", oneBuy.getPageNumber());
				map.put("totalPage", oneBuy.getTotalPage());
				map.put("check_type", check_type);
				setAttr("from_user",from_user);
				renderJson(oneBuy);
			}
			}else{
				String url=getRequest().getRequestURI();
				setAttr("url", url.substring(url.indexOf("x")+1, url.length()));
				render("error.html");
			}
		}
		
}
