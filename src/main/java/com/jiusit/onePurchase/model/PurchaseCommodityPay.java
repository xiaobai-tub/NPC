package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_commodity_pay", pkName="id")
public class PurchaseCommodityPay extends Model<PurchaseCommodityPay>{
	public static final PurchaseCommodityPay dao = new PurchaseCommodityPay();
}
