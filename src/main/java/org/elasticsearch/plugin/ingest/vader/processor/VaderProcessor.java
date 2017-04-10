package org.elasticsearch.plugin.ingest.vader.processor;

import com.vader.sentiment.analyzer.SentimentAnalyzer;
import com.vader.sentiment.util.Utils;
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
    private final String positivePolarityField;
    private final String negativePolarityField;
    private final String neutralPolarityField;
    private final String compoundPolarityField;
    private final SentimentAnalyzer sentimentAnalyzer;
    private final boolean ignoreMissing;

    public VaderProcessor(String tag, String field, String positivePolarityField, String negativePolarityField,
                          String neutralPolarityField, String compoundPolarityField, SentimentAnalyzer sentimentAnalyzer,
                          boolean ignoreMissing) {
        super(tag);
        this.field = field;
        this.positivePolarityField = positivePolarityField;
        this.negativePolarityField = negativePolarityField;
        this.neutralPolarityField = neutralPolarityField;
        this.compoundPolarityField = compoundPolarityField;
        this.sentimentAnalyzer = sentimentAnalyzer;
        this.ignoreMissing = ignoreMissing;
    }

    String getField() {
        return field;
    }

    public String getPositivePolarityField() {
        return positivePolarityField;
    }

    public String getNegativePolarityField() {
        return negativePolarityField;
    }

    public String getNeutralPolarityField() {
        return neutralPolarityField;
    }

    public String getCompoundPolarityField() {
        return compoundPolarityField;
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
        validateTargetField(document, positivePolarityField);
        validateTargetField(document, negativePolarityField);
        validateTargetField(document, neutralPolarityField);
        validateTargetField(document, compoundPolarityField);

        Object value = document.getFieldValue(field, Object.class);
        if (value != null && value instanceof String) {
            try {
                String fullText = value.toString().trim();
                if (fullText.length() > 1) {
                    sentimentAnalyzer.setInputString(fullText);
                    sentimentAnalyzer.setInputStringProperties();
                    sentimentAnalyzer.analyse();
                    HashMap<String, Float> polarity = sentimentAnalyzer.getPolarity();

                    String[] names = fullText.split("\\s+");
                    int length = names.length;
                    if (length < 2) {
                        throw new IllegalArgumentException("field [" + field + "] should have a full name");
                    }
                    try {
                        document.setFieldValue(positivePolarityField, polarity.get(Utils.POSITIVE_SCORE));
                        document.setFieldValue(negativePolarityField, polarity.get(Utils.NEGATIVE_SCORE));
                        document.setFieldValue(neutralPolarityField, polarity.get(Utils.NEUTRAL_SCORE));
                        document.setFieldValue(compoundPolarityField, polarity.get(Utils.COMPOUND_SCORE));
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
            String field = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "field");
            String positivePolarityField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "positive_polarity");
            String negativePolarityField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "negative_polarity");
            String neutralPolarityField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "neutral_polarity");
            String compoundPolarityField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "compound_polarity");
            boolean ignoreMissing = ConfigurationUtils.readBooleanProperty(TYPE, processorTag, config, "ignore_missing", false);
            return new VaderProcessor(processorTag, field, positivePolarityField, negativePolarityField,
                    neutralPolarityField, compoundPolarityField, sentimentAnalyzer, ignoreMissing);
        }
    }
}
