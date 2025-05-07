/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws.change;

import io.streamthoughts.jikkou.aws.AwsGlueAnnotations;
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
import org.jetbrains.annotations.NotNull;

public final class AwsGlueSchemaChangeComputer extends ResourceChangeComputer<String, AwsGlueSchema, ResourceChange> {

    public static final String DATA_COMPATIBILITY = "compatibility";
    public static final String DATA_SCHEMA = "schema";
    public static final String DATA_FORMAT = "dataFormat";
    public static final String DATA_SCHEMA_DESCRIPTION = "description";

    /**
     * Creates a new {@link AwsGlueSchemaChangeComputer} instance.
     */
    public AwsGlueSchemaChangeComputer() {
        super(object -> object.getMetadata().getName(), new AwsGlueSchemaChangeFactory());
    }

    public static class AwsGlueSchemaChangeFactory extends ResourceChangeFactory<String, AwsGlueSchema, ResourceChange> {

        /**
         * {@inheritDoc}
         **/
        @Override
        public ResourceChange createChangeForDelete(String key, AwsGlueSchema before) {
            return GenericResourceChange
                .builder(AwsGlueSchema.class)
                .withMetadata(before.getMetadata())
                .withSpec(ResourceChangeSpec
                    .builder()
                    .withOperation(Operation.DELETE)
                    .build()
                )
                .build();
        }

        @Override
        public ResourceChange createChangeForCreate(String key, AwsGlueSchema after) {
            ResourceChangeSpecBuilder builder = ResourceChangeSpec
                .builder()
                .withOperation(Operation.CREATE)
                .withChange(StateChange.create(DATA_SCHEMA, getSchemaAndType(after)))
                .withChange(StateChange.create(DATA_FORMAT, after.getSpec().getDataFormat()))
                .withChange(StateChange.create(DATA_SCHEMA_DESCRIPTION, after.getSpec().getDescription()));

            if (after.getSpec().getCompatibility() != null) {
                builder.withChange(StateChange.create(DATA_COMPATIBILITY, after.getSpec().getCompatibility()));
            }

            return GenericResourceChange
                .builder(AwsGlueSchema.class)
                .withMetadata(after.getMetadata())
                .withSpec(builder.build())
                .build();
        }

        @Override
        public ResourceChange createChangeForUpdate(String key, AwsGlueSchema before, AwsGlueSchema after) {
            StateChangeList<StateChange> changes = StateChangeList.emptyList()
                .with(getChangeForCompatibility(before, after))
                .with(getChangeForSchema(before, after))
                .with(getChangeForSchemaType(before, after));

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

        @NotNull
        private StateChange getChangeForSchemaType(AwsGlueSchema before,
                                                   AwsGlueSchema after) {
            return StateChange.with(
                DATA_FORMAT,
                Optional.ofNullable(before).map(o -> o.getSpec().getDataFormat()).orElse(null),
                Optional.ofNullable(after).map(o -> o.getSpec().getDataFormat()).orElse(null)
            );
        }

        @NotNull
        private StateChange getChangeForCompatibility(AwsGlueSchema before,
                                                      AwsGlueSchema after) {
            return StateChange.with(
                DATA_COMPATIBILITY,
                Optional.ofNullable(before).map(o -> o.getSpec().getCompatibility()).orElse(null),
                Optional.ofNullable(after).map(o -> o.getSpec().getCompatibility()).orElse(null)
            );
        }

        @NotNull
        private StateChange getChangeForSchema(AwsGlueSchema before,
                                               AwsGlueSchema after) {

            SchemaAndType beforeSchema = getSchemaAndType(before);
            SchemaAndType afterSchema = getSchemaAndType(after);

            return StateChange.with(DATA_SCHEMA, beforeSchema, afterSchema);
        }

        private SchemaAndType getSchemaAndType(AwsGlueSchema subject) {
            return Optional.ofNullable(subject)
                .map(AwsGlueSchema::getSpec)
                .map(spec -> new SchemaAndType(
                    spec.getSchemaDefinition().value(),
                    spec.getDataFormat(),
                    CoreAnnotations.isAnnotatedWith(subject, AwsGlueAnnotations.SCHEMA_REGISTRY_USE_CANONICAL_FINGERPRINT)
                ))
                .orElse(SchemaAndType.empty());
        }
    }
}
