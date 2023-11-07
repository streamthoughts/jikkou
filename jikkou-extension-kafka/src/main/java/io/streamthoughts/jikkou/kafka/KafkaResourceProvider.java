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
package io.streamthoughts.jikkou.kafka;

import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaBrokerList;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaClientQuotaList;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaBroker;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalRole;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.spi.ResourceProvider;

public final class KafkaResourceProvider implements ResourceProvider {

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerAll(ResourceRegistry context) {
        context.register(V1KafkaBrokerList.class);
        context.register(V1KafkaBroker.class);
        context.register(V1KafkaClientQuota.class);
        context.register(V1KafkaClientQuotaList.class);
        context.register(V1KafkaTopicList.class);
        context.register(V1KafkaTopic.class);
        context.register(V1KafkaPrincipalAuthorization.class);
        context.register(V1KafkaPrincipalRole.class);
        context.register(V1KafkaTableRecord.class);
        context.register(V1KafkaConsumerGroup.class);
    }
}
