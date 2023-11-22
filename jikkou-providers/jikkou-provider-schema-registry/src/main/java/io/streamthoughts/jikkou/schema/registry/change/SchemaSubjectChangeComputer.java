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
package io.streamthoughts.jikkou.schema.registry.change;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.reconcilier.Change;
import io.streamthoughts.jikkou.core.reconcilier.ChangeComputer;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.core.reconcilier.change.JsonValueChange;
import io.streamthoughts.jikkou.core.reconcilier.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryAnnotations;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaReference;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.SchemaAndType;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SchemaSubjectChangeComputer extends ResourceChangeComputer<V1SchemaRegistrySubject, V1SchemaRegistrySubject, SchemaSubjectChange>

        implements ChangeComputer<V1SchemaRegistrySubject, SchemaSubjectChange> {


    /**
     * Creates a new {@link SchemaSubjectChangeComputer} instance.
     */
    public SchemaSubjectChangeComputer() {
        super(metadataNameKeyMapper(), identityChangeValueMapper(), false);
    }

    /** {@inheritDoc} **/
    @Override
    public List<SchemaSubjectChange> buildChangeForDeleting(V1SchemaRegistrySubject before) {
        SchemaSubjectChange change = SchemaSubjectChange.builder()
                .withSubject(before.getMetadata().getName())
                .withChangeType(ChangeType.DELETE)
                .withOptions(getOptions(before))
                .build();
        return List.of(change);
    }
    /** {@inheritDoc} **/
    @Override
    public List<SchemaSubjectChange> buildChangeForUpdating(V1SchemaRegistrySubject before, V1SchemaRegistrySubject after) {
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

        SchemaSubjectChange change = SchemaSubjectChange.builder()
                .withSubject(before.getMetadata().getName())
                .withChangeType(changeType)
                .withSchemaType(changeForSchemaType)
                .withSchema(changeForSchema)
                .withReferences(changeForReferences)
                .withCompatibilityLevels(changeForCompatibility)
                .withOptions(getOptions(after))
                .build();

        return List.of(change);
    }

    /** {@inheritDoc} **/
    @Override
    public List<SchemaSubjectChange> buildChangeForNone(V1SchemaRegistrySubject before, V1SchemaRegistrySubject after) {
        return null;
    }
    /** {@inheritDoc} **/
    @Override
    public List<SchemaSubjectChange> buildChangeForCreating(V1SchemaRegistrySubject after) {
        V1SchemaRegistrySubjectSpec spec = after.getSpec();

        ValueChange<CompatibilityLevels> compatibilityLevels = Optional
                .ofNullable(spec.getCompatibilityLevel())
                .map(ValueChange::withAfterValue)
                .orElse(null);

        SchemaSubjectChange change = SchemaSubjectChange.builder()
                .withChangeType(ChangeType.ADD)
                .withSubject(after.getMetadata().getName())
                .withSchemaType(ValueChange.withAfterValue(spec.getSchemaType()))
                .withSchema(getChangeForSchema(null, after))
                .withReferences(ValueChange.withAfterValue(spec.getReferences()))
                .withCompatibilityLevels(compatibilityLevels)
                .withOptions(getOptions(after))
                .build();

        return List.of(change);
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

        return ValueChange.with(before.getSpec().getReferences(), after.getSpec().getReferences());
    }

    @NotNull
    private ValueChange<SchemaType> getChangeForSchemaType(
            @NotNull V1SchemaRegistrySubject before, @NotNull V1SchemaRegistrySubject after) {

        return ValueChange.with(before.getSpec().getSchemaType(), after.getSpec().getSchemaType());
    }


    @NotNull
    private ValueChange<CompatibilityLevels> getChangeForCompatibility(
            @NotNull V1SchemaRegistrySubject before, @NotNull V1SchemaRegistrySubject after) {

        return ValueChange.with(before.getSpec().getCompatibilityLevel(), after.getSpec().getCompatibilityLevel());
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
                    .with(beforeSchemaAndType.schema(), afterSchemaAndType.schema());
            case PROTOBUF -> ValueChange
                    .with(beforeSchemaAndType.schema(), afterSchemaAndType.schema());
            case INVALID -> throw new JikkouRuntimeException("unsupported schema type");
        };
    }
}
