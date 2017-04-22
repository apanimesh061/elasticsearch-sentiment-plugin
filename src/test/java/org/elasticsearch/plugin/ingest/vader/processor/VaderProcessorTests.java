package org.elasticsearch.plugin.ingest.vader.processor;

import com.vader.sentiment.analyzer.SentimentAnalyzer;
import org.junit.Test;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.junit.Before;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.test.ESTestCase.randomAsciiOfLength;
import static org.elasticsearch.test.ESTestCase.random;
import static org.elasticsearch.ingest.RandomDocumentPicks.randomIngestDocument;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;


/**
 * @author Animesh Pandey
 */
public class VaderProcessorTests {
    private SentimentAnalyzer sentimentAnalyzerService;
    private static String SOURCE_FIELD = "source_field";
    private static String TARGET_FIELD = "target_field";

    @Test
    public void testThatExtractionWork() throws Exception {
        VaderProcessor vaderProcessor = new VaderProcessor(
                randomAsciiOfLength(10),
                SOURCE_FIELD,
                TARGET_FIELD,
                sentimentAnalyzerService,
                false
        );

        Map<String, Object> entityData = getIngestDocumentData(vaderProcessor);

        System.out.println(entityData);
    }

    private IngestDocument getIngestDocument() throws Exception {
        return getIngestDocument("I'm a bit of a regular here. I'd like to give it 5 stars, but almost every time I go " +
                "my friends and I get frustrated with the service, so I had to knock it down to 4 stars. The servers " +
                "are extremely inattentive. \\nWe always sit in the bar/lounge area, which is beautifully decorated " +
                "and has quite a fresh, relaxing vibe. \\nThe drinks and food are excellent -- I've only had 1 slightly " +
                "bad food experience here out of probably 15 visits. During happy hour, drinks are cheaper and we always " +
                "get a few apps to share. The food is really outstanding, full of flavors and VERY unique for Pittsburgh. " +
                "I love it! Go!"
        );
    }

    private IngestDocument getIngestDocument(String content) throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put(SOURCE_FIELD, content);
        return randomIngestDocument(random(), document);
    }

    private Map<String, Object> getIngestDocumentData(VaderProcessor processor) throws Exception {
        IngestDocument ingestDocument = getIngestDocument();
        processor.execute(ingestDocument);
        return getIngestDocumentData(ingestDocument);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getIngestDocumentData(IngestDocument ingestDocument) {
        return (Map<String, Object>) ingestDocument.getSourceAndMetadata().get(SOURCE_FIELD);
    }
}
