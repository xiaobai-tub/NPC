package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_users", pkName="id")
public class OnePurchaseUser extends Model<OnePurchaseUser>{
	public static final OnePurchaseUser dao = new OnePurchaseUser();
}
