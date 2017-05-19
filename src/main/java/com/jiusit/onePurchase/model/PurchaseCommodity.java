package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_commodity", pkName="id")
public class PurchaseCommodity extends Model<PurchaseCommodity>{
	public static final PurchaseCommodity dao = new PurchaseCommodity();
}
