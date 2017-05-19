package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_ticket", pkName="id")
public class PurchaseTicket extends Model<PurchaseTicket>{
	public static final PurchaseTicket dao = new PurchaseTicket();
}
