package org.elasticsearch.index.analysis;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class IkAnalyzerProvider extends AbstractIndexAnalyzerProvider<IKAnalyzer> {

    private final IKAnalyzer analyzer;
    private final Dictionary dictionary;

    public IkAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings, boolean useSmart) {
        super(indexSettings, name, settings);
        Configuration configuration = new Configuration(env, settings).setUseSmart(useSmart);
        this.dictionary = Dictionary.getInstance(configuration);
        analyzer = new IKAnalyzer(configuration, dictionary);
        if (configuration.isEnableCustomDict()) {
            logger.info("dictionary={}", dictionary);
        }
    }

    public static IkAnalyzerProvider getIkSmartAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new IkAnalyzerProvider(indexSettings, env, name, settings, true);
    }

    public static IkAnalyzerProvider getIkAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new IkAnalyzerProvider(indexSettings, env, name, settings, false);
    }

    @Override
    public IKAnalyzer get() {
        return this.analyzer;
    }

    @Override
    protected void finalize() throws Throwable {
        Dictionary.MONITOR_MAP.remove(this.dictionary).forEach(scheduledFuture -> {
            logger.info("删除scheduledFuture={}", scheduledFuture);
            Dictionary.POOL.remove(scheduledFuture);
        });
    }
}
