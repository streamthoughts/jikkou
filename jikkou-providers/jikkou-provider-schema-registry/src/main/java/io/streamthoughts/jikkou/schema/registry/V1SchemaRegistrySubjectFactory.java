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
package io.streamthoughts.jikkou.schema.registry;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaVersion;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.SchemaRegistry;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import io.streamthoughts.jikkou.schema.registry.reconcilier.internals.SchemaSubjectPrettyPrinter;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class V1SchemaRegistrySubjectFactory {

    private final String schemaRegistryVendor;
    private final String schemaRegistryUrl;

    private final boolean prettyPrintSchema;

    public V1SchemaRegistrySubjectFactory(String schemaRegistryVendor,
                                          String schemaRegistryUrl,
                                          boolean prettyPrintSchema) {
        this.schemaRegistryVendor = schemaRegistryVendor;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.prettyPrintSchema = prettyPrintSchema;
    }

    @NotNull
    public V1SchemaRegistrySubject createSchemaRegistrySubject(@NotNull SubjectSchemaVersion subjectSchema,
                                                               @Nullable CompatibilityLevels compatibilityLevels) {
        SchemaType schemaType = Optional.ofNullable(subjectSchema.schemaType())
                .map(SchemaType::getForNameIgnoreCase)
                .orElse(SchemaType.defaultType());

        V1SchemaRegistrySubjectSpec.V1SchemaRegistrySubjectSpecBuilder specBuilder = V1SchemaRegistrySubjectSpec
                .builder()
                .withSchemaRegistry(SchemaRegistry
                        .builder()
                        .withVendor(schemaRegistryVendor)
                        .build()
                )
                .withSchemaType(schemaType)
                .withSchema(new SchemaHandle(subjectSchema.schema()));

        if (compatibilityLevels != null) {
            specBuilder = specBuilder.withCompatibilityLevel(compatibilityLevels);
        }

        V1SchemaRegistrySubject res = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(subjectSchema.subject())
                        .withAnnotation(SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_URL,
                                schemaRegistryUrl)
                        .withAnnotation(SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_VERSION,
                                subjectSchema.version())
                        .withAnnotation(SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_ID,
                                subjectSchema.id())
                        .build()
                )
                .withSpec(specBuilder.build())
                .build();

        if (prettyPrintSchema) {
            return SchemaSubjectPrettyPrinter.prettyPrintSchema(res);
        }
        return res;
    }
}
