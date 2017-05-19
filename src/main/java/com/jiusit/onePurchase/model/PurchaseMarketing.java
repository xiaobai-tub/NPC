package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_marketing", pkName="id")
public class PurchaseMarketing extends Model<PurchaseMarketing>{
	public static final PurchaseMarketing dao = new PurchaseMarketing();
}
