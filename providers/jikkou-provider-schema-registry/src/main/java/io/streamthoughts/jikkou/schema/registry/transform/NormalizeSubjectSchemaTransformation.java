/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.data.json.Json;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasPriority;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Priority(HasPriority.HIGHEST_PRECEDENCE)
@SupportedResource(kind = "SchemaRegistrySubject")
@Enabled
public class NormalizeSubjectSchemaTransformation implements Transformation<V1SchemaRegistrySubject> {

    private static final Logger LOG = LoggerFactory.getLogger(NormalizeSubjectSchemaTransformation.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<V1SchemaRegistrySubject> transform(@NotNull V1SchemaRegistrySubject resource,
                                                                @NotNull HasItems resources,
                                                                @NotNull ReconciliationContext context) {
        V1SchemaRegistrySubjectSpec spec = resource.getSpec();

        final String value = spec.getSchema().value();
        String normalized;
        try {
            normalized = switch (spec.getSchemaType()) {
                case AVRO, JSON -> Json.normalize(value);
                case PROTOBUF -> value;
            };
        } catch (Exception e) {
            LOG.error("Failed to normalize AVRO/JSON schema. Cause: " + e.getLocalizedMessage());
            normalized = value;
        }

        return Optional.of(resource.withSpec(spec.withSchema(new SchemaHandle(normalized))));
    }
}
