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
package io.streamthoughts.jikkou.kafka.validations;

import io.streamthoughts.jikkou.api.AcceptResource;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.extensions.annotations.EnableAutoConfigure;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceValidation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessUserObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAuthorizationList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@AcceptResource(type = V1KafkaAuthorizationList.class)
@EnableAutoConfigure
public class NoDuplicateUsersAllowedValidation implements ResourceValidation {

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final @NotNull HasMetadata resource) throws ValidationException {
        V1KafkaAuthorizationList accessList = (V1KafkaAuthorizationList) resource;

        final Collection<V1KafkaAccessUserObject> users = accessList.getSpec().getUsers();

        if (users.isEmpty()) return;

        final Map<String, List<V1KafkaAccessUserObject>> groupedByName = users.stream()
                .collect(Collectors.groupingBy(V1KafkaAccessUserObject::getPrincipal));

        final Set<String> duplicates = groupedByName.entrySet()
                .stream()
                .filter(it -> it.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (!duplicates.isEmpty()) {
            throw new ValidationException("Duplicates 'principal' in specs.security.users: " + duplicates, this);
        }
    }
}
