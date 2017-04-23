package org.elasticsearch.plugin.ingest.vader.processor;

import org.elasticsearch.ingest.Processor;
import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a custom IngestPlugin.
 *
 * @author Animesh Pandey
 */
public class VaderSentimentPlugin extends Plugin implements IngestPlugin {

    /**
     * Defines a map of all types of processors implemented in this plugin.
     *
     * @param parameters These values are input when this plugin being registered.
     * @return a HashMap of the processor type and their respective factories.
     */
    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
        VaderSentimentService vaderSentimentService = new VaderSentimentService();
        vaderSentimentService.start();
        Processor.Factory sentimentFactory = new VaderProcessor.Factory(vaderSentimentService);
        return new HashMap<String, Processor.Factory>() {{
            put(VaderProcessor.TYPE, sentimentFactory);
        }};
    }
}
