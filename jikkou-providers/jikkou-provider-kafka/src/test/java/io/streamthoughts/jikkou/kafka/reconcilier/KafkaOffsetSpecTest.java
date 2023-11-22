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
package io.streamthoughts.jikkou.kafka.reconcilier;

import io.streamthoughts.jikkou.kafka.reconcilier.service.KafkaOffsetSpec;
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
