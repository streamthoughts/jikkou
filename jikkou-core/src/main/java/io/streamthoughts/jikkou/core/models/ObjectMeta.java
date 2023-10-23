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
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/**
 * Object Metadata
 *
 * @see HasMetadata
 */
@Reflectable
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "labels",
        "annotations"
})
public final class ObjectMeta implements Serializable {

    private final String name;

    private final Map<String, Object> labels;

    private final Map<String, Object> annotations;

    /**
     * Creates a new {@link ObjectMeta} instance.
     */
    public ObjectMeta() {
        this(null, null, null);
    }

    /**
     * Creates a new {@link ObjectMeta} instance with the specified name.
     *
     * @param name the object name.
     */
    public ObjectMeta(@Nullable final String name) {
        this(name, null, null);
    }

    /**
     * Creates a new {@link ObjectMeta} instance.
     *
     * @param name        the object name.
     * @param labels      the objet labels.
     * @param annotations the object annotations.
     */
    @ConstructorProperties({
            "name",
            "labels",
            "annotations"
    })
    public ObjectMeta(@Nullable final String name,
                      @Nullable final Map<String, Object> labels,
                      @Nullable final Map<String, Object> annotations) {
        this.name = name;
        this.labels = Optional.ofNullable(labels)
                .map(HashMap::new)
                .orElse(new HashMap<>());
        this.annotations =Optional.ofNullable(annotations)
                .map(HashMap::new)
                .orElse(new HashMap<>());
    }

    /**
     * Gets the name.
     *
     * @return the string name.
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Gets the labels.
     *
     * @return the key/value map.
     */
    @JsonProperty("labels")
    public Map<String, Object> getLabels() {
        return Collections.unmodifiableMap(labels);
    }

    /**
     * Gets the annotations.
     *
     * @return the key/value map.
     */
    @JsonProperty("annotations")
    public Map<String, Object> getAnnotations() {
        return Collections.unmodifiableMap(annotations);
    }

    /**
     * Finds the label value for the specified key.
     *
     * @param key   the label key. Must not be {@code null}.
     * @return  the optional value.
     */
    public Object getLabelByKey(final String key) {
        return findLabelByKey(key)
                .orElseThrow(() -> new NoSuchElementException("no label for key '" + key + "'"));
    }

    /**
     * Finds the label value for the specified key.
     *
     * @param key   the label key. Must not be {@code null}.
     * @return  the optional value.
     */
    public Optional<Object> findLabelByKey(final String key) {
        if (key == null) throw new IllegalArgumentException("key must not be null");
        return Optional.ofNullable(labels.get(key));
    }

    /**
     * Finds the annotation value for the specified key.
     *
     * @param key   the annotation key. Must not be {@code null}.
     * @return  the optional value.
     */
    public Optional<Object> findAnnotationByKey(final String key) {
        if (key == null) throw new IllegalArgumentException("key must not be null");
        return Optional.ofNullable(annotations.get(key));
    }

    /**
     * Add annotation if the specified key is not already associated with
     * a value (or is mapped to null) associate it with the given value.
     *
     * @param key   the annotation key. Must not be {@code null}.
     * @param value the annotation value.
     * @throws IllegalArgumentException if the passed key is equals to {@code null}.
     */
    public void addAnnotationIfAbsent(final String key,
                                      final Object value) {
        if (key == null) throw new IllegalArgumentException("key must not be null");
        this.annotations.putIfAbsent(key, value);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ObjectMeta[" +
                "name=" + name +
                ", labels=" + labels +
                ", annotations=" + annotations +
                ']';
    }

    /**
     * Creates a new {@link ObjectMetaBuilder} instance.
     *
     * @return the new {@link ObjectMetaBuilder} instance.
     */
    public static ObjectMetaBuilder builder() {
        return new ObjectMetaBuilder();
    }

    /**
     * Creates a new {@link ObjectMetaBuilder} initialized with this {@link ObjectMeta}.
     *
     * @return new {@link ObjectMetaBuilder} instance.
     */
    public ObjectMetaBuilder toBuilder() {
        return new ObjectMetaBuilder()
                .withName(name)
                .withAnnotations(annotations)
                .withLabels(labels);
    }

    /**
     * Builder to create new {@link ObjectMeta} objects.
     */
    public static final class ObjectMetaBuilder {

        private String name;
        private final Map<String, Object> labels = new HashMap<>();
        private final Map<String, Object> annotations = new HashMap<>();

        /**
         * Creates a new {@link ObjectMetaBuilder} instance.
         */
        private ObjectMetaBuilder() {
        }

        /**
         * Set the name.
         *
         * @param name the name.
         * @return {@code this} builder.
         */
        public ObjectMetaBuilder withName(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Add the specified labels.
         *
         * @param labels the labels.
         * @return {@code this} builder.
         */
        public ObjectMetaBuilder withLabels(final Map<String, Object> labels) {
            this.labels.putAll(labels);
            return this;
        }

        /**
         * Add label for the specified key and value.
         *
         * @param key   the key.
         * @param value the value.
         * @return {@code this} builder.
         */
        public ObjectMetaBuilder withLabel(final String key, final Object value) {
            this.labels.put(key, value);
            return this;
        }

        /**
         * Add the specified annotations.
         *
         * @param annotations the annotations.
         * @return {@code this} builder.
         */
        public ObjectMetaBuilder withAnnotations(final Map<String, Object> annotations) {
            this.annotations.putAll(annotations);
            return this;
        }

        /**
         * Add annotation for the specified key and value.
         *
         * @param key   the key.
         * @param value the value.
         * @return {@code this} builder.
         */
        public ObjectMetaBuilder withAnnotation(final String key, final Object value) {
            this.annotations.put(key, value);
            return this;
        }

        /**
         * Build a new {@link ObjectMeta}.
         *
         * @return a new {@link ObjectMeta}.
         */
        public ObjectMeta build() {
            return new ObjectMeta(
                    name,
                    labels,
                    annotations
            );
        }
    }
}
