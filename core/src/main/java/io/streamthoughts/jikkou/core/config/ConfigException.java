/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.config;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;

public class ConfigException extends JikkouRuntimeException {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public static class Missing extends ConfigException {

        private final ConfigProperty<?> property;

        public Missing(ConfigProperty<?> property) {
            super(String.format(
                    "No value present for param '%s'%s",
                    property.key(),
                    property.description() != null ? " (" + property.description() + ")" : ""
                    )
            );
            this.property = property;
        }

        public ConfigProperty<?> property() {
            return property;
        }
    }
}
