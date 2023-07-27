/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import io.streamthoughts.jikkou.api.control.ChangeDescription;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;

public final class AclEntryChangeDescription {

    public static ChangeDescription of(ChangeType type, KafkaAclEntry entry) {
        return () -> String.format("%s Kafka ACL Entry for user '%s' (topic=%s, permission=%s)",
                ChangeDescription.humanize(type),
                entry.username(),
                entry.topic(),
                entry.permission()
        );
    }

    public static ChangeDescription of(ChangeType type, SchemaRegistryAclEntry entry) {
        return () -> String.format("%s Schema Registry ACL Entry for user '%s' (resource=%s, permission=%s)",
                ChangeDescription.humanize(type),
                entry.username(),
                entry.resource(),
                entry.permission()
        );
    }
}
