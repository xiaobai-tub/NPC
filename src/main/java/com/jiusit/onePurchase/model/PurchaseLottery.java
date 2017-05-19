package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_lottery", pkName="id")
public class PurchaseLottery extends Model<PurchaseLottery>{
	public static final PurchaseLottery dao = new PurchaseLottery();
}
