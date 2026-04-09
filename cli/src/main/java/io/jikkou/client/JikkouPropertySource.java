/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client;

import io.jikkou.core.config.Configuration;
import io.micronaut.context.env.MapPropertySource;

/**
 * PropertySource to provide Jikkou configuration to Micronaut.
 */
public final class JikkouPropertySource extends MapPropertySource {

    public static final String NAME = "jikkou";

    /**
     * Creates a map property source.
     */
    public JikkouPropertySource(Configuration config) {
        super(NAME, config.asMap(false));
    }
}
