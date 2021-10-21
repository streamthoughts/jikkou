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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class V1QuotaEntityObject {

    private final String user;
    private final String clientId;

    /**
     * Creates a new {@link V1QuotaEntityObject} instance.
     *
     * @param user          The user to set the quota for.
     * @param clientId      The client id to set the quota for.
     */
    @JsonCreator
    public V1QuotaEntityObject(@Nullable @JsonProperty("user") final String user,
                               @Nullable @JsonProperty("client_id") final String clientId) {
        this.user = user;
        this.clientId = clientId;
    }

    @JsonProperty("user")
    public String user() {
        return user;
    }

    @JsonProperty("client_id")
    public String clientId() {
        return clientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V1QuotaEntityObject that = (V1QuotaEntityObject) o;
        return Objects.equals(user, that.user) && Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, clientId);
    }

    @Override
    public String toString() {
        return "V1QuotaEntityObject{" +
                "user='" + user + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
