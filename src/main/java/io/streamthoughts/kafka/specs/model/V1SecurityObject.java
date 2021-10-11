/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.kafka.specs.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collection;

public class V1SecurityObject implements Serializable {

    private final Collection<V1AccessPrincipalObject> users;
    private final Collection<V1AccessRoleObject> roles;

    /**
     * Creates a new {@link V1SecurityObject} instance.
     * 
     * @param users the list of {@link V1AccessPrincipalObject} instance
     * @param roles the list of {@link V1AccessRoleObject} instance
     */
    @JsonCreator
    public V1SecurityObject(@JsonProperty("users") final Collection<V1AccessPrincipalObject> users,
                            @JsonProperty("roles") final Collection<V1AccessRoleObject> roles) {
        this.users = users;
        this.roles = roles;
    }

    /**
     * @return  the list of {@link V1AccessPrincipalObject}.
     */
    @JsonProperty
    public Collection<V1AccessPrincipalObject> users() {
        return users;
    }

    /**
     * @return  the list of {@link V1AccessRoleObject}.
     */
    @JsonProperty
    public Collection<V1AccessRoleObject> roles() {
        return roles;
    }
}
