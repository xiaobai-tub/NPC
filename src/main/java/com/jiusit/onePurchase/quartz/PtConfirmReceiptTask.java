package com.jiusit.onePurchase.quartz;

import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jiusit.common.utils.DateUtil;
import com.jiusit.onePurchase.model.PurchaseCommodityBuyTotal;
/*
 * 确认收货
 * */
public class PtConfirmReceiptTask implements Job  {
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		List<PurchaseCommodityBuyTotal> commodityTotalList=PurchaseCommodityBuyTotal.dao.find("select * from purchase_commodity_buy_total where order_status='03' and TO_DAYS(NOW()) - TO_DAYS(deliver_date) >="+PropKit.get("confirm_time")+"");
		if(commodityTotalList.size()>0){
			for(PurchaseCommodityBuyTotal commodityTotal : commodityTotalList){
				PurchaseCommodityBuyTotal updateCommodityTtoal=PurchaseCommodityBuyTotal.dao.set("id", commodityTotal.get("id"))
						.set("order_status", "08")
						.set("receipt_date", new Date())
						.set("order_remark", "定时任务确认收货");
				updateCommodityTtoal.update();
				Db.update("update purchase_commodity_buy set order_status='08' where total_id='"+commodityTotal.get("id")+"'");
			}
		}
	}
}
