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
package io.streamthoughts.jikkou.core.models.generics;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.reconcilier.Change;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import java.beans.ConstructorProperties;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides a generic serializable/deserializable implementation of the {@link Change} interface.
 *
 * @param operation     the change operation.
 * @param additionalProperties    the change properties.
 */
@JsonDeserialize
@Reflectable
public record GenericChange(@JsonProperty("operation") ChangeType operation,
                            @JsonIgnore Map<String, Object> additionalProperties) implements Change {

    @ConstructorProperties({
            "operation"
    })
    public GenericChange(ChangeType operation) {
        this(operation, new LinkedHashMap<>());
    }

    @JsonAnyGetter
    public Map<String, Object> additionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
