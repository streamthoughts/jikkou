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
package io.streamthoughts.kafka.specs.resources;

import java.io.Serializable;
import java.util.Objects;

public class BrokerResource implements ClusterResource, Serializable {

    private final String id;
    private final String host;
    private final int port;
    private final String rack;

    private final Configs configs;

    public BrokerResource(final String id) {
        this(id, null, -1, null, new Configs());
    }

    public BrokerResource(final String id,
                          final String host,
                          final int port,
                          final String rack,
                          final Configs configs) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.rack = rack;
        this.configs = configs;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String rack() {
        return rack;
    }

    public Configs configs() {
        return configs;
    }

    public String id() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrokerResource)) return false;
        BrokerResource that = (BrokerResource) o;
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
