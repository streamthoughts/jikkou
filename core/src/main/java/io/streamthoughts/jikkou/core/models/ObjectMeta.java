/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
public class ObjectMeta implements Nameable<ObjectMeta>, Serializable {

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
     * @param name        The object name.
     * @param labels      The objet labels.
     * @param annotations The object annotations.
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
        this.annotations = Optional.ofNullable(annotations)
                .map(HashMap::new)
                .orElse(new HashMap<>());
    }

    /**
     * Gets the name.
     *
     * @return The string name.
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /** {@inheritDoc} **/
    @Override
    public ObjectMeta withName(String name) {
        return new ObjectMeta(name, labels, annotations);
    }

    /**
     * Gets the labels.
     *
     * @return The key/value map.
     */
    @JsonProperty("labels")
    public Map<String, Object> getLabels() {
        return Collections.unmodifiableMap(labels);
    }

    /**
     * Gets the annotations.
     *
     * @return The key/value map.
     */
    @JsonProperty("annotations")
    public Map<String, Object> getAnnotations() {
        return Collections.unmodifiableMap(annotations);
    }

    /**
     * Get the label value for the specified key.
     *
     * @param key The label key. Must not be {@code null}.
     * @return The optional value.
     */
    public NamedValue getLabelByKey(final String key) {
        return findLabelByKey(key)
                .orElseThrow(() -> new NoSuchElementException("Cannot found label for key '" + key + "'"));
    }

    /**
     * Finds the label value for the specified key.
     *
     * @param key The label key. Must not be {@code null}.
     * @return The optional value.
     */
    public Optional<NamedValue> findLabelByKey(final String key) {
        if (key == null) throw new IllegalArgumentException("key must not be null");
        return Optional.ofNullable(labels.get(key)).map(val -> new NamedValue(key, val));
    }

    /**
     * Checks whether a label exists for the specified key.
     *
     * @param key The label key. Must not be {@code null}.
     * @return {@code true} if the label is present, otherwise {@code false}.
     */
    public boolean hasLabel(final String key) {
        return findLabelByKey(key).isPresent();
    }

    /**
     * Get the label value for the specified key.
     *
     * @param key The label key. Must not be {@code null}.
     * @return The optional value.
     */
    public NamedValue getAnnotationByKey(final String key) {
        return findAnnotationByKey(key)
            .map(val -> new NamedValue(key, val))
            .orElseThrow(() -> new NoSuchElementException("Cannot found annotation for key '" + key + "'"));
    }

    /**
     * Add label if the specified key is not already associated with
     * a value (or is mapped to null) associate it with the given value.
     *
     * @param key   the label key. Must not be {@code null}.
     * @param value the label value.
     * @throws IllegalArgumentException if the passed key is equals to {@code null}.
     */
    public void addLabelIfAbsent(final String key,
                                 final Object value) {
        if (key == null) throw new IllegalArgumentException("key must not be null");
        this.labels.putIfAbsent(key, value);
    }

    /**
     * Finds the annotation value for the specified key.
     *
     * @param key the annotation key. Must not be {@code null}.
     * @return the optional value.
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
