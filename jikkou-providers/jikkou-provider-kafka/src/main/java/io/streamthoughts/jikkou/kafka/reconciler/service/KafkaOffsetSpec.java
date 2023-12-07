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
package io.streamthoughts.jikkou.kafka.reconciler.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * KafkaOffsetSpec.
 */
public sealed interface KafkaOffsetSpec permits
        KafkaOffsetSpec.ToEarliest,
        KafkaOffsetSpec.ToLatest,
        KafkaOffsetSpec.ToTimestamp,
        KafkaOffsetSpec.ToOffset {

    /**
     * Used to retrieve the EARLIEST offset of a partition.
     */
    record ToEarliest() implements KafkaOffsetSpec {
        @Override
        public String toString() {
            return "ToEarliest";
        }
    }
    /**
     * Used to retrieve the LATEST offset of a partition.
     */
    record ToLatest() implements KafkaOffsetSpec {
        @Override
        public String toString() {
            return "ToLatest";
        }
    }
    /**
     * Used to retrieve the earliest offset whose timestamp is greater than or equal
     * to the given timestamp in the corresponding partition
     *
     * @param timestamp The timestamp.
     */
    record ToTimestamp(Long timestamp) implements KafkaOffsetSpec {

        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                .withZone(ZoneOffset.UTC);

        public static ToTimestamp fromISODateTime(String dateTime) {
            OffsetDateTime odt = OffsetDateTime.parse(dateTime, DATE_TIME_FORMATTER);
            Instant instant = Instant.from(odt);
            return new ToTimestamp(instant.toEpochMilli());
        }

        @Override
        public String toString() {
            String dateTime = DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(timestamp));
            return "ToTimestamp[epoch=" + timestamp + ", dateTime=" + dateTime + "]";
        }
    }
    /**
     * Used to indicate a specific offset of a partition.
     */
    record ToOffset(Long offset)  implements KafkaOffsetSpec {
    }
}
