/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import picocli.CommandLine.Option;

/**
 * Mixin class for the --provider CLI option.
 * Use this mixin to add provider selection support to commands that need
 * to target a specific provider instance when multiple providers of the
 * same type are configured.
 */
public final class ProviderOptionMixin {

    @Option(
            names = {"--provider"},
            description = "Select a specific provider instance when multiple providers of the same type are configured (e.g., kafka-prod, kafka-dev)"
    )
    public String provider;

    /**
     * Gets the provider name.
     *
     * @return the provider name, or null if not specified.
     */
    public String getProvider() {
        return provider;
    }
}