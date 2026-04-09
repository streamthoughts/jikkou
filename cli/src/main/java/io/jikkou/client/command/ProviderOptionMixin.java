/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command;

import picocli.CommandLine.Option;

/**
 * Mixin class for the --provider, --provider-all, --provider-group, and --continue-on-error CLI options.
 * Use this mixin to add provider selection support to commands that need
 * to target a specific provider instance or multiple providers when multiple
 * providers of the same type are configured.
 */
public final class ProviderOptionMixin {

    @Option(
            names = {"--provider"},
            description = "Select a specific provider instance when multiple providers of the same type are configured (e.g., kafka-prod, kafka-dev)"
    )
    public String provider;

    @Option(
            names = {"--provider-all"},
            description = "Apply to all registered providers of matching type",
            defaultValue = "false"
    )
    public boolean providerAll;

    @Option(
            names = {"--provider-group"},
            description = "Apply to a named group of providers (configured in provider-groups)"
    )
    public String providerGroup;

    @Option(
            names = {"--continue-on-error"},
            description = "Continue reconciliation when a provider fails during batch operations (default: fail-fast)",
            defaultValue = "false"
    )
    public boolean continueOnError;

    /**
     * Gets the provider name.
     *
     * @return the provider name, or null if not specified.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Gets whether --provider-all flag is set.
     *
     * @return true if batch apply to all providers is requested.
     */
    public boolean isProviderAll() {
        return providerAll;
    }

    /**
     * Gets the provider group name.
     *
     * @return the provider group name, or null if not specified.
     */
    public String getProviderGroup() {
        return providerGroup;
    }

    /**
     * Gets whether --continue-on-error flag is set.
     *
     * @return true if reconciliation should continue on error.
     */
    public boolean isContinueOnError() {
        return continueOnError;
    }
}
