/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.health.indicator;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.util.Toggleable;
import java.util.Optional;

@ConfigurationProperties("endpoints.health.jikkou")
public interface JikkouHealthIndicatorConfiguration extends Toggleable {

    Optional<Long> getTimeoutMs();
}
