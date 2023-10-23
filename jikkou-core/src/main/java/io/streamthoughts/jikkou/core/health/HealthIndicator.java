/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.core.health;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.annotation.Category;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.config.Configurable;
import io.streamthoughts.jikkou.core.extension.Extension;
import java.time.Duration;

/**
 * Provides indicators about the health of a service or sub-system.
 */
@Evolving
@Enabled
@Category("HealthIndicator")
public interface HealthIndicator extends Extension, Configurable {

    /**
     * Gets the health for a service or sub-system.
     *
     * @param timeout the timeout to be used for getting health.
     *
     * @return  a new {@link Health} instance.
     */
    Health getHealth(final Duration timeout);
}