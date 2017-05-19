package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_random_num", pkName="id")
public class PurchaseRandomNum extends Model<PurchaseRandomNum>{
	public static final PurchaseRandomNum dao = new PurchaseRandomNum();
}
