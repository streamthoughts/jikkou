/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws.change;

import static io.streamthoughts.jikkou.aws.change.AwsGlueSchemaChangeComputer.DATA_COMPATIBILITY;
import static io.streamthoughts.jikkou.aws.change.AwsGlueSchemaChangeComputer.DATA_FORMAT;

import io.streamthoughts.jikkou.aws.AwsGlueLabelsAndAnnotations;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.glue.model.DataFormat;

public final class AwsGlueSchemaChangeDescription implements TextDescription {

    private final ResourceChange change;

    public AwsGlueSchemaChangeDescription(final @NotNull ResourceChange change) {
        this.change = Objects.requireNonNull(change, "change cannot be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String textual() {
        final String schemaName = change.getMetadata().getName();
        final String registryName = (String) change.getMetadata()
            .getLabelByKey(AwsGlueLabelsAndAnnotations.SCHEMA_REGISTRY_NAME)
            .getValue();

        SpecificStateChange<String> compatibility = change.getSpec()
            .getChanges()
            .getLast(DATA_COMPATIBILITY, TypeConverter.String());

        SpecificStateChange<DataFormat> dataFormat = change.getSpec()
            .getChanges()
            .getLast(DATA_FORMAT, TypeConverter.of(DataFormat.class));
        
        final Operation op = change.getSpec().getOp();
        return "%s schema '%s' (registryName=%s, dataFormat=%s, compatibility=%s)".formatted(
            op.humanize(),
            schemaName,
            registryName,
            op.isUpdateOrCreate() ? dataFormat.getAfter().name() : dataFormat.getBefore().name(),
            op.isUpdateOrCreate() ? compatibility.getAfter() : compatibility.getBefore()
        );
    }
}
