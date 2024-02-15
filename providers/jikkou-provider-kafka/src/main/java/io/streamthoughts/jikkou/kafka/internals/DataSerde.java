/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals;

import io.streamthoughts.jikkou.kafka.model.DataHandle;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

public interface DataSerde {

    Optional<ByteBuffer> serialize(String topicName,
                                   DataHandle data,
                                   Map<String, Object> properties,
                                   boolean isForRecordKey
    );

    Optional<DataHandle> deserialize(String topicName,
                                     ByteBuffer data,
                                     Map<String, Object> properties,
                                     boolean isForRecordKey
    );
}
