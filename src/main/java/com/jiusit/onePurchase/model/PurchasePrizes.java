package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_prizes", pkName="id")
public class PurchasePrizes extends Model<PurchasePrizes>{
	public static final PurchasePrizes dao = new PurchasePrizes();
}
