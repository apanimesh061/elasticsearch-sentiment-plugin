package org.elasticsearch.plugin.ingest.vader.processor;

import com.vader.sentiment.analyzer.SentimentAnalyzer;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Animesh Pandey
 *         Created on 4/2/2017.
 */
public class VaderSentimentPlugin extends Plugin implements IngestPlugin {
    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
        SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer();
        Processor.Factory sentimentFactory = new VaderProcessor.Factory(sentimentAnalyzer);
        return new HashMap<String, Processor.Factory>() {{
            put(VaderProcessor.TYPE, sentimentFactory);
        }};
    }
}
