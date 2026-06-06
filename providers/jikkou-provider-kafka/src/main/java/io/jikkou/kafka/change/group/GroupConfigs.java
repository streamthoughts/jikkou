/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.change.group;

import static io.jikkou.core.reconciler.Operation.DELETE;

import io.jikkou.core.models.ConfigValue;
import io.jikkou.core.models.Configs;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.change.ChangeComputer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

/**
 * Shared helper for diffing dynamic configurations of a Kafka GROUP resource
 * (consumer groups and share groups). Mirrors the topic config-diff logic but
 * is config-only and emits config entries prefixed with {@link #CONFIG_PREFIX}.
 */
public final class GroupConfigs {

    public static final String CONFIG_PREFIX = "config.";

    private GroupConfigs() {
    }

    /**
     * Computes the prefixed config {@link StateChange}s between two config sets.
     *
     * @param before        the actual configs, or {@code null}.
     * @param after         the desired configs, or {@code null}.
     * @param deleteOrphans {@code true} to emit DELETE for configs present in {@code before} but not {@code after}.
     * @return the list of prefixed config changes.
     */
    public static List<StateChange> getConfigChanges(@Nullable Configs before,
                                                     @Nullable Configs after,
                                                     boolean deleteOrphans) {
        List<StateChange> changes = configEntryChangeComputer(deleteOrphans).computeChanges(before, after);
        return changes.stream()
            .map(change -> StateChange.builder()
                .withName(CONFIG_PREFIX + change.getName())
                .withOp(change.getOp())
                .withBefore(change.getBefore())
                .withAfter(change.getAfter())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Returns the core config-entry change computer, identical to the one used for topics.
     *
     * @param deleteOrphans {@code true} to emit DELETE for orphaned config entries.
     * @return the {@link ChangeComputer}.
     */
    public static ChangeComputer<ConfigValue, StateChange> configEntryChangeComputer(boolean deleteOrphans) {
        return ChangeComputer
            .<String, ConfigValue, StateChange>builder()
            .withDeleteOrphans(deleteOrphans)
            .withKeyMapper(ConfigValue::name)
            .withChangeFactory((key, before, after) -> {
                Object beforeValue = Optional.ofNullable(before).map(ConfigValue::value).orElse(null);
                Object afterValue = Optional.ofNullable(after).map(ConfigValue::value).orElse(null);
                StateChange change = StateChange.with(key, beforeValue, afterValue);
                return change.getOp() == DELETE && !before.isDeletable() ? Optional.empty() : Optional.of(change);
            })
            .build();
    }
}
