package com.jiusit.onePurchase.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="trade_market_share", pkName="id")
public class TradeMarketShare extends Model<TradeMarketShare>{
	public static final TradeMarketShare dao = new TradeMarketShare();
}
