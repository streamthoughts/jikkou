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
package io.streamthoughts.jikkou.extension.aiven.change;

import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaQuotaEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;

public final class KafkaChangeDescriptions {

    public static TextDescription of(Operation type, KafkaAclEntry entry) {
        return () -> String.format("%s Kafka ACL Entry for user '%s' (topic=%s, permission=%s)",
                type.humanize(),
                entry.username(),
                entry.topic(),
                entry.permission()
        );
    }

    public static TextDescription of(Operation type, SchemaRegistryAclEntry entry) {
        return () -> String.format("%s Schema Registry ACL Entry for user '%s' (resource=%s, permission=%s)",
                type.humanize(),
                entry.username(),
                entry.resource(),
                entry.permission()
        );
    }

    public static TextDescription of(Operation type, KafkaQuotaEntry entry) {
        return () -> String.format("%s Kafka quotas for user '%s' and client-id '%s'",
                type.humanize(),
                entry.user(),
                entry.clientId()
        );
    }
}
