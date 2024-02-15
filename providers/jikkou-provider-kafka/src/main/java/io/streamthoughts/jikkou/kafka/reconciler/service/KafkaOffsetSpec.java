/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
