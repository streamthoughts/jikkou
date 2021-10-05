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

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

public class MetaObject {

    private final Map<String, String> annotations;

    public static MetaObject defaults() {
        final MetaObject metaObject = new MetaObject();
        metaObject.setAnnotation("generated", Instant.now().toString());
        return metaObject;
    }

    public MetaObject() {
        this.annotations = new TreeMap<>();
    }

    @JsonCreator
    public MetaObject(@JsonProperty("annotations") final Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public MetaObject setAnnotation(final String key, final String value) {
        this.annotations.put(key, value);
        return this;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }
}
