/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import jakarta.validation.constraints.NotNull;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * ApiResource.
 *
 * @param name          the name of the resource.
 * @param kind          the Kind of the resource.
 * @param singularName  the singular name of the resource.
 * @param shortNames    the short names of the resource.
 * @param description   the description of the resource.
 * @param verbs         the verbs supported by the resource.
 * @param metadata      the resource metadata.
 */
@JsonPropertyOrder( {
        "name",
        "kind",
        "singularName",
        "shortNames",
        "description",
        "verbs",
        "metadata"}
)
@Reflectable
public record ApiResource (
        @NotNull @JsonProperty("name") String name,
        @NotNull @JsonProperty("kind") String kind,
        @JsonProperty("singularName") String singularName,
        @JsonProperty("shortNames") Set<String> shortNames,
        @JsonProperty("description") String description,
        @JsonProperty("verbs") Set<String> verbs,
        @JsonProperty("verbsOptions") List<ApiResourceVerbOptionList> verbsOptions,
        @JsonProperty("metadata") Map<String, Object> metadata) {

    @ConstructorProperties({
            "name",
            "kind",
            "singularName",
            "shortNames",
            "description",
            "verbs",
            "verbsOptions",
            "metadata"})
    public ApiResource {
    }

    public ApiResource(
            String name,
            String kind,
            String singularName,
            Set<String> shortNames,
            String description,
            Set<String> verbs) {
        this(name, kind, singularName, shortNames, description, verbs, null, Collections.emptyMap());
    }

    public ApiResource(
            String name,
            String kind,
            String singularName,
            Set<String> shortNames,
            String description,
            Set<String> verbs,
            List<ApiResourceVerbOptionList> verbsOptions) {
        this(name, kind, singularName, shortNames, description, verbs, verbsOptions, Collections.emptyMap());
    }

    public ApiResource withApiResourceVerbOptionList(final ApiResourceVerbOptionList list) {
        Objects.requireNonNull(list, "list must not be null");
        if (!isVerbSupported(list.verb())) {
            throw new IllegalArgumentException(
                    "Cannot add options. Verb '" + list.verb() + "' is not supported by this resource"
            );
        }
        List<ApiResourceVerbOptionList> options = Optional
                .ofNullable(verbsOptions)
                .map(ArrayList::new)
                .orElse(new ArrayList<>());
        options.add(list);
        return new ApiResource(name, kind, singularName, shortNames, description, verbs, options);
    }

    /**
     * Verify if the specified verb is supported by this resource.
     *
     * @param verb  the verb.
     * @return  {@code true} of the specified verb is contained in {@link #verbs()}.
     */
    public boolean isVerbSupported(final Verb verb) {
        return isVerbSupported(verb.value());
    }

    /**
     * Verify if the specified verb is supported by this resource.
     *
     * @param verb  the verb.
     * @return  {@code true} of the specified verb is contained in {@link #verbs()}.
     */
    public boolean isVerbSupported(final String verb) {
        if (verbs == null) return false;
        return verbs.stream().anyMatch(v -> v.equalsIgnoreCase(verb));
    }

    /**
     * Gets the {@link ApiResourceVerbOptionList} for the specified verb.
     *
     * @param verb  the verb.
     * @return an optional {@link ApiResourceVerbOptionList}.
     *
     * @throws NullPointerException if the specified verb is {@code null}.
     */
    public Optional<ApiResourceVerbOptionList> getVerbOptionList(final Verb verb) {
        Objects.requireNonNull(verb, "verb must not be null");

        if (verbsOptions == null || verbsOptions.isEmpty())
            return Optional.empty();

        return verbsOptions.stream()
                .filter(verbsOptions -> verbsOptions.verb().equalsIgnoreCase(verb.value()))
                .findFirst();
    }

    /** {@inheritDoc} **/
    @Override
    public Set<String> shortNames() {
        return Optional.ofNullable(shortNames).orElse(Collections.emptySet());
    }

    /** {@inheritDoc} **/
    @Override
    public Set<String> verbs() {
        return Optional.ofNullable(verbs).orElse(Collections.emptySet());
    }
}
