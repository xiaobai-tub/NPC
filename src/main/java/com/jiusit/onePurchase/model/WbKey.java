package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;
@TableBind(tableName="wb_key", pkName="key_id")
public class WbKey extends Model<WbKey> {
	public static final WbKey dao = new WbKey();
}
