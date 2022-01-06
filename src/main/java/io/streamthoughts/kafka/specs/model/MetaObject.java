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
import io.streamthoughts.kafka.specs.internal.CollectionUtils;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

public class MetaObject implements Serializable {

    public static final String ANNOT_RESOURCE = "resource";
    public static final String ANNOT_GENERATED = "generated";

    private final Map<String, Object> annotations = new TreeMap<>();

    private final Map<String, Object> labels = new TreeMap<>();

    public static MetaObject defaults() {
        final MetaObject metaObject = new MetaObject();
        metaObject.setAnnotation(ANNOT_GENERATED, Instant.now().toString());
        return metaObject;
    }

    public MetaObject() {}

    @JsonCreator
    public MetaObject(@JsonProperty("annotations") final Map<String, Object> annotations,
                      @JsonProperty("labels") final Map<String, Object> labels) {
        CollectionUtils.toFlattenMap(this.annotations, Optional.ofNullable(annotations).orElse(new HashMap<>()), null);
        CollectionUtils.toFlattenMap(this.labels, Optional.ofNullable(labels).orElse(new HashMap<>()), null);
    }

    public MetaObject setLabels(final Map<String, Object> labels) {
        this.labels.putAll(labels);
        return this;
    }

    public MetaObject setAnnotations(final Map<String, Object> annotations) {
        this.annotations.putAll(annotations);
        return this;
    }

    public MetaObject setAnnotation(final String key, final String value) {
        this.annotations.put(key, value);
        return this;
    }

    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    public Map<String, Object> getLabels() {
        return labels;
    }

}
