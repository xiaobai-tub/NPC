package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="trade_user_access", pkName="id")
public class TradeUserAccess extends Model<TradeUserAccess>{
	public static final TradeUserAccess dao = new TradeUserAccess();
}
