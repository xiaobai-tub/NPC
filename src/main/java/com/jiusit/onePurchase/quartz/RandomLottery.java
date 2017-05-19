package com.jiusit.onePurchase.quartz;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jfinal.plugin.activerecord.Db;
import com.jiusit.common.utils.DateUtil;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.model.PurchaseCommodityVersion;
import com.jiusit.onePurchase.model.PurchaseLottery;
import com.jiusit.onePurchase.model.PurchaseOnepurchaseBuy;
import com.jiusit.onePurchase.model.PurchaseTimeSlot;
public class RandomLottery implements Job{

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		List<PurchaseTimeSlot> ptsList = PurchaseTimeSlot.dao.find("select * from purchase_time_slot where status='02' ");
		if(ptsList.size()>0){
		for(PurchaseTimeSlot pts : ptsList){
			if(pts.getDate("lottery_time").getTime()<= new Date().getTime()){
			PurchaseOnepurchaseBuy onepurchaseBuy = PurchaseOnepurchaseBuy.dao.findFirst("SELECT SUM(timeNum) as 'total_sum' FROM `purchase_onepurchase_buy` where time_slot = '"+pts.getStr("id")+"' and pay_status='02' LIMIT 0,20");
			PurchaseLottery lottery=PurchaseLottery.dao.findFirst("SELECT * FROM `purchase_lottery` where lottery_numbers <> ''  ORDER BY create_date DESC");
			BigDecimal total_sum=new BigDecimal(onepurchaseBuy.get("total_sum").toString());
			BigDecimal winning_number=new BigDecimal(lottery.getInt("winning_number"));
			BigDecimal lunck_num=total_sum.add(winning_number);
			BigDecimal total_count=new BigDecimal(pts.getInt("total_count"));
			lunck_num=lunck_num.divideAndRemainder(total_count)[1];
			System.out.println(lunck_num.toString());
			lunck_num=lunck_num.add(new BigDecimal(10000001));
			System.out.println(lunck_num.toString());
			String period=lottery.get("periods")+""+lottery.get("issue");
			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			String time_slot=df.format(new Date());
			Date begin_date=new Date();
			Date end_date = DateUtil.dateAdd(2, 1,
					begin_date);
		        PurchaseOnepurchaseBuy oneBuy=PurchaseOnepurchaseBuy.dao.findFirst("select * from purchase_onepurchase_buy where lucky_num='"+lunck_num.toString()+"' and time_slot='"+pts.getStr("id")+"'");
			    Db.update("update purchase_onepurchase_buy set is_win = '02',order_status='02' where lucky_num='"+lunck_num.toString()+"' and time_slot='"+pts.getStr("id")+"'");
				Db.update("update purchase_onepurchase_buy_total set order_status='02' where id ='"+oneBuy.getStr("total_id")+"'");
			    Db.update("update purchase_time_slot set status = '03',lottery_num='"+lottery.get("lottery_numbers")+"',lucky_num='"+lunck_num.toString()+"',period='"+period+"',caipiao_period='"+lottery.getStr("periods")+"' where id='"+pts.getStr("id")+"' ");
				Db.update("update purchase_onepurchase_buy_total set order_status='04' where id <> '"+oneBuy.getStr("total_id")+"' and pay_order_no in (select distinct pay_order_no from purchase_onepurchase_buy where time_slot='"+pts.getStr("id")+"' and is_win='01' and pay_status='02' and pay_order_no <> '"+oneBuy.getStr("pay_order_no")+"') and pay_status='02' ");
			    Db.update("update purchase_onepurchase_buy set order_status = '04' where time_slot='"+pts.getStr("id")+"' and is_win='01' and pay_status='02'");
			    Db.update("update purchase_commodity_version set version_stock=version_stock-1 where id = '"+oneBuy.getStr("version_id")+"'");
			    PurchaseCommodityVersion pcv=PurchaseCommodityVersion.dao.findFirst("select sum(version_stock) as 'total_sum' from purchase_commodity_version where commodity_id='"+oneBuy.getStr("commodity_id")+"'");
			    if(!StringUtil.isEqual(pcv.getBigDecimal("total_sum").toString(), "0")){
			    	String id=StringUtil.getUUID();
				    PurchaseTimeSlot timeSlot=PurchaseTimeSlot.dao.set("id", id)
				    		.set("commodity_id", pts.get("commodity_id"))
				    		.set("version_id", pts.get("version_id"))
				    		.set("begin_date", begin_date)
				    		.set("end_date", end_date)
				    		.set("total_count", pts.get("total_count"))
				    		.set("surplus_count", pts.get("total_count"))
				    		.set("is_over", "0")
				    		.set("time_slot", time_slot)
				    		.set("status", "01")
				    		.set("sort", pts.get("sort"))
				    		.set("create_by", pts.get("create_by"))
				    		.set("create_name", pts.get("create_name"))
				    		.set("create_date", pts.get("create_date"))
				    		.set("update_by", pts.get("update_by"))
				    		.set("update_name", pts.get("update_name"))
				    		.set("update_date", pts.get("update_date"))
				    		.set("skip_url", "one/getOnePurchase?id="+id)
						    .set("remarks", "定时任务添加");
				    timeSlot.save();
				    for(int i=1;i<=pts.getInt("total_count");i++){
				    	String randomId=StringUtil.getUUID();
				    	String insertSql="insert into purchase_random_num values('"+randomId+"','"+id+"','"+i+"')";
				    	Db.update(insertSql);
				    }
				    
			    }
			    
			}
		
	 }
	}
	}
}
