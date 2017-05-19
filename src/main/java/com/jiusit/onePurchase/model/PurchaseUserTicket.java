package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_user_ticket", pkName="id")
public class PurchaseUserTicket extends Model<PurchaseUserTicket>{
	public static final PurchaseUserTicket dao = new PurchaseUserTicket();
}
