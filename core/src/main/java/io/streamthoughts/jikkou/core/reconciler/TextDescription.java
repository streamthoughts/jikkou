/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.change.DefaultTextDescription;
import java.io.IOException;

/**
 * Simple interface to get a human-readable description of an executed operation.
 */
@FunctionalInterface
@Reflectable
@JsonDeserialize(using = TextDescription.Deserializer.class)
@JsonSerialize(using = TextDescription.Serializer.class)
public interface TextDescription {

    /**
     * @return a textual description.
     */
    String textual();

    /**
     * JsonSerializer for {@link TextDescription}.
     */
    class Serializer extends JsonSerializer<TextDescription> {
        /**
         * {@inheritDoc}
         **/
        @Override
        public void serialize(TextDescription value,
                              JsonGenerator gen,
                              SerializerProvider serializers) throws IOException {
            gen.writeString(value.textual());
        }
    }

    /**
     * JsonDeserializer for {@link TextDescription}.
     */
    class Deserializer extends JsonDeserializer<TextDescription> {
        /**
         * {@inheritDoc}
         **/
        @Override
        public TextDescription deserialize(JsonParser p,
                                           DeserializationContext context) throws IOException {
            return new DefaultTextDescription(p.getValueAsString());
        }
    }

}
