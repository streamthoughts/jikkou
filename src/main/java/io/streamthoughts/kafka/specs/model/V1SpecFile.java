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

import java.io.Serializable;
import java.util.Optional;

/**
 * The Kafka cluster specification.
 */
public class V1SpecFile implements Serializable {

    public static final String VERSION = "1";

    private final MetaObject metadata;

    private final V1SpecsObject specs;

    /**
     * Creates a new {@link V1SpecFile} instance.
     *
     * @param metadata  the {@link MetaObject}.
     * @param specs     the {@link V1SpecsObject}.
     */
    @JsonCreator
    public V1SpecFile(@Nullable @JsonProperty("metadata") final MetaObject metadata,
                      @Nullable @JsonProperty("specs") final V1SpecsObject specs) {
        this.metadata = Optional.ofNullable(metadata).orElse(new MetaObject());
        this.specs = Optional.ofNullable(specs).orElse(new V1SpecsObject());
    }

    @JsonProperty
    public MetaObject metadata() {
        return metadata;
    }

    @JsonProperty
    public V1SpecsObject specs() {
        return specs;
    }

    @JsonProperty
    public String version() {
        return VERSION;
    }
}
