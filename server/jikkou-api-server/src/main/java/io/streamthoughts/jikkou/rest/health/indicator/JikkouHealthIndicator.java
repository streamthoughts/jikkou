/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.health.indicator;

import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicator;
import io.streamthoughts.jikkou.core.models.ApiHealthResult;
import java.time.Duration;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public final class JikkouHealthIndicator implements HealthIndicator {

    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    private final JikkouApi api;
    private final ApiHealthIndicator indicator;
    private final JikkouHealthIndicatorConfiguration configuration;

    public JikkouHealthIndicator(JikkouApi api,
                                 ApiHealthIndicator indicator,
                                 JikkouHealthIndicatorConfiguration configuration) {
        this.api = api;
        this.indicator = indicator;
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Publisher<HealthResult> getResult() {
        return Flux.just(computeHealthResult());
    }

    public HealthResult computeHealthResult() {
        try {
            Duration timeout = configuration
                    .getTimeoutMs()
                    .map(Duration::ofMillis).orElse(DEFAULT_TIMEOUT);
            ApiHealthResult result = api.getApiHealth(indicator, timeout);
            return HealthResult
                    .builder(result.name())
                    .status(new HealthStatus(
                            result.status().name(),
                            result.status().description(),
                            null,
                            null)
                    )
                    .details(result.details())
                    .build();
        } catch (Exception e) {
            return HealthResult
                    .builder(indicator.name())
                    .status(HealthStatus.DOWN)
                    .exception(e)
                    .build();
        }
    }
}
