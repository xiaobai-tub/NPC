package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;
@TableBind(tableName="purchase_onepurchase_buy", pkName="id")
public class PurchaseOnepurchaseBuy extends Model<PurchaseOnepurchaseBuy> {
	public static final PurchaseOnepurchaseBuy dao = new PurchaseOnepurchaseBuy();
}
