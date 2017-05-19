package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_commodity_buy", pkName="id")

public class PurchaseCommodityBuy extends Model<PurchaseCommodityBuy>{
	public static final PurchaseCommodityBuy dao = new PurchaseCommodityBuy();
}
