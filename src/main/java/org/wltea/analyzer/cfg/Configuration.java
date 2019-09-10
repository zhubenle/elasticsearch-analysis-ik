/**
 * 
 */
package org.wltea.analyzer.cfg;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugin.analysis.ik.AnalysisIkPlugin;

import java.io.File;
import java.nio.file.Path;

public class Configuration {

	private Environment environment;
	private Settings settings;

	/**
	 * 是否启用智能分词
	 */
	private  boolean useSmart;
	/**
	 * 是否启用小写处理
	 */
	private boolean enableLowercase;
	/**
	 * 是否启用自定义配置，既在mapping中自定义配置，而不是采用IKAnalyzer.cfg.xml的默认配置
	 */
	private boolean enableCustomDict;


	@Inject
	public Configuration(Environment env,Settings settings) {
		this.environment = env;
		this.settings=settings;
		this.useSmart = false;
		this.enableLowercase = settings.getAsBoolean("enable_lowercase", true);
		this.enableCustomDict = settings.getAsBoolean("enable_custom_dict", false);
	}

	public Path getConfigInPluginDir() {
		return PathUtils
				.get(new File(AnalysisIkPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath())
						.getParent(), "config")
				.toAbsolutePath();
	}

	public boolean isUseSmart() {
		return useSmart;
	}

	public Configuration setUseSmart(boolean useSmart) {
		this.useSmart = useSmart;
		return this;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public Settings getSettings() {
		return settings;
	}

	public boolean isEnableCustomDict() {
		return enableCustomDict;
	}

	public boolean isEnableLowercase() {
		return enableLowercase;
	}
}
