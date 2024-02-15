/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaOffsetSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaOffsetSpecTest {

    @Test
    void shouldGetToTimestampGivenDateTime() {
        KafkaOffsetSpec.ToTimestamp spec = KafkaOffsetSpec.ToTimestamp
                .fromISODateTime("2023-01-01T01:02:30.999");
        Assertions.assertEquals(new KafkaOffsetSpec.ToTimestamp(1672534950999L), spec);

    }
}
