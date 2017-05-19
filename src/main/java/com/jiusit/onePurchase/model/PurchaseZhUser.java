package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="purchase_zh_user", pkName="id")
public class PurchaseZhUser extends Model<PurchaseZhUser>{
	public static final PurchaseZhUser dao = new PurchaseZhUser();
}
