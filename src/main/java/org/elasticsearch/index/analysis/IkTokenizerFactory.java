package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKTokenizer;

public class IkTokenizerFactory extends AbstractTokenizerFactory {

    private Configuration configuration;
    private Dictionary dictionary;

    public IkTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, settings);
        configuration = new Configuration(env, settings);
        this.dictionary = Dictionary.getInstance(configuration);
        if (configuration.isEnableCustomDict()) {
            logger.info("dictionary={}", dictionary);
        }
    }

    public static IkTokenizerFactory getIkTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new IkTokenizerFactory(indexSettings, env, name, settings).setSmart(false);
    }

    public static IkTokenizerFactory getIkSmartTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new IkTokenizerFactory(indexSettings, env, name, settings).setSmart(true);
    }

    public IkTokenizerFactory setSmart(boolean smart) {
        this.configuration.setUseSmart(smart);
        return this;
    }

    @Override
    public Tokenizer create() {
        return new IKTokenizer(configuration, dictionary);
    }

    @Override
    protected void finalize() throws Throwable {
        Dictionary.MONITOR_MAP.remove(this.dictionary).forEach(scheduledFuture -> {
            logger.info("删除scheduledFuture={}", scheduledFuture);
            Dictionary.POOL.remove(scheduledFuture);
        });
    }
}
