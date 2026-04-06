/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

/**
 * Registry for managing named groups of providers for batch operations.
 *
 * <p>Provider groups allow users to define named sets of providers (e.g., "production", "non-prod")
 * that can be targeted with the {@code --provider-group} CLI flag.</p>
 *
 * @since 0.38.0
 */
public class ProviderGroupRegistry {

    private final Map<String, List<String>> groups = new ConcurrentHashMap<>();

    /**
     * Registers a provider group.
     *
     * @param groupName     the name of the group (e.g., "production", "non-prod")
     * @param providerNames the list of provider names in this group
     * @throws IllegalArgumentException if the group name is already registered
     */
    public void registerGroup(@NotNull String groupName, @NotNull List<String> providerNames) {
        if (groups.containsKey(groupName)) {
            throw new IllegalArgumentException("Provider group already registered: " + groupName);
        }
        groups.put(groupName, Collections.unmodifiableList(new ArrayList<>(providerNames)));
    }

    /**
     * Gets the provider names for a specific group.
     *
     * @param groupName the name of the group
     * @return the list of provider names in the group
     * @throws IllegalArgumentException if the group is not found
     */
    public @NotNull List<String> getProviderNames(@NotNull String groupName) {
        List<String> names = groups.get(groupName);
        if (names == null) {
            throw new IllegalArgumentException(String.format(
                "Provider group '%s' not found. Available groups: %s.",
                groupName,
                groups.keySet()
            ));
        }
        return names;
    }

    /**
     * Gets all registered group names.
     *
     * @return set of all group names
     */
    public @NotNull Set<String> getAllGroupNames() {
        return Collections.unmodifiableSet(groups.keySet());
    }

    /**
     * Checks if a group exists.
     *
     * @param groupName the group name
     * @return true if the group exists
     */
    public boolean hasGroup(@NotNull String groupName) {
        return groups.containsKey(groupName);
    }
}
