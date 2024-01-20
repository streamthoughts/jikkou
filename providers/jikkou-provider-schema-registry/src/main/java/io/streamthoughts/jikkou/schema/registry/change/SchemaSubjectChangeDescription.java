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

import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_COMPATIBILITY_LEVEL;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
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
