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
package io.streamthoughts.jikkou.schema.registry.transform;

import io.streamthoughts.jikkou.common.utils.Json;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.HandledResource;
import io.streamthoughts.jikkou.core.annotation.Priority;
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
@HandledResource(kind = "SchemaRegistrySubject")
@Enabled
public class NormalizeSubjectSchemaTransformation implements Transformation<V1SchemaRegistrySubject> {

    private static final Logger LOG = LoggerFactory.getLogger(NormalizeSubjectSchemaTransformation.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<V1SchemaRegistrySubject> transform(@NotNull V1SchemaRegistrySubject resource,
                                                                @NotNull HasItems resources) {
        V1SchemaRegistrySubjectSpec spec = resource.getSpec();

        final String value = spec.getSchema().value();
        String normalized;
        try {
            normalized = switch (spec.getSchemaType()) {
                case AVRO, JSON -> Json.normalize(value);
                case PROTOBUF, INVALID -> value;
            };
        } catch (Exception e) {
            LOG.error("Failed to normalize AVRO/JSON schema. Cause: " + e.getLocalizedMessage());
            normalized = value;
        }

        return Optional.of(resource.withSpec(spec.withSchema(new SchemaHandle(normalized))));
    }
}
