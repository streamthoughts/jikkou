/*
 * Copyright 2020 StreamThoughts.
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

import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This interface is used to convert a {@link V1KafkaPrincipalAuthorization}
 * resource to a corresponding list of {@link KafkaAclBinding}.
 */
public interface KafkaAclBindingBuilder {

    List<KafkaAclBinding> toKafkaAclBindings(final V1KafkaPrincipalAuthorization resource);

    /**
     * Static helper method to combine multiple builders.
     *
     * @param builders  the list of builder to combine.
     * @return  a new {@link KafkaAclBindingBuilder}.
     */
    static KafkaAclBindingBuilder combines(final KafkaAclBindingBuilder...builders) {

        return new KafkaAclBindingBuilder() {

            /** {@inheritDoc} */
            @Override
            public List<KafkaAclBinding> toKafkaAclBindings(final V1KafkaPrincipalAuthorization resource) {
                return Arrays.stream(builders)
                        .flatMap(b -> b.toKafkaAclBindings(resource).stream())
                        .collect(Collectors.toCollection(LinkedList::new));
            }
        };
    }
}
