package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;
@TableBind(tableName = "purchase_commodity_content",pkName="id")
public class PurchaseCommodityContent extends Model<PurchaseCommodityContent>{
	public static final  PurchaseCommodityContent dao = new PurchaseCommodityContent();
}
