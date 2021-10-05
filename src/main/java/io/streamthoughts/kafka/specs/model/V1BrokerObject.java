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
import io.streamthoughts.kafka.specs.resources.ClusterResource;
import io.streamthoughts.kafka.specs.resources.Configs;

import java.io.Serializable;
import java.util.Objects;

public class V1BrokerObject implements ClusterResource, Serializable {

    private final String id;
    private final String host;
    private final int port;
    private final String rack;

    private final Configs configs;

    public static V1BrokerObject withBrokerId(final String id) {
        return new V1BrokerObject(id, null, -1, null, new Configs());
    }

    /**
     * Creates a new {@link V1BrokerObject} instance.
     *
     * @param id        the broker id.
     * @param host      the broker host.
     * @param port      the broker listener port.
     * @param rack      the broker rack.
     * @param configs   the configurations of the broker.
     */
    @JsonCreator
    public V1BrokerObject(@JsonProperty("id") final String id,
                          @JsonProperty("host") final String host,
                          @JsonProperty("port") final int port,
                          @JsonProperty("rack") final String rack,
                          @JsonProperty("configs") final Configs configs) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.rack = rack;
        this.configs = configs;
    }

    @JsonProperty
    public String host() {
        return host;
    }

    @JsonProperty
    public int port() {
        return port;
    }

    @JsonProperty
    public String rack() {
        return rack;
    }

    @JsonProperty
    public Configs configs() {
        return configs;
    }

    @JsonProperty
    public String id() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1BrokerObject)) return false;
        V1BrokerObject that = (V1BrokerObject) o;
        return port == that.port &&
                Objects.equals(id, that.id) &&
                Objects.equals(host, that.host) &&
                Objects.equals(rack, that.rack);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, host, port, rack);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String
    toString() {
        return "BrokerResource{" +
                "id='" + id + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", rack='" + rack + '\'' +
                ", configs=" + configs +
                '}';
    }
}