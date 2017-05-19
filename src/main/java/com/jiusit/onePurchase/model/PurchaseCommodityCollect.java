package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;
@TableBind(tableName = "purchase_commodity_collect",pkName="id")
public class PurchaseCommodityCollect extends Model<PurchaseCommodityCollect>{
	public static final PurchaseCommodityCollect dao = new PurchaseCommodityCollect();
}
