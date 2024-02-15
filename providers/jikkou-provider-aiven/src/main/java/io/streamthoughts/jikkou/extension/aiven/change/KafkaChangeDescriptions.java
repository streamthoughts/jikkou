/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
