/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * Determines the type of changes that can be applied on the resources to be reconciled.
 */
public enum ReconciliationMode {

    /**
     * Only changes that create new resource objects on the system will be applied.
     */
    CREATE(ChangeType.ADD),

    /**
     * Only changes that delete an existing resource objects on your system will be applied.
     */
    DELETE(ChangeType.DELETE),

    /**
     * Only changes that create or update existing resource objects on the system will be applied.
     */
    UPDATE(ChangeType.ADD, ChangeType.UPDATE),

    /**
     * Apply all reconciliation changes
     */
    FULL(ChangeType.ADD, ChangeType.UPDATE, ChangeType.DELETE);

    @JsonCreator
    public static ReconciliationMode getForNameIgnoreCase(final @Nullable String str) {
        if (str == null) throw new IllegalArgumentException("Unsupported mode 'null'");
        return Arrays.stream(ReconciliationMode.values())
                .filter(e -> e.name().equals(str.toUpperCase(Locale.ROOT)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported mode '" + str + "'"));
    }

    /**
     * Set of change-type supported for this reconciliation mode.
     */
    private final Set<ChangeType> changeTypes;

    /**
     * Creates a new {@link ReconciliationMode} instance.
     *
     * @param changeTypes {@link #changeTypes}
     */
    ReconciliationMode(ChangeType... changeTypes) {
        this(Set.of(changeTypes));
    }

    /**
     * Creates a new {@link ReconciliationMode} instance.
     *
     * @param changeTypes {@link #changeTypes}
     */
    ReconciliationMode(Set<ChangeType> changeTypes) {
        this.changeTypes = changeTypes;
    }

    /**
     * Checks whether the given change is supported by this reconciliation mode.
     *
     * @param change the change to test.
     * @return {@code true} if the change is supported, otherwise {@code false}.
     */
    public boolean isSupported(HasMetadataChange<?> change) {
        ChangeType changeType = change.getChange().operation();
        return changeType == ChangeType.NONE || changeTypes.contains(changeType);
    }
}
