/*
 * Copyright 2023 The original authors
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
package io.streamthoughts.jikkou.kafka.change.acl;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import java.util.stream.Collectors;

public final class KafkaPrincipalAuthorizationDescription implements TextDescription {

    private final ResourceChange change;

    public KafkaPrincipalAuthorizationDescription(ResourceChange change) {
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

    private String textual(SpecificStateChange<KafkaAclBinding> change) {
        KafkaAclBinding binding = change.getAfter();
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