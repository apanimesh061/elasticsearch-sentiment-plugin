package org.elasticsearch.plugin.ingest;

import org.elasticsearch.plugin.ingest.processor.InitialProcessor;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.Collections;
import java.util.Map;

/**
 * @author Animesh Pandey
 *         Created on 4/2/2017.
 */
public class VaderSentimentPlugin extends Plugin implements IngestPlugin {
    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
        return Collections.singletonMap(
                InitialProcessor.TYPE,
                (factories, tag, config) -> new InitialProcessor.Factory().create(factories, tag, config)
        );
    }
}
