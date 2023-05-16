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
package io.streamthoughts.jikkou.kafka.control.handlers.acls;

import io.streamthoughts.jikkou.api.control.ChangeDescription;
import io.streamthoughts.jikkou.kafka.control.change.AclChange;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;

public class AclChangeDescription implements ChangeDescription {

    private final AclChange change;

    public AclChangeDescription(AclChange change) {
        this.change = change;
    }

    /** {@inheritDoc} **/
    @Override
    public String textual() {
        KafkaAclBinding policy = change.getAclBindings();
        return String.format("%s ACL to %s '%s' to execute operation(s) '%s' on resource(s) '%s:%s:%s'",
                ChangeDescription.humanize(change.getChangeType()),
                policy.getType(),
                policy.getPrincipal(),
                policy.getOperation(),
                policy.getResourceType(),
                policy.getPatternType(),
                policy.getResourcePattern()
        );
    }
}
