package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;
@TableBind(tableName = "purchase_commodity_record",pkName="id")
public class PurchaseCommodityRecord extends Model<PurchaseCommodityRecord>{
	public static final PurchaseCommodityRecord dao = new PurchaseCommodityRecord();
}
