package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="trade_market_user", pkName="id")
public class TradeMarketUser extends Model<TradeMarketUser>{
	public static final TradeMarketUser dao = new TradeMarketUser();
}
