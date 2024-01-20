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
