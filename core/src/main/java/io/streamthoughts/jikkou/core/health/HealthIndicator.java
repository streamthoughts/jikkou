/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.health;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.annotations.Category;
import java.time.Duration;

/**
 * Provides indicators about the health of a service or sub-system.
 */
@Evolving
@Enabled
@Category(ExtensionCategory.HEALTH_INDICATOR)
public interface HealthIndicator extends Extension {

    /**
     * Gets the health for a service or sub-system.
     *
     * @param timeout the timeout to be used for getting health.
     * @return a new {@link Health} instance.
     */
    Health getHealth(final Duration timeout);
}