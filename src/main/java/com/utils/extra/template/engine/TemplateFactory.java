package com.utils.extra.template.engine;

import com.utils.core.util.StrUtil;
import com.utils.extra.template.TemplateConfig;
import com.utils.extra.template.TemplateEngine;
import com.utils.log.StaticLog;

/**
 * 简单模板工厂，用于根据用户引入的模板引擎jar，自动创建对应的模板引擎对象
 * 
 * @author looly
 *
 */
public class TemplateFactory {
	/**
	 * 根据用户引入的模板引擎jar，自动创建对应的模板引擎对象<br>
	 * 推荐创建的引擎单例使用，此方法每次调用会返回新的引擎
	 * 
	 * @param config 模板配置，包括编码、模板文件path等信息
	 */
	public static TemplateEngine create(TemplateConfig config) {
		final TemplateEngine engine = doCreate(config);
		StaticLog.debug("Use [{}] Engine As Default.", StrUtil.removeSuffix(engine.getClass().getSimpleName(), "Engine"));
		return engine;
	}

	/**
	 * 根据用户引入的模板引擎jar，自动创建对应的模板引擎对象<br>
	 * 推荐创建的引擎单例使用，此方法每次调用会返回新的引擎
	 * 
	 * @param config 模板配置，包括编码、模板文件path等信息
	 */
	private static TemplateEngine doCreate(TemplateConfig config) {/*
		try {
			return new BeetlEngine(config);
		} catch (NoClassDefFoundError e) {
			// ignore
		}
		try {
			return new FreemarkerEngine(config);
		} catch (NoClassDefFoundError e) {
			// ignore
		}
		try {
			return new VelocityEngine(config);
		} catch (NoClassDefFoundError e) {
			// ignore
		}
		try {
			return new RythmEngine(config);
		} catch (NoClassDefFoundError e) {
			// ignore
		}
		try {
			return new EnjoyEngine(config);
		} catch (NoClassDefFoundError e) {
			// ignore
		}
		try {
			return new ThymeleafEngine(config);
		} catch (NoClassDefFoundError e) {
			// ignore
		}
		throw new TemplateException("No template found ! Please add one of template jar to your project !");*/
		return  null ;
	}
}
