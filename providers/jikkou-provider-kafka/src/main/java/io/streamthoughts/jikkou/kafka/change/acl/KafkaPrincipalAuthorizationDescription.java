/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.acl;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;

import java.util.Optional;
import java.util.stream.Collectors;

public final class KafkaPrincipalAuthorizationDescription implements TextDescription {

    private final ResourceChange change;

    public KafkaPrincipalAuthorizationDescription(final ResourceChange change) {
        this.change = change;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String textual() {
        String bindings = change
                .getSpec()
                .getChanges()
                .get(AclChangeComputer.ACL)
                .all(TypeConverter.of(KafkaAclBinding.class))
                .stream()
                .map(this::textual)
                .collect(Collectors.joining(System.lineSeparator()));
        return String.format("%s ACLs for principal '%s':%n%s",
                change.getSpec().getOp().humanize(),
                change.getMetadata().getName(),
                bindings
        );
    }

    private String textual(final SpecificStateChange<KafkaAclBinding> change) {
        KafkaAclBinding binding = Optional.ofNullable(change.getAfter()).orElse(change.getBefore());
        return String.format("%s access control entry to %s '%s' to execute operation(s) '%s' on resource(s) '%s:%s:%s'",
                change.getOp().humanize(),
                binding.type(),
                binding.principal(),
                binding.operation(),
                binding.resourceType(),
                binding.patternType(),
                binding.resourcePattern()
        );
    }
}