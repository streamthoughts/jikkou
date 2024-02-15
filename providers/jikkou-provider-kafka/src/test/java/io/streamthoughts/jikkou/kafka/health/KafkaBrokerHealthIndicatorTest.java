/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.health;

import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class KafkaBrokerHealthIndicatorTest {

    @Test
    void shouldThrowExceptionWhenNotConfigured() {
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> new KafkaBrokerHealthIndicator().getHealth(Duration.ZERO)
        );
    }

}