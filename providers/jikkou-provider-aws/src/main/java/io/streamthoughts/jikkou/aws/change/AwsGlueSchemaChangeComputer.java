/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws.change;

import io.streamthoughts.jikkou.aws.AwsGlueLabelsAndAnnotations;
import io.streamthoughts.jikkou.aws.models.AwsGlueSchema;
import io.streamthoughts.jikkou.core.data.SchemaAndType;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
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
import java.util.Optional;

public final class AwsGlueSchemaChangeComputer extends ResourceChangeComputer<String, AwsGlueSchema> {

    public static final String DATA_COMPATIBILITY = "compatibility";
    public static final String DATA_SCHEMA = "schema";
    public static final String DATA_FORMAT = "dataFormat";
    public static final String DATA_SCHEMA_DESCRIPTION = "description";
    public static final String EMPTY_DESCRIPTION = "";

    /**
     * Creates a new {@link AwsGlueSchemaChangeComputer} instance.
     */
    public AwsGlueSchemaChangeComputer() {
        super(object -> object.getMetadata().getName(), new AwsGlueSchemaChangeFactory());
    }

    public static class AwsGlueSchemaChangeFactory extends ResourceChangeFactory<String, AwsGlueSchema> {

        /**
         * {@inheritDoc}
         **/
        @Override
        public ResourceChange createChangeForDelete(String key, AwsGlueSchema before) {
            ResourceChangeSpecBuilder builder = ResourceChangeSpec
                .builder()
                .withOperation(Operation.DELETE)
                .withChange(StateChange.delete(DATA_SCHEMA, getSchemaAndType(before)))
                .withChange(StateChange.delete(DATA_FORMAT, before.getSpec().getDataFormat()));

            if (before.getSpec().getDescription() != null) {
                builder.withChange(StateChange.delete(DATA_SCHEMA_DESCRIPTION, before.getSpec().getDescription()));
            }

            if (before.getSpec().getCompatibility() != null) {
                builder.withChange(StateChange.delete(DATA_COMPATIBILITY, before.getSpec().getCompatibility()));
            }

            return GenericResourceChange
                .builder(AwsGlueSchema.class)
                .withMetadata(before.getMetadata())
                .withSpec(builder.build())
                .build();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ResourceChange createChangeForCreate(String key, AwsGlueSchema after) {
            ResourceChangeSpecBuilder builder = ResourceChangeSpec
                .builder()
                .withOperation(Operation.CREATE)
                .withChange(StateChange.create(DATA_SCHEMA, getSchemaAndType(after)))
                .withChange(StateChange.create(DATA_FORMAT, after.getSpec().getDataFormat()));

            if (after.getSpec().getDescription() != null) {
                builder.withChange(StateChange.create(DATA_SCHEMA_DESCRIPTION, after.getSpec().getDescription()));
            }

            if (after.getSpec().getCompatibility() != null) {
                builder.withChange(StateChange.create(DATA_COMPATIBILITY, after.getSpec().getCompatibility()));
            }

            return GenericResourceChange
                .builder(AwsGlueSchema.class)
                .withMetadata(after.getMetadata())
                .withSpec(builder.build())
                .build();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ResourceChange createChangeForUpdate(String key, AwsGlueSchema before, AwsGlueSchema after) {
            StateChangeList<StateChange> changes = StateChangeList.emptyList()
                .with(StateChange.with(DATA_COMPATIBILITY, before.getSpec().getCompatibility(), after.getSpec().getCompatibility()))
                .with(StateChange.with(DATA_FORMAT, before.getSpec().getDataFormat(), after.getSpec().getDataFormat()))
                .with(StateChange.with(DATA_SCHEMA_DESCRIPTION,
                    Optional.ofNullable(before.getSpec().getDescription()).orElse(EMPTY_DESCRIPTION),
                    Optional.ofNullable(after.getSpec().getDescription()).orElse(EMPTY_DESCRIPTION))
                )
                .with(StateChange.with(DATA_SCHEMA, getSchemaAndType(before), getSchemaAndType(after)));

            return GenericResourceChange
                .builder(AwsGlueSchema.class)
                .withMetadata(after.getMetadata())
                .withSpec(ResourceChangeSpec
                    .builder()
                    .withOperation(Change.computeOperation(changes.all()))
                    .withChanges(changes)
                    .build()
                )
                .build();
        }

        private SchemaAndType getSchemaAndType(AwsGlueSchema subject) {
            return Optional.ofNullable(subject)
                .map(AwsGlueSchema::getSpec)
                .map(spec -> new SchemaAndType(
                    spec.getSchemaDefinition().value(),
                    spec.getDataFormat(),
                    CoreAnnotations.isAnnotatedWith(subject, AwsGlueLabelsAndAnnotations.SCHEMA_REGISTRY_USE_CANONICAL_FINGERPRINT)
                ))
                .orElse(SchemaAndType.empty());
        }
    }
}
