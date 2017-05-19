package com.jiusit.onePurchase.controller;

import java.util.Map;

import com.alibaba.fastjson.JSON;

public class CardTicket {
	private String jsapi;// 服务器返回
	private Long expiredTime; // 过期时间
	private Integer errcode; // 错误码
	private String errmsg; // 错误信息
	private String ticket; // 票据

	private Integer expires_in; //

	public CardTicket(String jsapiStr) {
		this.jsapi = jsapiStr;
		Map e = JSON.parseObject(jsapiStr, Map.class);
		this.expires_in = (Integer) e.get("expires_in");
		this.errcode = (Integer) e.get("errcode");
		this.errmsg = (String) e.get("errmsg");
		this.ticket = (String) e.get("ticket");

		if (this.expires_in != null) {
			this.expiredTime = Long.valueOf(System.currentTimeMillis() + (long) ((this.expires_in.intValue() - 5) * 1000));
		}

	}

	public boolean isAvailable() {
		return this.expiredTime == null ? false : (this.errcode != 0 ? false : (this.expiredTime.longValue() < System.currentTimeMillis() ? false : this.ticket != null));
	}

	public String getJsapi() {
		return jsapi;
	}

	public Long getExpiredTime() {
		return expiredTime;
	}

	public Integer getErrcode() {
		return errcode;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public String getTicket() {
		return ticket;
	}

	public Integer getExpires_in() {
		return expires_in;
	}
}
