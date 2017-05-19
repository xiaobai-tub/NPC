package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="sys_areas", pkName="area_code")
public class Areas extends Model<Areas>{
	public static final Areas dao = new Areas();
}
