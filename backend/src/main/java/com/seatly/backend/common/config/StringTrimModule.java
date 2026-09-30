package com.seatly.backend.common.config;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.module.SimpleModule;

// Trims every JSON string; as a JacksonModule bean, Boot registers it automatically.
@Component
public class StringTrimModule extends SimpleModule {

    public StringTrimModule() {
        super();
        addDeserializer(String.class, new TrimmingStringDeserializer());
    }

    private static final class TrimmingStringDeserializer extends StdDeserializer<String> {

        private TrimmingStringDeserializer() {
            super(String.class);
        }

        @Override
        public String deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
            String value = parser.getValueAsString();
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }
}
