/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.acl.builder;

import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.kafka.change.acl.KafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAcl;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.kafka.common.resource.PatternType;

public class LiteralKafkaAclBindingBuilder extends AbstractKafkaAclBindingBuilder implements KafkaAclBindingBuilder {

    /**
     * Creates a new {@link LiteralKafkaAclBindingBuilder} instance.
     */
    public LiteralKafkaAclBindingBuilder() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<KafkaAclBinding> toKafkaAclBindings(final V1KafkaPrincipalAuthorization resource) {

        List<V1KafkaPrincipalAcl> acls = getAcceptedAclsFromResource(resource);
        if (acls.isEmpty()) return Collections.emptyList();

        String principal = getPrincipalFromResource(resource);
        return buildAclBindings(
                principal,
                acls,
                null,
                null,
                null,
                CoreAnnotations.isAnnotatedWithDelete(resource)
        );
    }

    private List<V1KafkaPrincipalAcl> getAcceptedAclsFromResource(V1KafkaPrincipalAuthorization resource) {
        return resource.getSpec().getAcls()
                .stream()
                .filter(this::isAclAccepted)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isAclAccepted(V1KafkaPrincipalAcl object) {
        return object.getResource().getPatternType() != PatternType.MATCH;
    }

    private String getPrincipalFromResource(V1KafkaPrincipalAuthorization resource) {
        return resource.getMetadata().getName();
    }
}
