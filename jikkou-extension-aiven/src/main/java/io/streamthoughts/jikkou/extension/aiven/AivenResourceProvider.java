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
package io.streamthoughts.jikkou.extension.aiven;

import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuotaList;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntryList;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntryList;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.spi.ResourceProvider;

public final class AivenResourceProvider implements ResourceProvider {

    public static final String SCHEMA_REGISTRY_API_VERSION = "kafka.aiven.io/v1beta1";
    public static final String SCHEMA_REGISTRY_KIND = "SchemaRegistrySubject";


    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerAll(ResourceRegistry context) {
        context.register(V1KafkaTopicAclEntry.class);
        context.register(V1KafkaTopicAclEntryList.class);
        context.register(V1SchemaRegistryAclEntry.class);
        context.register(V1SchemaRegistryAclEntryList.class);
        context.register(V1KafkaQuota.class);
        context.register(V1KafkaQuotaList.class);
        context.register(V1SchemaRegistrySubject.class, SCHEMA_REGISTRY_API_VERSION)
                .setSingularName("avn-schemaregistrysubject")
                .setPluralName("avn-schemaregistrysubjects")
                .setShortNames(null);
    }
}
