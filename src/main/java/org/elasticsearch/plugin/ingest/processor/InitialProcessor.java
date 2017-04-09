package org.elasticsearch.plugin.ingest.processor;

import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.ConfigurationUtils;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.util.Map;

/**
 * @author Animesh Pandey
 *         Created on 4/2/2017.
 */
public final class InitialProcessor extends AbstractProcessor {
    public static final String TYPE = "initial";

    private final String field;
    private final String targetField;
    private final String lengthField;
    private final boolean ignoreMissing;

    InitialProcessor(String tag, String field, String targetField, String lengthField, boolean ignoreMissing) {
        super(tag);
        this.field = field;
        this.targetField = targetField;
        this.lengthField = lengthField;
        this.ignoreMissing = ignoreMissing;
    }

    String getField() {
        return field;
    }

    String getTargetField() {
        return targetField;
    }

    String getLengthField() {
        return lengthField;
    }

    boolean isIgnoreMissing() {
        return ignoreMissing;
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
        // We fail here if the target field point to an array slot that is out of range.
        // If we didn't do this then we would fail if we set the value in the target_field
        // and then on failure processors would not see that value we tried to rename as we already
        // removed it.
        if (document.hasField(targetField, true)) {
            throw new IllegalArgumentException("field [" + targetField + "] already exists");
        }
        if (document.hasField(lengthField, true)) {
            throw new IllegalArgumentException("field [" + lengthField + "] already exists");
        }

        Object value = document.getFieldValue(field, Object.class);
        if (value != null && value instanceof String) {
            String fullInput = value.toString().trim();
            if (fullInput.length() > 1) {
                String[] names = fullInput.split("\\s+");
                int length = names.length;
                if (length < 2) {
                    throw new IllegalArgumentException("field [" + field + "] should have a full name");
                }
                String firstName = names[0];
                String lastName = names[length - 1];
                try {
                    String fullInitials = firstName.substring(0, 1).toUpperCase() + '.' +
                            lastName.substring(0, 1).toUpperCase() + '.';
                    document.setFieldValue(targetField, fullInitials);
                    document.setFieldValue(lengthField, length);
                } catch (Exception e) {
                    // setting the value back to the original field shouldn't as we just fetched it from that field:
                    document.setFieldValue(field, value);
                    throw e;
                }
            }
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {
        @Override
        public InitialProcessor create(Map<String, Processor.Factory> registry, String processorTag, Map<String, Object> config) throws Exception {
            String field = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "field");
            String targetField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "target_field");
            String lengthField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "length_field");
            boolean ignoreMissing = ConfigurationUtils.readBooleanProperty(TYPE, processorTag, config, "ignore_missing", false);
            return new InitialProcessor(processorTag, field, targetField, lengthField, ignoreMissing);
        }
    }
}
