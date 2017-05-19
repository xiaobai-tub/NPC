package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_commodity_version", pkName="id")
public class PurchaseCommodityVersion extends Model<PurchaseCommodityVersion>{
	public static final PurchaseCommodityVersion dao = new PurchaseCommodityVersion();
}
