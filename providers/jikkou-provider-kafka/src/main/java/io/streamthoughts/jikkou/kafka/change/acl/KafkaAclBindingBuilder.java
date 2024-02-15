/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.acl;

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
