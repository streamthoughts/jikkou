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
