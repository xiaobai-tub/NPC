package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_commodity_buy_total", pkName="id")

public class PurchaseCommodityBuyTotal extends Model<PurchaseCommodityBuyTotal>{
	public static final PurchaseCommodityBuyTotal dao = new PurchaseCommodityBuyTotal();
}
