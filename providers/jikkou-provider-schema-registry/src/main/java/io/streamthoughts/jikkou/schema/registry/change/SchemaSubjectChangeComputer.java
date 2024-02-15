/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.change;

import com.fasterxml.jackson.core.type.TypeReference;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.data.converter.ObjectTypeConverter;
import io.streamthoughts.jikkou.core.data.json.Json;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpecBuilder;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.models.change.StateChangeList;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryAnnotations;
import io.streamthoughts.jikkou.schema.registry.model.SchemaAndType;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public final class SchemaSubjectChangeComputer extends ResourceChangeComputer<String, V1SchemaRegistrySubject, ResourceChange> {

    public static final String DATA_COMPATIBILITY_LEVEL = "compatibilityLevel";
    public static final String DATA_SCHEMA = "schema";
    public static final String DATA_SCHEMA_TYPE = "schemaType";
    public static final String DATA_REFERENCES = "references";
    public static final TypeConverter<Map<String, Object>> TYPE_CONVERTER = ObjectTypeConverter.newForType(new TypeReference<>() {
    });

    /**
     * Creates a new {@link SchemaSubjectChangeComputer} instance.
     */
    public SchemaSubjectChangeComputer() {
        super(object -> object.getMetadata().getName(), new SchemaSubjectChangeFactory());
    }

    public static class SchemaSubjectChangeFactory extends ResourceChangeFactory<String, V1SchemaRegistrySubject, ResourceChange> {

        /**
         * {@inheritDoc}
         **/
        @Override
        public ResourceChange createChangeForDelete(String key, V1SchemaRegistrySubject before) {
            return GenericResourceChange
                    .builder(V1SchemaRegistrySubject.class)
                    .withMetadata(before.getMetadata())
                    .withSpec(ResourceChangeSpec
                            .builder()
                            .withData(TYPE_CONVERTER.convertValue(getOptions(before)))
                            .withOperation(Operation.DELETE)
                            .build()
                    )
                    .build();
        }

        @Override
        public ResourceChange createChangeForCreate(String key, V1SchemaRegistrySubject after) {
            ResourceChangeSpecBuilder specBuilder = ResourceChangeSpec
                .builder()
                .withData(TYPE_CONVERTER.convertValue(getOptions(after)))
                .withOperation(Operation.CREATE)
                .withChange(StateChange.create(DATA_SCHEMA, after.getSpec().getSchema().value()))
                .withChange(StateChange.create(DATA_SCHEMA_TYPE, after.getSpec().getSchemaType()))
                .withChange(StateChange.create(DATA_REFERENCES, after.getSpec().getReferences()));

            if (after.getSpec().getCompatibilityLevel() != null) {
                specBuilder = specBuilder.withChange(
                    StateChange.create(DATA_COMPATIBILITY_LEVEL, after.getSpec().getCompatibilityLevel()));
            }

            return GenericResourceChange
                    .builder(V1SchemaRegistrySubject.class)
                    .withMetadata(after.getMetadata())
                    .withSpec(specBuilder.build())
                    .build();
        }

        @Override
        public ResourceChange createChangeForUpdate(String key, V1SchemaRegistrySubject before, V1SchemaRegistrySubject after) {
            StateChangeList<StateChange> changes = StateChangeList.emptyList()
                    .with(getChangeForCompatibility(before, after))
                    .with(getChangeForSchema(before, after))
                    .with(getChangeForSchemaType(before, after))
                    .with(getChangeForReferences(before, after));

            return GenericResourceChange
                    .builder(V1SchemaRegistrySubject.class)
                    .withMetadata(after.getMetadata())
                    .withSpec(ResourceChangeSpec
                            .builder()
                            .withData(TYPE_CONVERTER.convertValue(getOptions(after)))
                            .withOperation(Change.computeOperation(changes.all()))
                            .withChanges(changes)
                            .build()
                    )
                    .build();
        }

        @NotNull
        private SchemaSubjectChangeOptions getOptions(@NotNull V1SchemaRegistrySubject subject) {
            return new SchemaSubjectChangeOptions(
                    SchemaRegistryAnnotations.isAnnotatedWitPermananteDelete(subject),
                    SchemaRegistryAnnotations.isAnnotatedWithNormalizeSchema(subject)
            );
        }

        @NotNull
        private StateChange getChangeForReferences(
                @NotNull V1SchemaRegistrySubject before, @NotNull V1SchemaRegistrySubject after) {

            return StateChange.with(
                    DATA_REFERENCES,
                    before.getSpec().getReferences()
                            .stream()
                            .map(TYPE_CONVERTER::convertValue)
                            .toList(),
                    after.getSpec().getReferences()
                            .stream()
                            .map(TYPE_CONVERTER::convertValue)
                            .toList()
            );
        }

        @NotNull
        private StateChange getChangeForSchemaType(V1SchemaRegistrySubject before,
                                                   V1SchemaRegistrySubject after) {
            return StateChange.with(
                    DATA_SCHEMA_TYPE,
                    Optional.ofNullable(before).map(o -> o.getSpec().getSchemaType()).orElse(null),
                    Optional.ofNullable(after).map(o -> o.getSpec().getSchemaType()).orElse(null)
            );
        }


        @NotNull
        private StateChange getChangeForCompatibility(V1SchemaRegistrySubject before,
                                                      V1SchemaRegistrySubject after) {
            return StateChange.with(
                    DATA_COMPATIBILITY_LEVEL,
                    Optional.ofNullable(before).map(o -> o.getSpec().getCompatibilityLevel()).orElse(null),
                    Optional.ofNullable(after).map(o -> o.getSpec().getCompatibilityLevel()).orElse(null)
            );
        }

        @NotNull
        private StateChange getChangeForSchema(V1SchemaRegistrySubject before,
                                               V1SchemaRegistrySubject after) {

            SchemaAndType beforeSchema = Optional.ofNullable(before)
                    .map(V1SchemaRegistrySubject::getSpec)
                    .map(spec -> new SchemaAndType(spec.getSchema().value(), spec.getSchemaType()))
                    .orElse(SchemaAndType.empty());

            SchemaAndType afterSchema = Optional.ofNullable(after)
                    .map(V1SchemaRegistrySubject::getSpec)
                    .map(spec -> new SchemaAndType(spec.getSchema().value(), spec.getSchemaType()))
                    .orElse(SchemaAndType.empty());

            return StateChange.with(DATA_SCHEMA,
                    isJsonBasedSchema(beforeSchema) ? Json.normalize(beforeSchema.schema()) : beforeSchema.schema(),
                    isJsonBasedSchema(afterSchema) ? Json.normalize(afterSchema.schema()) : afterSchema.schema()
            );
        }

        private boolean isJsonBasedSchema(SchemaAndType schemaAndType) {
            return schemaAndType.type() == SchemaType.JSON || schemaAndType.type() == SchemaType.AVRO;
        }
    }
}
