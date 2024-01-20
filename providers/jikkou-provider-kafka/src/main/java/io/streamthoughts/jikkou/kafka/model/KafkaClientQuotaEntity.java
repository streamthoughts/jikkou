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
package io.streamthoughts.jikkou.kafka.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Objects;


/**
 * A secure logical group of clients that share both user principal and client ID.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Description("A secure logical group of clients that share both user principal and client ID.")
@JsonPropertyOrder({
        "user",
        "clientId"
})
@Reflectable
public final class KafkaClientQuotaEntity {

    @JsonProperty("user")
    private final String user;
    @JsonProperty("clientId")
    private final String clientId;

    /**
     * Creates a new {@link KafkaClientQuotaEntity} instance.
     *
     * @param clientId The client-id.
     * @param user     The user.
     */
    @ConstructorProperties({
            "user",
            "clientId"
    })
    public KafkaClientQuotaEntity(String user, String clientId) {
        super();
        this.user = user;
        this.clientId = clientId;
    }

    @JsonProperty("user")
    public String getUser() {
        return user;
    }

    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        KafkaClientQuotaEntity that = (KafkaClientQuotaEntity) object;
        return Objects.equals(user, that.user) && Objects.equals(clientId, that.clientId);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(user, clientId);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "KafkaClientQuotaEntity[" +
                "user=" + user +
                ", clientId=" + clientId +
                ']';
    }

    public KafkaClientQuotaEntityBuilder toBuilder() {
        return new KafkaClientQuotaEntityBuilder().withUser(this.user).withClientId(this.clientId);
    }

    public static KafkaClientQuotaEntityBuilder builder() {
        return new KafkaClientQuotaEntityBuilder();
    }

    public static class KafkaClientQuotaEntityBuilder {
        private String user;
        private String clientId;

        KafkaClientQuotaEntityBuilder() {}

        public KafkaClientQuotaEntityBuilder withUser(String user) {
            this.user = user;
            return this;
        }

        public KafkaClientQuotaEntityBuilder withClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public KafkaClientQuotaEntity build() {
            return new KafkaClientQuotaEntity(this.user, this.clientId);
        }
    }
}
