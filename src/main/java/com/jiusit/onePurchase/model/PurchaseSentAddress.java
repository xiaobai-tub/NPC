package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;
@TableBind(tableName = "purchase_sent_address",pkName="id")
public class PurchaseSentAddress extends Model<PurchaseSentAddress>{
	public static final PurchaseSentAddress dao = new PurchaseSentAddress();

}
