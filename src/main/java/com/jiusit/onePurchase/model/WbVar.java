package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;
@TableBind(tableName="wb_var", pkName="var_id")
public class WbVar extends Model<WbVar> {
	public static final WbVar dao = new WbVar();
}
