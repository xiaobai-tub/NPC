package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;
@TableBind(tableName="purchase_onepurchase_buy_total", pkName="id")
public class PurchaseOnepurchaseBuyTotal extends Model<PurchaseOnepurchaseBuyTotal> {
	public static final PurchaseOnepurchaseBuyTotal dao = new PurchaseOnepurchaseBuyTotal();
}
