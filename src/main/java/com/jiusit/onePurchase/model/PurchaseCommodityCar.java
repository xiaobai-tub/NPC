package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_commodity_car", pkName="id")

public class PurchaseCommodityCar extends Model<PurchaseCommodityCar>{
	public static final PurchaseCommodityCar dao = new PurchaseCommodityCar();
}
