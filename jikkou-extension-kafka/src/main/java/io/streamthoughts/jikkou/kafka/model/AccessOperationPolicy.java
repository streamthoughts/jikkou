/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import lombok.Builder;
import lombok.With;
import org.apache.kafka.common.acl.AclOperation;
import org.jetbrains.annotations.NotNull;

@JsonSerialize(using = AccessOperationPolicy.Serializer.class)
@JsonDeserialize(using = AccessOperationPolicy.Deserializer.class)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
public class AccessOperationPolicy implements Serializable {

    private static final String ANY_HOSTS = "*";

    private final AclOperation operation;

    private final String host;

    public static AccessOperationPolicy fromString(final String policy) {
        if (policy.contains(":")) {
            String operation = policy.substring(0, policy.indexOf(":"));
            String host = policy.substring(operation.length() + 1);
            return new AccessOperationPolicy(AclOperation.fromString(operation), host);
        }

        return new AccessOperationPolicy(AclOperation.fromString(policy));
    }

    @JsonCreator
    public AccessOperationPolicy(final String value) {
        if (value.contains(":")) {
            String operation = value.substring(0, value.indexOf(":"));
            String host = value.substring(operation.length() + 1);
            this.operation = AclOperation.fromString(operation);
            this.host = host;
        } else {
            this.operation = AclOperation.fromString(value);
            this.host = ANY_HOSTS;
        }
    }

    /**
     * Creates a new {@link AccessOperationPolicy} instance.
     *
     * @param operation the {@link AclOperation}.
     */
    public AccessOperationPolicy(final AclOperation operation) {
        this(operation, ANY_HOSTS);
    }

    /**
     * Creates a new {@link AccessOperationPolicy} instance.
     *
     * @param operation the {@link AclOperation}.
     * @param host      the host.
     */
    public AccessOperationPolicy(@NotNull final AclOperation operation,
                                 @NotNull final String host) {
        this.operation =  Objects.requireNonNull(operation, "operation should be non-null");
        this.host = Objects.requireNonNull(host, "host should be non-null");
    }

    public String host() {
        return host;
    }

    public AclOperation operation() {
        return operation;
    }

    public String toLiteral() {
        return operation.name() + ":" + host;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessOperationPolicy that = (AccessOperationPolicy) o;
        return Objects.equals(host, that.host) &&
                operation == that.operation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, operation);
    }

    @JsonValue
    @Override
    public String toString() {
        return operation + ":" + host;
    }

    public static class Deserializer extends JsonDeserializer<AccessOperationPolicy> {

        /** {@inheritDoc} */
        @Override
        public AccessOperationPolicy deserialize(final JsonParser jsonParser,
                                   final DeserializationContext deserializationContext) {

            try {
               return AccessOperationPolicy.fromString(jsonParser.getValueAsString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Serializer extends JsonSerializer<AccessOperationPolicy> {

        /** {@inheritDoc} */
        @Override
        public void serialize(final AccessOperationPolicy object,
                              final JsonGenerator gen,
                              final SerializerProvider serializers) throws IOException {
            gen.writeString(object.toLiteral());
        }
    }
}