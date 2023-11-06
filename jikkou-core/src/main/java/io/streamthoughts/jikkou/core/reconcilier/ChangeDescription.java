/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.core.reconcilier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.DefaultChangeDescription;
import java.io.IOException;

/**
 * Simple interface to get a human-readable description of an executed operation.
 */
@FunctionalInterface
@Reflectable
@JsonDeserialize(using = ChangeDescription.Deserializer.class)
@JsonSerialize(using = ChangeDescription.Serializer.class)
public interface ChangeDescription {

    /**
     * @return a textual description of the operation.
     */
    String textual();

    static String humanize(final ChangeType type) {
        var str = type.equals(ChangeType.NONE) ? "unchanged" : type.name().toLowerCase();
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * JsonSerializer for {@link ChangeDescription}.
     */
    class Serializer extends JsonSerializer<ChangeDescription> {
        /** {@inheritDoc} **/
        @Override
        public void serialize(ChangeDescription value,
                              JsonGenerator gen,
                              SerializerProvider serializers) throws IOException {
            gen.writeString(value.textual());
        }
    }

    /**
     * JsonDeserializer for {@link ChangeDescription}.
     */
    class Deserializer extends JsonDeserializer<ChangeDescription> {
        /** {@inheritDoc} **/
        @Override
        public ChangeDescription deserialize(JsonParser p,
                                             DeserializationContext context) throws IOException {
            return new DefaultChangeDescription(p.getValueAsString());
        }
    }

}
