/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.runtime.configurator;

import io.jikkou.core.ApiConfigurator;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.jikkou.core.validation.Validation;
import io.jikkou.runtime.JikkouConfigProperties;

/**
 * An {@link ApiConfigurator} used to configure {@link JikkouApi} with all {@link Validation}
 * dynamically passed through the CLI configuration.
 */
public final class ValidationApiConfigurator extends ExtensionApiConfigurator<Validation<?>> {

    /**
     * Creates a new {@link ValidationApiConfigurator} instance.
     *
     * @param registry the ExtensionDescriptorRegistry instance.
     */
    public ValidationApiConfigurator(final ExtensionDescriptorRegistry registry) {
        super(registry, JikkouConfigProperties.VALIDATIONS_CONFIG);
    }
}