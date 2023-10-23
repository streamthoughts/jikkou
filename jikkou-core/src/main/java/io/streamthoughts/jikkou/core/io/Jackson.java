/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.core.io;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public interface Jackson {

    SimpleModule NULL_COLLECTIONS_AS_EMPTY = new SimpleModule()
            .setDeserializerModifier(new NullCollectionsAsEmptyModifier());

    ObjectMapper YAML_OBJECT_MAPPER = YAMLMapper.builder()
            .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
            .addModules(new Jdk8Module(), new VavrModule(), new JavaTimeModule(), NULL_COLLECTIONS_AS_EMPTY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET,false)
            .serializationInclusion(JsonInclude.Include.NON_ABSENT)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .build();

    ObjectMapper JSON_OBJECT_MAPPER = JsonMapper.builder()
            .addModules(new Jdk8Module(), new VavrModule(), new JavaTimeModule(), NULL_COLLECTIONS_AS_EMPTY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET,false)
            .serializationInclusion(JsonInclude.Include.NON_ABSENT)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .build();


    abstract class ContextualJsonDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer { }

    class NullCollectionsAsEmptyModifier extends BeanDeserializerModifier {
        @Override
        public JsonDeserializer<?> modifyMapDeserializer(DeserializationConfig config,
                                                         MapType type,
                                                         BeanDescription beanDesc,
                                                         JsonDeserializer<?> deserializer) {
            return new ContextualJsonDeserializer() {
                @Override
                public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
                    return modifyMapDeserializer(config, type, beanDesc, ((ContextualDeserializer) deserializer).createContextual(ctxt, property));
                }

                @Override
                public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    return deserializer.deserialize(p, ctxt);
                }

                @Override
                public Object getNullValue(DeserializationContext ctxt) {
                    return Collections.emptyMap();
                }
            };
        }

        @Override
        public JsonDeserializer<?> modifyCollectionDeserializer(DeserializationConfig config,
                                                                CollectionType type,
                                                                BeanDescription beanDesc,
                                                                JsonDeserializer<?> deserializer) {
            return new ContextualJsonDeserializer() {
                @Override
                public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
                    return modifyCollectionDeserializer(config, type, beanDesc, ((ContextualDeserializer) deserializer)
                            .createContextual(ctxt, property)
                    );
                }

                @Override
                public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    return deserializer.deserialize(p, ctxt);
                }

                @Override
                public Object getNullValue(DeserializationContext ctxt) {
                    return Set.class.isAssignableFrom(type.getRawClass()) ?
                            Collections.emptySet() : Collections.emptyList();
                }
            };
        }
    }
}
