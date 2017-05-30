package org.elasticsearch.plugin.ingest.vader.processor;

import com.vader.sentiment.analyzer.SentimentAnalyzer;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.ConfigurationUtils;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.io.IOException;
import java.util.Map;

/**
 * This class is the processor class for the ingestion plugin for sentiment analysis.
 * This class defines the field in a document on which the processor would be applied.
 *
 * @author Animesh Pandey
 */
public final class VaderProcessor extends AbstractProcessor {

    /**
     * This is wrapper around the {@link SentimentAnalyzer} which helps in making it thread safe
     * as the original implementation is not thread safe.
     * <p>
     * This class helps in calculating the value of the text associated with the
     * {@link org.elasticsearch.plugin.ingest.vader.processor.VaderProcessor#sourceField}
     * and then sets the result obtained from {@link SentimentAnalyzer#getPolarity()}
     * to the field {@link org.elasticsearch.plugin.ingest.vader.processor.VaderProcessor#targetField}.
     * <p>
     * Ref:
     * The implementation of {@link SentimentAnalyzer} is
     * given <a href="https://github.com/apanimesh061/VaderSentimentJava">here</a>.
     */
    private VaderSentimentService vaderSentimentService;

    /**
     * Name of the current processor.
     * We will be using this name when we register or query
     * the current plugin.
     */
    public static final String TYPE = "vader_analyzer";

    /**
     * field name which will be processed in this processor.
     */
    private final String sourceField;

    /**
     * field name which is going to a HashMap of sentiment polarity and
     * their respective values.
     */
    private final String targetField;

    /**
     * If in the input document the {@link org.elasticsearch.plugin.ingest.vader.processor.VaderProcessor#sourceField}
     * does not exist, you either ignore that document or throw an exception.
     * <p>
     * This value will false by default.
     */
    private final boolean ignoreMissing;

    /**
     * Parameterised constructor for current processor.
     * This sets the values for all the field in this class.
     *
     * @param vaderSentimentService library that will help in performing sentiment analysis
     * @param tag                   Tag of the current processor
     * @param sourceField           field in input document that will be processed
     * @param targetField           field that will be added to the current document
     * @param ignoreMissing         flag specified to make the processor to ignore invalid documents
     */
    public VaderProcessor(VaderSentimentService vaderSentimentService, String tag, String sourceField,
                          String targetField, boolean ignoreMissing) {
        super(tag);
        this.vaderSentimentService = vaderSentimentService;
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.ignoreMissing = ignoreMissing;
    }

    /**
     * When a document has been processed and we have to add modified field to it we need to check
     * if or not the field already exists.
     * <p>
     * This field is {@link org.elasticsearch.plugin.ingest.vader.processor.VaderProcessor#targetField}
     * in this processor.
     *
     * @param document  Current document
     * @param fieldName Field name that will be checked.
     */
    private void validateTargetField(IngestDocument document, String fieldName) {
        if (document.hasField(fieldName, true)) {
            throw new IllegalArgumentException("field [" + fieldName + "] already exists");
        }
    }

    /**
     * Throw exception iff {@link org.elasticsearch.plugin.ingest.vader.processor.VaderProcessor#sourceField}
     * is absent and {@link org.elasticsearch.plugin.ingest.vader.processor.VaderProcessor#ignoreMissing} is false.
     *
     * @param document  Current document
     * @param fieldName processable field.
     */
    private void validateProcessableField(IngestDocument document, String fieldName) {
        if (!document.hasField(fieldName, true) && !this.ignoreMissing) {
            throw new IllegalArgumentException("field [" + fieldName + "] doesn't exist");
        }
    }

    /**
     * Executes the processor on the incoming document.
     *
     * @param document Current document.
     */
    @Override
    public void execute(IngestDocument document) {
        /**
         * Validate the current document.
         */
        validateProcessableField(document, this.sourceField);
        validateTargetField(document, this.targetField);

        /**
         * If correctly validated, retrieve the value of the sourceField.
         */
        Object value = document.getFieldValue(this.sourceField, Object.class);

        /**
         * Make sure that the value of sourceField is a String and then perform the
         * processing on its value.
         */
        if (value != null && value instanceof String) {
            try {
                String fullText = value.toString().trim();
                /**
                 * Perform processing only if the text length is greater than 1 character.
                 */
                if (fullText.length() > 1) {
                    Map<String, Float> polarity = this.vaderSentimentService.apply(fullText);
                    try {
                        document.setFieldValue(this.targetField, polarity);
                    } catch (Exception e) {
                        document.setFieldValue(this.sourceField, value);
                        throw e;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the type of current processor.
     *
     * @return the type of current processor
     */
    @Override
    public String getType() {
        return TYPE;
    }

    public String getSourceField() {
        return sourceField;
    }

    public String getTargetField() {
        return targetField;
    }

    boolean isIgnoreMissing() {
        return ignoreMissing;
    }

    /**
     * This class creates a factory of processors.
     */
    public static final class Factory implements Processor.Factory {
        private VaderSentimentService vaderSentimentService;

        public Factory(VaderSentimentService vaderSentimentService) {
            this.vaderSentimentService = vaderSentimentService;
        }

        /**
         * Creates a processor on the basis on the given config.
         *
         * @param processorFactories processor factories
         * @param processorTag       tag for the current processor
         * @param config             config of the current processor
         * @return an object of {@link VaderProcessor}
         * @throws Exception iff there was an error in creating an instance of a processor
         */
        @Override
        public VaderProcessor create(Map<String, Processor.Factory> processorFactories, String processorTag,
                                     Map<String, Object> config) throws Exception {
            String sourceField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "input_field");
            String targetField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "target_field");
            boolean ignoreMissing = ConfigurationUtils.readBooleanProperty(TYPE, processorTag, config, "ignore_missing", false);
            return new VaderProcessor(this.vaderSentimentService, processorTag, sourceField, targetField, ignoreMissing);
        }
    }
}
