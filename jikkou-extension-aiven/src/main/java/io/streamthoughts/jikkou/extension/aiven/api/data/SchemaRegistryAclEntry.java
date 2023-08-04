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
 * Schema Registry ACL entry
 */
@Reflectable
public final class SchemaRegistryAclEntry {

    /**
     * ACL entry for Schema Registry
     */
    private final String permission;
    /**
     * Schema Registry ACL entry resource name pattern
     */
    private final String resource;
    /**
     * Username
     */
    private final String username;
    /**
     * ID
     */
    private final String id;

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
    @JsonCreator
    public SchemaRegistryAclEntry(@JsonProperty("permission") final String permission,
                                  @JsonProperty("resource") final String resource,
                                  @JsonProperty("username") final String username,
                                  @JsonProperty("id") final String id) {
        this.permission = permission;
        this.resource = resource;
        this.username = username;
        this.id = id;
    }

    @JsonProperty("permission")
    public String permission() {
        return permission;
    }

    @JsonProperty("resource")
    public String resource() {
        return resource;
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

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "SchemaRegistryAclEntry{" +
                "permission=" + permission +
                ", resource='" + resource + '\'' +
                ", username='" + username + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
