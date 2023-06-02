/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.schema.registry.change;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.control.Change;
import io.streamthoughts.jikkou.api.control.ChangeComputer;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.JsonValueChange;
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryAnnotations;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaReference;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.SchemaAndType;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SchemaSubjectChangeComputer implements ChangeComputer<V1SchemaRegistrySubject, SchemaSubjectChange> {


    /**
     * Creates a new {@link SchemaSubjectChangeComputer} instance.
     */
    public SchemaSubjectChangeComputer() {
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<SchemaSubjectChange> computeChanges(Iterable<V1SchemaRegistrySubject> actualStates,
                                                    Iterable<V1SchemaRegistrySubject> expectedStates) {

        Map<String, V1SchemaRegistrySubject> actualStateGroupBySubject = StreamSupport
                .stream(actualStates.spliterator(), false)
                .collect(Collectors.toMap(it -> it.getMetadata().getName(), it -> it));

        Map<String, V1SchemaRegistrySubject> expectedStateGroupBySubject = StreamSupport
                .stream(expectedStates.spliterator(), false)
                .collect(Collectors.toMap(it -> it.getMetadata().getName(), it -> it));

        return expectedStateGroupBySubject.entrySet()
                .stream()
                .flatMap(entry -> {
                    V1SchemaRegistrySubject before = actualStateGroupBySubject.get(entry.getKey());
                    V1SchemaRegistrySubject after = entry.getValue();
                    return getSchemaSubjectChange(before, after).stream();
                })
                .collect(Collectors.toList());
    }

    @NotNull
    private Optional<SchemaSubjectChange> getSchemaSubjectChange(V1SchemaRegistrySubject before,
                                                                 V1SchemaRegistrySubject after) {

        boolean isAnnotatedWithDelete = JikkouMetadataAnnotations.isAnnotatedWithDelete(after);
        if (before != null && isAnnotatedWithDelete) {
            return Optional.of(getChangeForDeleteSubject(after));
        }

        if (before != null) {
            return Optional.of(getChangeForExistingSubject(before, after));
        }

        if (!isAnnotatedWithDelete) {
            return Optional.of(getChangeForNewSubject(after));
        }

        return Optional.empty();
    }

    private SchemaSubjectChange getChangeForDeleteSubject(@NotNull final V1SchemaRegistrySubject resource) {
        return SchemaSubjectChange.builder()
                .withSubject(resource.getMetadata().getName())
                .withChangeType(ChangeType.DELETE)
                .withOptions(getOptions(resource))
                .build();
    }

    private SchemaSubjectChange getChangeForExistingSubject(V1SchemaRegistrySubject before,
                                                            V1SchemaRegistrySubject after) {
        // Compute change for Schema
        var changeForSchema = getChangeForSchema(
                before, after);

        // Compute change for Compatibility
        var changeForCompatibility = getChangeForCompatibility(
                before, after);

        // Compute change for References
        var changeForReferences =
                getChangeForReferences(before, after);

        // Compute change for Schema Type
        var changeForSchemaType = getChangeForSchemaType(
                before, after);

        var changeType = Change.computeChangeTypeFrom(
                changeForSchema, changeForCompatibility, changeForReferences, changeForSchemaType);

        return SchemaSubjectChange.builder()
                .withSubject(before.getMetadata().getName())
                .withChangeType(changeType)
                .withSchemaType(changeForSchemaType)
                .withSchema(changeForSchema)
                .withReferences(changeForReferences)
                .withCompatibilityLevels(changeForCompatibility)
                .withOptions(getOptions(after))
                .build();
    }

    private SchemaSubjectChange getChangeForNewSubject(V1SchemaRegistrySubject resource) {
        V1SchemaRegistrySubjectSpec spec = resource.getSpec();

        ValueChange<CompatibilityLevels> compatibilityLevels = Optional
                .ofNullable(spec.getCompatibilityLevel())
                .map(ValueChange::withAfterValue)
                .orElse(null);

        return SchemaSubjectChange.builder()
                .withChangeType(ChangeType.ADD)
                .withSubject(resource.getMetadata().getName())
                .withSchemaType(ValueChange.withAfterValue(spec.getSchemaType()))
                .withSchema(getChangeForSchema(null, resource))
                .withReferences(ValueChange.withAfterValue(spec.getReferences()))
                .withCompatibilityLevels(compatibilityLevels)
                .withOptions(getOptions(resource))
                .build();
    }

    @NotNull
    private SchemaSubjectChangeOptions getOptions(@NotNull V1SchemaRegistrySubject subject) {
        return new SchemaSubjectChangeOptions()
                .withPermanentDeleteEnabled(SchemaRegistryAnnotations.isAnnotatedWitPermananteDelete(subject))
                .withSchemaOptimizationEnabled(SchemaRegistryAnnotations.isAnnotatedWithNormalizeSchema(subject));
    }

    @NotNull
    private ValueChange<List<SubjectSchemaReference>> getChangeForReferences(
            @NotNull V1SchemaRegistrySubject before, @NotNull V1SchemaRegistrySubject after) {

        return ValueChange.with(after.getSpec().getReferences(), before.getSpec().getReferences());
    }

    @NotNull
    private ValueChange<SchemaType> getChangeForSchemaType(
            @NotNull V1SchemaRegistrySubject before, @NotNull V1SchemaRegistrySubject after) {

        return ValueChange.with(after.getSpec().getSchemaType(), before.getSpec().getSchemaType());
    }


    @NotNull
    private ValueChange<CompatibilityLevels> getChangeForCompatibility(
            @NotNull V1SchemaRegistrySubject before, @NotNull V1SchemaRegistrySubject after) {

        return ValueChange.with(after.getSpec().getCompatibilityLevel(), before.getSpec().getCompatibilityLevel());
    }

    @NotNull
    private ValueChange<String> getChangeForSchema(
            @Nullable V1SchemaRegistrySubject before, @NotNull V1SchemaRegistrySubject after) {

        SchemaAndType beforeSchemaAndType = Optional.ofNullable(before)
                .map(V1SchemaRegistrySubject::getSpec)
                .map(spec -> new SchemaAndType(spec.getSchema().value(), spec.getSchemaType()))
                .orElse(SchemaAndType.empty());

        SchemaAndType afterSchemaAndType = new SchemaAndType(
                after.getSpec().getSchema().value(),
                after.getSpec().getSchemaType()
        );

        return switch (afterSchemaAndType.type()) {
            case AVRO, JSON -> JsonValueChange
                    .with(afterSchemaAndType.schema(), beforeSchemaAndType.schema());
            case PROTOBUF -> ValueChange
                    .with(afterSchemaAndType.schema(), beforeSchemaAndType.schema());
            case INVALID -> throw new JikkouRuntimeException("unsupported schema type");
        };
    }
}
