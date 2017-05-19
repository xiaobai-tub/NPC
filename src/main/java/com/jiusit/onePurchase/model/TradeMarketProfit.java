package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="trade_market_profit", pkName="id")
public class TradeMarketProfit extends Model<TradeMarketProfit>{
	public static final TradeMarketProfit dao = new TradeMarketProfit();
}
