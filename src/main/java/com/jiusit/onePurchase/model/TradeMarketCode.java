package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="trade_market_code", pkName="id")
public class TradeMarketCode extends Model<TradeMarketCode>{
	public static final TradeMarketCode dao = new TradeMarketCode();
}
