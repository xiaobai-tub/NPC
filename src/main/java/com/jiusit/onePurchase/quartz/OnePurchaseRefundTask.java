package com.jiusit.onePurchase.quartz;

import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jfinal.plugin.activerecord.Db;
import com.jiusit.common.utils.DateUtil;
import com.jiusit.common.utils.StringUtil;
import com.jiusit.onePurchase.model.OnePurchaseUser;
import com.jiusit.onePurchase.model.PurchaseCommodityPay;
import com.jiusit.onePurchase.model.PurchaseOnepurchaseBuy;
import com.jiusit.onePurchase.model.PurchaseOnepurchaseBuyTotal;
import com.jiusit.onePurchase.model.PurchaseTimeSlot;

public class OnePurchaseRefundTask implements Job {
	/*
	 * 购买一元购后，购买次数没有达到，要把钱退换给用户账户上
	 */
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		String selSql = "select * from purchase_time_slot where is_over='1' and status='04' ";
		List<PurchaseTimeSlot> ptsList = PurchaseTimeSlot.dao.find(selSql);
		if (ptsList.size() > 0) {
			for (PurchaseTimeSlot timeSolt : ptsList) {
				List<PurchaseOnepurchaseBuy> pobList = PurchaseOnepurchaseBuy.dao
						.find("select * from purchase_onepurchase_buy where time_slot = '"
								+ timeSolt.getStr("id")
								+ "' and  pay_status='02' and order_status = '09' ");
				if (pobList.size() > 0) {
					for (PurchaseOnepurchaseBuy oneBuy : pobList) {
						OnePurchaseUser user = OnePurchaseUser.dao
								.findById(oneBuy.get("user_id"));
						Db.update("update purchase_users set amount_money=amount_money+"
								+ oneBuy.getFloat("total_price")
								+ " where id='" + oneBuy.get("user_id") + "'");
						PurchaseCommodityPay commodity_pay = new PurchaseCommodityPay()
								.set("id", StringUtil.getUUID())
								.set("order_id", oneBuy.get("total_id"))
								.set("pay_order_no", oneBuy.get("pay_order_no"))
								.set("paid_amount", oneBuy.get("total_price"))
								.set("source_type", "02")
								.set("user_id", oneBuy.get("user_id"))
								.set("status", "02").set("pay_type", "02")
								.set("commodity_type", "02")
								.set("create_by", oneBuy.get("user_id"))
								.set("create_name", user.get("nick_name"))
								.set("create_date", new Date())
								.set("update_by", oneBuy.get("user_id"))
								.set("update_name", user.get("nick_name"))
								.set("update_date", new Date())
								.set("remarks", "定时任务退款");
						commodity_pay.save();
						Db.update("update purchase_onepurchase_buy set order_status='07' where id = '"
								+ oneBuy.getStr("id") + "'");
						List<PurchaseOnepurchaseBuy> countOnepurchaseBuy = PurchaseOnepurchaseBuy.dao
								.find("select * from purchase_onepurchase_buy where total_id='"
										+ oneBuy.getStr("total_id")
										+ "' and order_status = '09'");
						if (countOnepurchaseBuy.size() == 0) {
							Db.update("update purchase_onepurchase_buy_total set order_status='07' where id = '"
									+ oneBuy.getStr("total_id") + "'");
						}
					}
				}

				PurchaseTimeSlot timeSlot = new PurchaseTimeSlot().set("id",
						timeSolt.getStr("id")).set("status", "05");
				timeSlot.update();
			}
		}
	}
}
