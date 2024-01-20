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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Objects;

/**
 * Schema Registry ACL entry.
 *
 * @param permission The permission.
 * @param resource   The Schema Registry ACL entry resource name pattern
 * @param username   The Username
 * @param id         The ID.
 */
@Reflectable
@JsonPropertyOrder({
        "permission",
        "resource",
        "username",
        "id"
})
public record SchemaRegistryAclEntry(@JsonProperty("permission") String permission,
                                     @JsonProperty("resource") String resource,
                                     @JsonProperty("username") String username,
                                     @JsonProperty("id") String id) {

    /**
     * Creates a new {@link SchemaRegistryAclEntry} instance.
     *
     * @param permission Permission
     * @param resource   Resource name pattern
     * @param username   Username
     */
    public SchemaRegistryAclEntry(final String permission,
                                  final String resource,
                                  final String username) {
        this(permission, resource, username, null);
    }

    /**
     * Creates a new {@link SchemaRegistryAclEntry} instance.
     *
     * @param permission Permission
     * @param resource   Resource name pattern
     * @param username   Username
     * @param id         ID
     */
    @ConstructorProperties({
            "permission",
            "resource",
            "username",
            "id"
    })
    public SchemaRegistryAclEntry {
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaRegistryAclEntry that = (SchemaRegistryAclEntry) o;
        return Objects.equals(permission, that.permission) &&
                Objects.equals(resource, that.resource) &&
                Objects.equals(username, that.username);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(permission, resource, username);
    }

}
