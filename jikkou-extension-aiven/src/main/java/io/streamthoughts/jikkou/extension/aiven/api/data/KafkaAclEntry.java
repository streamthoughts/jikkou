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
package io.streamthoughts.jikkou.extension.aiven.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.annotation.Reflectable;
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
public record KafkaAclEntry(String permission, String topic, String username, String id) {

    /**
     * Creates a new {@link KafkaAclEntry} instance.
     */
    @ConstructorProperties({
            "permission",
            "topic",
            "username",
            "id"
    })
    public KafkaAclEntry {}

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

    @Override
    @JsonProperty("permission")
    public String permission() {
        return permission;
    }

    @Override
    @JsonProperty("topic")
    public String topic() {
        return topic;
    }

    @Override
    @JsonProperty("username")
    public String username() {
        return username;
    }

    @Override
    @JsonProperty("id")
    public String id() {
        return id;
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

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "KafkaAclEntry{" +
                "permission=" + permission +
                ", topic='" + topic + '\'' +
                ", username='" + username + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
