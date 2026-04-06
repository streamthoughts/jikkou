/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.runtime.JikkouConfigProperties;
import io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Resolves provider names from CLI flags (--provider-all, --provider-group)
 * using the application configuration, and builds reconciliation contexts
 * for multi-provider operations.
 *
 * @since 0.38.0
 */
public final class ProviderResolver {

    private final Configuration configuration;

    public ProviderResolver(@NotNull Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Resolves all enabled provider names from the configuration.
     *
     * @return list of all enabled provider names
     */
    public @NotNull List<String> resolveAllProviders() {
        List<ExtensionConfigEntry> providers = JikkouConfigProperties.PROVIDER_CONFIG.get(configuration);
        return providers.stream()
            .filter(ExtensionConfigEntry::enabled)
            .map(ExtensionConfigEntry::name)
            .toList();
    }

    /**
     * Resolves provider names for a named group.
     *
     * @param groupName the group name
     * @return list of provider names in the group
     * @throws IllegalArgumentException if the group is not found
     */
    public @NotNull List<String> resolveProviderGroup(@NotNull String groupName) {
        Map<String, List<String>> groups = JikkouConfigProperties.PROVIDER_GROUPS_CONFIG.get(configuration);
        List<String> providers = groups.get(groupName);
        if (providers == null) {
            throw new IllegalArgumentException(String.format(
                "Provider group '%s' not found. Available groups: %s. " +
                "Configure groups in: jikkou { provider-groups { %s = [\"provider1\", \"provider2\"] } }",
                groupName, groups.keySet(), groupName
            ));
        }
        return providers;
    }

    /**
     * Resolves provider names from the ProviderOptionMixin flags.
     * Returns an empty list if no batch flags are set (single-provider mode).
     *
     * @param mixin the provider option mixin
     * @return list of provider names, empty for single-provider mode
     * @throws IllegalArgumentException if mutually exclusive flags are set
     */
    public @NotNull List<String> resolveProviderNames(@NotNull ProviderOptionMixin mixin) {
        int flagCount = (mixin.isProviderAll() ? 1 : 0)
            + (mixin.getProviderGroup() != null ? 1 : 0)
            + (mixin.getProvider() != null ? 1 : 0);

        if (flagCount > 1) {
            throw new IllegalArgumentException(
                "Options --provider, --provider-all, and --provider-group are mutually exclusive.");
        }

        if (mixin.isProviderAll()) {
            return resolveAllProviders();
        }
        if (mixin.getProviderGroup() != null) {
            return resolveProviderGroup(mixin.getProviderGroup());
        }
        return List.of();
    }

    /**
     * Builds a {@link ReconciliationContext} from CLI option mixins, resolving
     * provider names from batch flags.
     *
     * @param providerOptions the provider CLI options
     * @param configOptions   the config CLI options
     * @param selectorOptions the selector CLI options
     * @param fileOptions     the file CLI options
     * @param dryRun          whether this is a dry-run
     * @return a fully configured reconciliation context
     * @since 0.38.0
     */
    public @NotNull ReconciliationContext buildReconciliationContext(
            @NotNull ProviderOptionMixin providerOptions,
            @NotNull ConfigOptionsMixin configOptions,
            @NotNull SelectorOptionsMixin selectorOptions,
            @NotNull FileOptionsMixin fileOptions,
            boolean dryRun) {
        List<String> providerNames = resolveProviderNames(providerOptions);
        return ReconciliationContext.builder()
            .dryRun(dryRun)
            .configuration(configOptions.getConfiguration())
            .selector(selectorOptions.getResourceSelector())
            .labels(fileOptions.getLabels())
            .annotations(fileOptions.getAnnotations())
            .providerName(providerOptions.getProvider())
            .providerNames(providerNames)
            .continueOnError(providerOptions.isContinueOnError())
            .build();
    }
}
