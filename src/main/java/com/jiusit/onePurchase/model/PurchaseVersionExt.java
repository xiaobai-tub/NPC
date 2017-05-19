package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_version_ext", pkName="id")
public class PurchaseVersionExt extends Model<PurchaseVersionExt>{
	public static final PurchaseVersionExt dao = new PurchaseVersionExt();
}
