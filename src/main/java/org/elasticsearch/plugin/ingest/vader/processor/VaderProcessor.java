package org.elasticsearch.plugin.ingest.vader.processor;

import com.vader.sentiment.analyzer.SentimentAnalyzer;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.ConfigurationUtils;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Animesh Pandey
 *         Created on 4/2/2017.
 */
public final class VaderProcessor extends AbstractProcessor {
    public static final String TYPE = "vader_analyzer";

    private final String field;
    private final String polarityField;
    private final SentimentAnalyzer sentimentAnalyzer;
    private final boolean ignoreMissing;

    public VaderProcessor(String tag, String field, String polarityField, SentimentAnalyzer sentimentAnalyzer, boolean ignoreMissing) {
        super(tag);
        this.field = field;
        this.polarityField = polarityField;
        this.sentimentAnalyzer = sentimentAnalyzer;
        this.ignoreMissing = ignoreMissing;
    }

    String getField() {
        return field;
    }

    public String getPolarityField() {
        return polarityField;
    }

    boolean isIgnoreMissing() {
        return ignoreMissing;
    }

    private void validateTargetField(IngestDocument document, String fieldName) {
        if (document.hasField(fieldName, true)) {
            throw new IllegalArgumentException("field [" + fieldName + "] already exists");
        }
    }

    @Override
    public void execute(IngestDocument document) {
        if (!document.hasField(field, true)) {
            if (ignoreMissing) {
                return;
            } else {
                throw new IllegalArgumentException("field [" + field + "] doesn't exist");
            }
        }

        validateTargetField(document, polarityField);

        Object value = document.getFieldValue(field, Object.class);
        if (value != null && value instanceof String) {
            try {
                String fullText = value.toString().trim();
                if (fullText.length() > 1) {
                    sentimentAnalyzer.setInputString(fullText);
                    sentimentAnalyzer.setInputStringProperties();
                    sentimentAnalyzer.analyse();
                    HashMap<String, Float> polarity = sentimentAnalyzer.getPolarity();
                    try {
                        document.setFieldValue(polarityField, polarity);
                    } catch (Exception e) {
                        document.setFieldValue(field, value);
                        throw e;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {
        private SentimentAnalyzer sentimentAnalyzer;

        public Factory(SentimentAnalyzer sentimentAnalyzer) {
            this.sentimentAnalyzer = sentimentAnalyzer;
        }

        @Override
        public VaderProcessor create(Map<String, Processor.Factory> registry, String processorTag, Map<String, Object> config) throws Exception {
            String field = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "input_text_field");
            String polarityField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "polarity_field");
            boolean ignoreMissing = ConfigurationUtils.readBooleanProperty(TYPE, processorTag, config, "ignore_missing", false);
            return new VaderProcessor(processorTag, field, polarityField, sentimentAnalyzer, ignoreMissing);
        }
    }
}
