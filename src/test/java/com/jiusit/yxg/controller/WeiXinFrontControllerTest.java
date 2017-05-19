package com.jiusit.yxg.controller;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.config.JFinalConfig;
import com.jfinal.ext.test.ControllerTestCase;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.UserApi;
import com.jiusit.onePurchase.DefaultConfig;

import junit.framework.TestCase;

public class WeiXinFrontControllerTest extends ControllerTestCase<DefaultConfig> {
	
	private static final  Logger log  =  LoggerFactory.getLogger(WeiXinFrontControllerTest.class);
	
	@Test
	public void testloadActivitData() {
		String url = "/loadActivtData";
        String body = "<root>中文</root>";
        String res = use(url).post(body).invoke();
        log.debug(res);
	}
	@Test
	public void testgetUser() {
		ApiResult apiResult = UserApi.getUserInfo("oS4AmuD4fZ7CgnfTB9veQYk222K0");
        System.out.println(apiResult);	
}
	
}  
