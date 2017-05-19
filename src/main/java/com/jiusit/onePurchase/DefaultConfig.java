/**
 * Copyright (c) 2011-2014, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.jiusit.onePurchase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.ext.plugin.quartz.QuartzPlugin;
import com.jfinal.ext.plugin.tablebind.AutoTableBindPlugin;
import com.jfinal.ext.plugin.tablebind.SimpleNameStyles;
import com.jfinal.ext.route.AutoBindRoutes;
import com.jfinal.kit.StringKit;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.render.FreeMarkerRender;
import com.jfinal.weixin.demo.WeixinApiController;
import com.jfinal.weixin.demo.WeixinMsgController;
import com.jfinal.weixin.sdk.api.ApiConfigKit;

import freemarker.template.TemplateModelException;

public class DefaultConfig extends JFinalConfig {

	@Override
	public void afterJFinalStart() {
		super.afterJFinalStart();
		// 加载基础数据,执行areautil
		//CacheKit.put("commonCache", "areas", res);
		QuartzPlugin quartzPlugin = new QuartzPlugin("job.properties");
		quartzPlugin.start();
	}

	public Properties loadProp(String pro, String dev) {
		try {
			return loadPropertyFile(pro);
		} catch (Exception e) {
			return loadPropertyFile(dev);
		}
	}

	public void configConstant(Constants me) {
		// 如果生产环境配置文件存在，则优先加载该配置，否则加载开发环境配置文件
		Properties props = loadProp("application_pro.properties", "application.properties");
		me.setDevMode(getPropertyToBoolean("devMode", false));
		// ApiConfigKit 设为开发模式可以在开发阶段输出请求交互的 xml 与 json 数据
		ApiConfigKit.setDevMode(me.getDevMode());
	}

	public void configRoute(Routes me) {
		/* 自动扫描 [注解方式] */
		AutoBindRoutes autoBindRoutes = new AutoBindRoutes();
		List excludeClasses = new ArrayList();
		excludeClasses.add(WeixinApiController.class);
		excludeClasses.add(WeixinMsgController.class);

		autoBindRoutes.addExcludeClasses(excludeClasses);
		me.add(autoBindRoutes);
	}

	public void configPlugin(Plugins me) {
		C3p0Plugin c3p0Plugin = new C3p0Plugin(getProperty("jdbcUrl"),
				getProperty("user"), getProperty("password").trim());
		me.add(c3p0Plugin);

		EhCachePlugin ecp = new EhCachePlugin();
		me.add(ecp);
		
		/*非注解方式
		// 配置ActiveRecord插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin(c3p0Plugin);
		arp.setShowSql(true);
		me.add(arp);
		
		arp.addMapping("cwo_news_content", "content_id", News.class); // 映射cwo_news_content 表到 News模型
		arp.addMapping("cwo_news_column", "news_id", Catalog.class); // 映射cwo_news_column 表到 Catalog模型
		arp.addMapping("cwo_wx_plan", "plan_id", Plan.class); //
		arp.addMapping("cwo_wx_plan_join", "join_id", PlanJoin.class); // 
		arp.addMapping("cwo_wx_user", "user_id", User.class); // 
		arp.addMapping("cwo_wx_user_amount", "amount_id", UserAmount.class); // 
		*/
		
		//开启注解
		AutoTableBindPlugin atbp = new AutoTableBindPlugin(c3p0Plugin, SimpleNameStyles.LOWER);
		atbp.setShowSql(true);
		me.add(atbp);
	}

	public void configInterceptor(Interceptors me) {
		
	}

	public void configHandler(Handlers me) {
		me.add(new ContextPathHandler("ctx"));
		/*添加context另一种写法
		try {
			FreeMarkerRender.getConfiguration().setSharedVariable("ctx", JFinal.me().getContextPath());
		} catch (TemplateModelException e) {
			e.printStackTrace();
		}
		*/
	}

	public static void main(String[] args) {
		JFinal.start("webapp", 80, "/", 5);
	}
}
