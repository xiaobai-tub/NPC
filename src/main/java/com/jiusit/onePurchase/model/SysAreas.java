package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="sys_areas", pkName="area_code")
public class SysAreas extends Model<SysAreas>{
	public static final SysAreas dao = new SysAreas();
}
