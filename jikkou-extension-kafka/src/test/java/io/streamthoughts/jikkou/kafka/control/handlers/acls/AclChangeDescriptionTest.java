/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.kafka.control.handlers.acls;

import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.kafka.control.change.AclChange;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AclChangeDescriptionTest {

    @Test
    void shouldReturnTextualDescription() {
        var desc = new AclChangeDescription(
                AclChange.builder()
                        .withOperation(ChangeType.ADD)
                        .withAcl(KafkaAclBinding.builder()
                                .withPrincipal("User:test")
                                .withPatternType(PatternType.LITERAL)
                                .withOperation(AclOperation.ALL)
                                .withType(AclPermissionType.ALLOW)
                                .withResourcePattern("test")
                                .withResourceType(ResourceType.TOPIC)
                                .withHost("*")
                                .build()
                        )
                        .build()
        );
        String textual = desc.textual();
        Assertions.assertEquals("Add ACL to ALLOW 'User:test' to execute operation(s) 'ALL' on resource(s) 'TOPIC:LITERAL:test'", textual);
    }

}