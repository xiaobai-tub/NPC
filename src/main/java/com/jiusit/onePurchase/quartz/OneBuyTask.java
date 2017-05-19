package com.jiusit.onePurchase.quartz;

import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jiusit.common.utils.DateUtil;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.model.PurchaseTimeSlot;
public class OneBuyTask implements Job{

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		String now_date = DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
		String sql="select * from purchase_onepurchase_buy where pay_status='01' and (UNIX_TIMESTAMP('"+now_date+"')-UNIX_TIMESTAMP(buy_date))/60>="+PropKit.get("pay_time")+" and order_status='01' ";
		List<Record> list = Db.find(sql);
		if(list.size()>0){
			for(Record r :list){
				Db.update("delete from  purchase_onepurchase_buy  where total_id = '"+r.getStr("total_id")+"'");
				Db.update("delete from  purchase_onepurchase_buy_total  where id = '"+r.getStr("total_id")+"'");
				}
		}
	}

}
