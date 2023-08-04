/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.kafka.control.change;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.streamthoughts.jikkou.api.control.Change;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import java.util.Objects;
import lombok.Builder;

@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
public final class AclChange implements Change {

    private final ChangeType operation;

    private final KafkaAclBinding acl;

    static AclChange delete(final KafkaAclBinding policy) {
        return new AclChange(ChangeType.DELETE, policy);
    }

    static AclChange add(final KafkaAclBinding policy) {
        return new AclChange(ChangeType.ADD, policy);
    }

    static AclChange none(final KafkaAclBinding policy) {
        return new AclChange(ChangeType.NONE, policy);
    }

    /**
     * Creates a new {@link AclChange} instance.
     *
     * @param operation the {@link ChangeType}.
     * @param acl    the {@link KafkaAclBinding}.
     */
    private AclChange(final ChangeType operation,
                      final KafkaAclBinding acl) {
        this.operation = Objects.requireNonNull(operation, "'operation' should not be null");
        this.acl = Objects.requireNonNull(acl, "'policy' should not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeType getChangeType() {
        return operation;
    }


    @JsonProperty
    @JsonUnwrapped
    public KafkaAclBinding getAclBindings() {
        return acl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AclChange aclChange = (AclChange) o;
        return operation == aclChange.operation && Objects.equals(acl, aclChange.acl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(operation, acl);
    }
}
