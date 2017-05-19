package com.jiusit.onePurchase.quartz;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jfinal.plugin.activerecord.Db;
import com.jiusit.common.utils.DateUtil;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.model.PurchaseCommodityVersion;
import com.jiusit.onePurchase.model.PurchaseTimeSlot;
public class TimedTask implements Job{

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		List<PurchaseTimeSlot> ptsList = PurchaseTimeSlot.dao.find("select * from purchase_time_slot where end_date<=Now() and is_over='0' ");
		if(ptsList.size()>0){
			for(PurchaseTimeSlot pts : ptsList){
				DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				String time_slot=df.format(new Date());
				Date begin_date=new Date();
				Date end_date = DateUtil.dateAdd(2, 1,
						begin_date);
				PurchaseCommodityVersion pcv=PurchaseCommodityVersion.dao.findFirst("select sum(version_stock) as 'total_sum' from purchase_commodity_version where commodity_id='"+pts.getStr("commodity_id")+"'");
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
			    String updateStatus="update purchase_time_slot set is_over='1' , status='04'  where id='"+pts.get("id")+"'";
				Db.update(updateStatus);
				String deleteRandom="delete from purchase_random_num where time_solt_id='"+pts.get("id")+"' ";
				Db.update(deleteRandom);
			}
		}
		
	}
}
