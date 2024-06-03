/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.model.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.annotation.Verbs;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.SpecificResource;
import io.streamthoughts.jikkou.core.models.Verb;

/**
 * V1User
 * <p>
 * KafkaUser resources provide a way of describing User SCRAM credentials in a Kafka cluster.
 */
@Description("KafkaUser resources provide a way of describing User SCRAM credentials in a Kafka cluster.")
@Names(singular = "kafkauser", plural = "kafkausers", shortNames = {
    "ku"
})
@Verbs({
    Verb.LIST,
    Verb.CREATE,
    Verb.UPDATE,
    Verb.DELETE,
    Verb.GET,
    Verb.APPLY
})
@ApiVersion("kafka.jikkou.io/v1")
@Kind("KafkaUser")
@JsonDeserialize(builder = V1KafkaUser.Builder.class)
public class V1KafkaUser extends SpecificResource<V1KafkaUser, V1KafkaUserSpec> {

    public V1KafkaUser(final String apiVersion,
                       final String kind,
                       final ObjectMeta metadata,
                       final V1KafkaUserSpec spec) {
        super(apiVersion, kind, metadata, spec);
    }

    public V1KafkaUser(final ObjectMeta metadata,
                       final V1KafkaUserSpec spec) {
        super(metadata, spec);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Builder toBuilder() {
        return new Builder()
            .withApiVersion(apiVersion)
            .withKind(kind)
            .withMetadata(metadata)
            .withSpec(spec);
    }

    public static final class Builder extends SpecificResource.Builder<V1KafkaUser.Builder, V1KafkaUser, V1KafkaUserSpec> {
        /**
         * {@inheritDoc}
         */
        @Override
        public V1KafkaUser build() {
            return new V1KafkaUser(metadata, spec);
        }
    }
}
