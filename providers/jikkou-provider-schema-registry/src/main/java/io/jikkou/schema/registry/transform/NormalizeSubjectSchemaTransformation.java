/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.schema.registry.transform;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.Enabled;
import io.jikkou.core.annotation.Priority;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.data.SchemaHandle;
import io.jikkou.core.data.json.Json;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.models.HasItems;
import io.jikkou.core.models.HasPriority;
import io.jikkou.core.transform.Transformation;
import io.jikkou.schema.registry.SchemaRegistryExtensionProvider;
import io.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Title("Normalize subject schemas")
@Description("Normalizes Schema Registry subject schema definitions before reconciliation.")
@Priority(HasPriority.HIGHEST_PRECEDENCE)
@SupportedResource(kind = "SchemaRegistrySubject")
@Enabled
public class NormalizeSubjectSchemaTransformation implements Transformation<V1SchemaRegistrySubject> {

    private static final Logger LOG = LoggerFactory.getLogger(NormalizeSubjectSchemaTransformation.class);

    private ExtensionContext extensionContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        this.extensionContext = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<V1SchemaRegistrySubject> transform(@NotNull V1SchemaRegistrySubject resource,
                                                                @NotNull HasItems resources,
                                                                @NotNull ReconciliationContext context) {
        SchemaRegistryExtensionProvider provider = extensionContext.provider();

        if (!provider.isNormalizeSchemaEnabled()) {
            return Optional.of(resource);
        }

        V1SchemaRegistrySubjectSpec spec = resource.getSpec();

        final String value = spec.getSchema().value();
        String normalized;
        try {
            normalized = switch (spec.getSchemaType()) {
                case AVRO, JSON -> Json.normalize(value);
                case PROTOBUF -> value;
            };
        } catch (Exception e) {
            LOG.error("Failed to normalize AVRO/JSON schema.", e);
            normalized = value;
        }

        return Optional.of(resource.withSpec(spec.withSchema(new SchemaHandle(normalized))));
    }
}
