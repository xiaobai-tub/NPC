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
 * 默认评价
 * */
public class CommentCommodityTask implements Job  {
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		List<PurchaseCommodityBuyTotal> commodityTotalList=PurchaseCommodityBuyTotal.dao.find("select * from purchase_commodity_buy_total where order_status='08' and TO_DAYS(NOW()) - TO_DAYS(receipt_date) >="+PropKit.get("comment_time")+"");
		if(commodityTotalList.size()>0){
			for(PurchaseCommodityBuyTotal commodityTotal : commodityTotalList){
				PurchaseCommodityBuyTotal updateCommodityTtoal=PurchaseCommodityBuyTotal.dao.set("id", commodityTotal.get("id"))
						.set("order_status", "04");
				updateCommodityTtoal.update();
				Db.update("update purchase_commodity_buy set order_status='04' where total_id='"+commodityTotal.get("id")+"'");
			}
		}
	}
}
