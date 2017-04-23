package org.elasticsearch.plugin.ingest.vader.processor;

import com.vader.sentiment.analyzer.SentimentAnalyzer;
import org.junit.Assert;
import org.junit.Test;

import org.elasticsearch.ingest.IngestDocument;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.test.ESTestCase.random;
import static org.elasticsearch.ingest.RandomDocumentPicks.randomIngestDocument;
import static org.elasticsearch.test.ESTestCase.randomAsciiOfLength;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;


/**
 * Test cases for processor.
 */
@RunWith(com.carrotsearch.randomizedtesting.RandomizedRunner.class)
public class VaderProcessorTests {
    private SentimentAnalyzer sentimentAnalyzerService;
    private static String SOURCE_FIELD = "source_field";
    private static String TARGET_FIELD = "target_field";

    @Before
    public void setUp() {
        sentimentAnalyzerService = new SentimentAnalyzer();
    }

    /**
     * Tests if the processed document has the correct target field and source field values.
     *
     * @throws Exception is an error occurs.
     */
    @Test
    public void testForNonEmptyValidDocument() throws Exception {
        VaderProcessor vaderProcessor = new VaderProcessor(
                randomAsciiOfLength(10),
                SOURCE_FIELD,
                TARGET_FIELD,
                sentimentAnalyzerService,
                false
        );

        Map<String, Object> entityData = getIngestDocumentData(vaderProcessor, getValidIngestDocument());
        Object sourceData = getIngestDocumentSourceData(vaderProcessor, getValidIngestDocument());
        assertThat(entityData.keySet(), containsInAnyOrder("compound", "positive", "negative", "neutral"));
        assertThat(sourceData, instanceOf(String.class));
    }

    /**
     * Tests if the document does not have any target field as the input document was empty.
     *
     * @throws Exception is an error occurs.
     */
    @Test
    public void testForEmptyValidDocument() throws Exception {
        VaderProcessor vaderProcessor = new VaderProcessor(
                randomAsciiOfLength(10),
                SOURCE_FIELD,
                TARGET_FIELD,
                sentimentAnalyzerService,
                false
        );

        Map<String, Object> entityData = getIngestDocumentData(vaderProcessor, getEmptyValidIngestDocument());
        Assert.assertNull(entityData);
    }

    /**
     * Tests if exception is thrown when the input document does not have any source field.
     *
     * @throws Exception is an other error occurs.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testForNonEmptyInValidDocument() throws Exception {
        VaderProcessor vaderProcessor = new VaderProcessor(
                randomAsciiOfLength(10),
                SOURCE_FIELD,
                TARGET_FIELD,
                sentimentAnalyzerService,
                false
        );

        Map<String, Object> entityData = getIngestDocumentData(vaderProcessor, getIngestDocumentWithWrongSource());
    }

    /**
     * Tests if exception is thrown when the input document already has a target field.
     *
     * @throws Exception is an other error occurs.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testForNonEmptyInValidDocumentWithExistingTargetField() throws Exception {
        VaderProcessor vaderProcessor = new VaderProcessor(
                randomAsciiOfLength(10),
                SOURCE_FIELD,
                TARGET_FIELD,
                sentimentAnalyzerService,
                false
        );

        Map<String, Object> entityData = getIngestDocumentData(vaderProcessor,
                getValidIngestDocumentWithExistingTargetField());
    }

    private IngestDocument getValidIngestDocument() throws Exception {
        return getIngestDocument(
                "I'm a bit of a regular here. I'd like to give it 5 stars, but almost every time I go " +
                        "my friends and I get frustrated with the service, so I had to knock it down to 4 stars. The servers " +
                        "are extremely inattentive. \\nWe always sit in the bar/lounge area, which is beautifully decorated " +
                        "and has quite a fresh, relaxing vibe. \\nThe drinks and food are excellent -- I've only had 1 slightly " +
                        "bad food experience here out of probably 15 visits. During happy hour, drinks are cheaper and we always " +
                        "get a few apps to share. The food is really outstanding, full of flavors and VERY unique for Pittsburgh. " +
                        "I love it! Go!",
                SOURCE_FIELD
        );
    }

    private IngestDocument getValidIngestDocumentWithExistingTargetField() throws Exception {
        return getIngestDocument(
                "I'm a bit of a regular here. I'd like to give it 5 stars, but almost every time I go " +
                        "my friends and I get frustrated with the service, so I had to knock it down to 4 stars. The servers " +
                        "are extremely inattentive. \\nWe always sit in the bar/lounge area, which is beautifully decorated " +
                        "and has quite a fresh, relaxing vibe. \\nThe drinks and food are excellent -- I've only had 1 slightly " +
                        "bad food experience here out of probably 15 visits. During happy hour, drinks are cheaper and we always " +
                        "get a few apps to share. The food is really outstanding, full of flavors and VERY unique for Pittsburgh. " +
                        "I love it! Go!",
                SOURCE_FIELD,
                TARGET_FIELD
        );
    }

    private IngestDocument getEmptyValidIngestDocument() throws Exception {
        return getIngestDocument("", SOURCE_FIELD);
    }

    private IngestDocument getIngestDocumentWithWrongSource() throws Exception {
        return getIngestDocument(
                "I'm a bit of a regular here. I'd like to give it 5 stars, but almost every time I go " +
                        "my friends and I get frustrated with the service, so I had to knock it down to 4 stars. The servers " +
                        "are extremely inattentive. \\nWe always sit in the bar/lounge area, which is beautifully decorated " +
                        "and has quite a fresh, relaxing vibe. \\nThe drinks and food are excellent -- I've only had 1 slightly " +
                        "bad food experience here out of probably 15 visits. During happy hour, drinks are cheaper and we always " +
                        "get a few apps to share. The food is really outstanding, full of flavors and VERY unique for Pittsburgh. " +
                        "I love it! Go!",
                "other_field"
        );
    }

    private IngestDocument getIngestDocument(String content, String sourceField, String targetField) throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put(sourceField, content);
        document.put(targetField, new Object());
        return randomIngestDocument(random(), document);
    }

    private IngestDocument getIngestDocument(String content, String sourceField) throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put(sourceField, content);
        return randomIngestDocument(random(), document);
    }

    private Map<String, Object> getIngestDocumentData(VaderProcessor processor, IngestDocument ingestDocument)
            throws Exception {
        processor.execute(ingestDocument);
        return getIngestDocumentData(ingestDocument);
    }

    private Object getIngestDocumentSourceData(VaderProcessor processor, IngestDocument ingestDocument)
            throws Exception {
        processor.execute(ingestDocument);
        return getIngestDocumentSourceValue(ingestDocument);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getIngestDocumentData(IngestDocument ingestDocument) {
        return (Map<String, Object>) ingestDocument.getSourceAndMetadata().get(TARGET_FIELD);
    }

    private Object getIngestDocumentSourceValue(IngestDocument ingestDocument) {
        return ingestDocument.getSourceAndMetadata().get(SOURCE_FIELD);
    }
}
