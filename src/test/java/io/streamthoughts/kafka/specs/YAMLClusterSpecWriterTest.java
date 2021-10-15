/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.kafka.specs;

import io.streamthoughts.kafka.specs.model.*;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class YAMLClusterSpecWriterTest {

    @Test
    public void should_serialize_given_security_users() {

        final V1AccessUserObject user = V1AccessUserObject.newBuilder()
                .withPrincipal("User:test")
                .withRoles(Set.of("test-rile"))
                .withPermission(
                        V1AccessPermission.newBuilder()
                                .allow(V1AccessOperationPolicy.fromString("READ:*"))
                                .onResource(V1AccessResourceMatcher.newBuilder()
                                        .withPattern("topic-test")
                                        .withPatternType(PatternType.LITERAL)
                                        .withType(ResourceType.TOPIC)
                                        .build()
                                )
                                .build()
                ).build();
        V1SpecsObject v1SpecsObject = V1SpecsObject.withSecurity(V1SecurityObject.withUsers(List.of(user)));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        YAMLClusterSpecWriter.instance().write(new V1SpecFile(MetaObject.defaults(), v1SpecsObject), os);
        Assertions.assertNotNull(os.toString(StandardCharsets.UTF_8));
    }
}