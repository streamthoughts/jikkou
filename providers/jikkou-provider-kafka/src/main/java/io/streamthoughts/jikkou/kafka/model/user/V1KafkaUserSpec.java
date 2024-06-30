/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.model.user;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * V1UserSpec.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Description("User SCRAM credential specification.")
@JsonPropertyOrder({
    "authentication",
})
@JsonDeserialize(builder = V1KafkaUserSpec.Builder.class)
@Reflectable
public record V1KafkaUserSpec(
    @JsonPropertyDescription("The authentication mechanisms enabled for this Kafka user.")
    @NotNull
    List<V1KafkaUserAuthentication> authentications
) {

    /**
     * Creates a new builder.
     *
     * @return the {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder from that object.
     *
     * @return a new {@link Builder}.
     */
    public Builder toBuilder() {
        return new Builder()
            .withAuthentications(authentications);
    }

    /**
     * Build class for {@link V1KafkaUserSpec}.
     */
    public static final class Builder {
        private List<V1KafkaUserAuthentication> authentications;

        public Builder withAuthentications(final Collection<V1KafkaUserAuthentication> authentications) {
            this.authentications = new ArrayList<>(authentications);
            return this;
        }

        public V1KafkaUserSpec build() {
            return new V1KafkaUserSpec(authentications);
        }
    }
}
