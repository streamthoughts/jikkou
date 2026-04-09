/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import io.jikkou.core.models.Resource;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ResourceSchemaGenerator} that uses victools/jsonschema-generator
 * with a Jackson module to generate JSON Schema (Draft 2020-12) from resource classes.
 */
public class JacksonResourceSchemaGenerator implements ResourceSchemaGenerator {

    private final SchemaGenerator schemaGenerator;

    public JacksonResourceSchemaGenerator() {
        JacksonModule jacksonModule = new JacksonModule(
                JacksonOption.RESPECT_JSONPROPERTY_ORDER,
                JacksonOption.RESPECT_JSONPROPERTY_REQUIRED,
                JacksonOption.INCLUDE_ONLY_JSONPROPERTY_ANNOTATED_METHODS
        );
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(
                SchemaVersion.DRAFT_2020_12,
                OptionPreset.PLAIN_JSON
        )
                .with(jacksonModule)
                .with(
                        Option.MAP_VALUES_AS_ADDITIONAL_PROPERTIES,
                        Option.PLAIN_DEFINITION_KEYS,
                        Option.INLINE_ALL_SCHEMAS);
        SchemaGeneratorConfig config = configBuilder.build();
        this.schemaGenerator = new SchemaGenerator(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonNode generate(@NotNull Class<? extends Resource> resourceClass) {
        return schemaGenerator.generateSchema(resourceClass);
    }
}
