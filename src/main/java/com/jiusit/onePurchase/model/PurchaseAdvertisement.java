package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_advertisement", pkName="id")
public class PurchaseAdvertisement extends Model<PurchaseAdvertisement>{
	public static final PurchaseAdvertisement dao = new PurchaseAdvertisement();
}
