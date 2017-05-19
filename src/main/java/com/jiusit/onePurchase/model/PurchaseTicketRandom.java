package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_ticket_random", pkName="id")
public class PurchaseTicketRandom extends Model<PurchaseTicketRandom>{
	public static final PurchaseTicketRandom dao = new PurchaseTicketRandom();
}
