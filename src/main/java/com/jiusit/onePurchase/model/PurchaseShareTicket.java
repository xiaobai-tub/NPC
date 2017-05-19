package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_share_ticket", pkName="id")
public class PurchaseShareTicket extends Model<PurchaseShareTicket>{
	public static final PurchaseShareTicket dao = new PurchaseShareTicket();
}
