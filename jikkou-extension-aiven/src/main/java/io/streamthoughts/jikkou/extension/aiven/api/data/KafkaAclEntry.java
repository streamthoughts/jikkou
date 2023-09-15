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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.annotation.Reflectable;
import java.util.Objects;

/**
 * Kafka Topic ACL entry
 */
@Reflectable
public final class KafkaAclEntry {

    /**
     * Kafka permission
     */
    private final String permission;
    /**
     * Topic name pattern
     */
    private final String topic;
    /**
     * Username
     */
    private final String username;
    /**
     * ID
     */
    private final String id;

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
     * Creates a new {@link KafkaAclEntry} instance.
     *
     * @param permission Kafka permission
     * @param topic      Topic name pattern
     * @param username   Username
     * @param id         Username
     */
    @JsonCreator
    public KafkaAclEntry(@JsonProperty("permission") final String permission,
                         @JsonProperty("topic") final String topic,
                         @JsonProperty("username") final String username,
                         @JsonProperty("id") final String id) {
        this.permission = permission;
        this.topic = topic;
        this.username = username;
        this.id = id;
    }

    @JsonProperty("permission")
    public String permission() {
        return permission;
    }

    @JsonProperty("topic")
    public String topic() {
        return topic;
    }

    @JsonProperty("username")
    public String username() {
        return username;
    }

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