/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.schema.registry.change;

import static io.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_COMPATIBILITY_LEVEL;

import io.jikkou.core.data.TypeConverter;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.SpecificStateChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import io.jikkou.schema.registry.model.CompatibilityLevels;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class SchemaSubjectChangeDescription implements TextDescription {

    private final ResourceChange change;

    public SchemaSubjectChangeDescription(final @NotNull ResourceChange change) {
        this.change = Objects.requireNonNull(change, "change cannot be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String textual() {
        final String subject = change.getMetadata().getName();

        String compatibilityLevel = change.getSpec().getChanges()
                .findLast(DATA_COMPATIBILITY_LEVEL, TypeConverter.of(CompatibilityLevels.class))
                .map(SpecificStateChange::getAfter)
                .map(Enum::name)
                .orElse("<global>");


        final Operation op = change.getSpec().getOp();
        return String.format("%s subject '%s' (type=%s, compatibilityLevel=%s)",
                op.humanize(),
                subject,
                op,
                compatibilityLevel
        );
    }
}
