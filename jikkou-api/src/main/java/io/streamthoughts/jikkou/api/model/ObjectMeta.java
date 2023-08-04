/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.jetbrains.annotations.Nullable;

@Reflectable
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "labels",
        "annotations"
})
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
public final class ObjectMeta implements Serializable, Nameable {

    public static final String ANNOT_RESOURCE = "jikkou.io/resource-location";
    public static final String ANNOT_GENERATED = "jikkou.io/resource-generated";

    private final String name;

    @Singular
    private Map<String, Object> labels;

    @Singular
    private Map<String, Object> annotations;

    public ObjectMeta() {
        this(null, null, null);
    }

    @ConstructorProperties({
            "name",
            "labels",
            "annotations"
    })
    public ObjectMeta(@Nullable final String name,
                      @Nullable @Singular final Map<String, Object> labels,
                      @Nullable @Singular final Map<String, Object> annotations) {
        this.name = name;
        this.labels = Optional.ofNullable(labels).orElse(Collections.emptyMap());
        this.annotations = Optional.ofNullable(annotations).orElse(Collections.emptyMap());
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("labels")
    public Map<String, Object> getLabels() {
        return labels;
    }

    @JsonProperty("annotations")
    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    public Optional<Object> getAnnotation(final String key) {
        return Optional.ofNullable(annotations.get(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectMeta that = (ObjectMeta) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(labels, that.labels) &&
               Objects.equals(annotations, that.annotations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, labels, annotations);
    }

    @Override
    public String toString() {
        return "ObjectMeta{" +
                "name=" + name +
                ", labels=" + labels +
                ", annotations=" + annotations +
                '}';
    }
}
