package org.elasticsearch.plugin.ingest.vader.processor;

import com.vader.sentiment.analyzer.SentimentAnalyzer;
import org.apache.log4j.Logger;
import org.elasticsearch.common.StopWatch;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Animesh Pandey
 *         Created on 4/23/2017.
 */
public class VaderSentimentService {
    private final Logger logger;

    private ThreadLocal<SentimentAnalyzer> sentimentAnalyzerThreadLocal = new ThreadLocal<>();

    public VaderSentimentService() {
        this.logger = Logger.getLogger(getClass());
    }

    protected VaderSentimentService start() {
        StopWatch sw = new StopWatch("Creating the VADER service");
        sw.start();
        sw.stop();
        logger.info("Creating the service for sentiment analysis in " + sw.totalTime().toString());
        return this;
    }

    public HashMap<String, Float> apply(String document) throws IOException {
        try {
            SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer();
            if (sentimentAnalyzerThreadLocal.get() == null || !sentimentAnalyzerThreadLocal.get().equals(sentimentAnalyzer)) {
                sentimentAnalyzerThreadLocal.set(sentimentAnalyzer);
            }
            sentimentAnalyzer.setInputString(document);
            sentimentAnalyzer.setInputStringProperties();
            sentimentAnalyzer.analyze();
            return sentimentAnalyzer.getPolarity();
        } finally {
            sentimentAnalyzerThreadLocal.remove();
        }
    }
}
