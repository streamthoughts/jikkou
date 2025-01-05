/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models.generics;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.BaseHasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ObjectTemplate;
import java.beans.ConstructorProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Provides a generic serializable/deserializable implementation of the {@link HasMetadata} interface.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "apiVersion",
        "kind",
        "metadata",
        "template"
})
@JsonDeserialize
@Reflectable
public class GenericResource extends BaseHasMetadata implements HasMetadata {

    @JsonProperty("template")
    private final ObjectTemplate template;

    @JsonIgnore
    private final Map<String, Object> additionalProperties;

    /**
     * Creates a new {@link GenericResource} instance.
     *
     * @param apiVersion The resource API Version.
     * @param kind       The resource Kind.
     * @param metadata   The resource metadata.
     * @param template   The resource template.
     */
    @ConstructorProperties({
            "apiVersion",
            "kind",
            "metadata",
            "template"
    })
    public GenericResource(final String apiVersion,
                           final String kind,
                           final ObjectMeta metadata,
                           final ObjectTemplate template) {
        this(apiVersion, kind, metadata, template, new LinkedHashMap<>());
    }

    /**
     * Creates a new {@link GenericResource} instance.
     *
     * @param apiVersion           The resource API Version.
     * @param kind                 The resource Kind.
     * @param metadata             The resource metadata.
     * @param template             The resource template.
     * @param additionalProperties The additional properties.
     */
    public GenericResource(final String apiVersion,
                           final String kind,
                           final ObjectMeta metadata,
                           final ObjectTemplate template,
                           final Map<String, Object> additionalProperties) {
        super(apiVersion, kind, metadata);
        this.template = template;
        this.additionalProperties = additionalProperties;
    }

    /**
     * (Required)
     */
    @Override
    public HasMetadata withMetadata(final ObjectMeta metadata) {
        return new GenericResource(apiVersion, kind, metadata, template);
    }

    @JsonProperty("template")
    public ObjectTemplate getObjectTemplate() {
        return template;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GenericResource that = (GenericResource) o;
        return Objects.equals(template, that.template) && Objects.equals(additionalProperties, that.additionalProperties);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), template, additionalProperties);
    }
}
