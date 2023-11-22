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

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import org.jetbrains.annotations.NotNull;

/**
 * Any class that need be configured with external config properties should implement that interface.
 *
 * @see Configuration
 */
@InterfaceStability.Evolving
public interface Configurable {

    /**
     * Configures this class with the given configuration.
     *
     * @param config    the {@link Configuration}.
     */
    default void configure(@NotNull Configuration config) throws ConfigException {
        // intentionally left blank
    }
}