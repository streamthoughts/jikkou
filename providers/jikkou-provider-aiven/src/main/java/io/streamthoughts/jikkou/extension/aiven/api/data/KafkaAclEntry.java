/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Objects;

/**
 * Kafka Topic ACL entry
 *
 * @param permission Kafka permission
 * @param topic      Topic name pattern
 * @param username   Username
 * @param id         ID
 */
@Reflectable
@JsonPropertyOrder({
        "permission",
        "topic",
        "username",
        "id"
})
public record KafkaAclEntry(@JsonProperty("permission") String permission,
                            @JsonProperty("topic") String topic,
                            @JsonProperty("username") String username,
                            @JsonProperty("id") String id) {

    /**
     * Creates a new {@link KafkaAclEntry} instance.
     */
    @ConstructorProperties({
            "permission",
            "topic",
            "username",
            "id"
    })
    public KafkaAclEntry {
    }

    /**
     * Creates a new {@link KafkaAclEntry} instance.
     *
     * @param permission Kafka permission
     * @param topic      Topic name pattern
     * @param username   Username
     */
    public KafkaAclEntry(final String permission,
                         final String topic,
                         final String username) {
        this(permission, topic, username, null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaAclEntry that = (KafkaAclEntry) o;
        return Objects.equals(permission, that.permission) &&
                Objects.equals(topic, that.topic) &&
                Objects.equals(username, that.username);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(permission, topic, username);
    }
}
