package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_time_slot", pkName="id")
public class PurchaseTimeSlot extends Model<PurchaseTimeSlot>{
	public static final PurchaseTimeSlot dao = new PurchaseTimeSlot();
}
