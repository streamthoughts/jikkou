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
package io.streamthoughts.jikkou.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.beans.ConstructorProperties;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "values",
})
@Builder(builderMethodName = "builder", toBuilder = true)
public final class ObjectTemplate {

    private final Map<String, Object> values;

    public ObjectTemplate() {
        this(null);
    }

    /**
     * Creates a new {@link ObjectTemplate} instance.
     *
     * @param values    the values to be passed to the template engine.
     */
    @ConstructorProperties({
            "values",
    })
    public ObjectTemplate(final Map<String, Object> values) {
        this.values = values;
    }

    @JsonProperty("values")
    public Map<String, Object> getValues() {
        return values;
    }

    public Optional<Map<String, Object>> optionalValues() {
        return Optional.ofNullable(values);
    }
}
