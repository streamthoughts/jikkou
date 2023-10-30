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
package io.streamthoughts.jikkou.core.health;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Health} wraps information about a service or sub-system.
 */
@Reflectable
@JsonPropertyOrder({
        "name",
        "status",
        "details",
})
@JsonInclude(Include.NON_EMPTY)
public final class Health {

    private final String name;
    private final HealthStatus status;
    private final Map<String, Object> details;

    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new {@link Health} instance.
     *
     * @param name    The status indicator name (maybe {@code null}).
     * @param status  The {@link HealthStatus} instance (cannot be {@code null}).
     * @param details The status indicator details (cannot be {@code null}).
     */
    private Health(@Nullable final String name,
                   @Nullable final HealthStatus status,
                   @Nullable final Map<String, Object> details) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(details);
        this.name = name;
        this.status = status;
        this.details = Collections.unmodifiableMap(details);
    }

    /**
     * Gets the health indicator name.
     *
     * @return the name (maybe {@code null}).
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the health indicator status.
     *
     * @return the {@link HealthStatus} (cannot be {@code null}).
     */
    @JsonUnwrapped
    public HealthStatus getStatus() {
        return status;
    }

    /**
     * Gets the health indicator details.
     *
     * @return the details or an empty map.
     */
    @JsonInclude(Include.NON_EMPTY)
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Health health)) return false;
        return Objects.equals(name, health.name) &&
                Objects.equals(status, health.status) &&
                Objects.equals(details, health.details);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, status, details);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Health[" +
                "name=" + name +
                ", status=" + status +
                ", details=" + details +
                ']';
    }

    /**
     * Builder for creating new {@link Health} instances.
     */
    public static class Builder {

        private static final Logger LOG = LoggerFactory.getLogger(Builder.class);

        private String name;
        private HealthStatus status;
        private final Map<String, Object> details;

        /**
         * Creates a new {@link Builder} instance.
         */
        public Builder() {
            status = HealthStatus.UNKNOWN;
            details = new LinkedHashMap<>();
        }

        /**
         * Sets the status for the {@link Health} indicator to be built to {@link HealthStatus#DOWN}.
         *
         * @return this {@link Builder} instance.
         */
        public Builder down() {
            status = HealthStatus.DOWN;
            return this;
        }

        /**
         * Sets the status for the {@link Health} indicator to be built to {@link HealthStatus#UP}.
         *
         * @return this {@link Builder} instance.
         */
        public Builder up() {
            status = HealthStatus.UP;
            return this;
        }

        /**
         * Sets the status for the {@link Health} indicator to be built {@link HealthStatus#UNKNOWN}.
         *
         * @return this {@link Builder} instance.
         */
        public Builder unknown() {
            status = HealthStatus.UNKNOWN;
            return this;
        }

        /**
         * Sets the name of service or sub-system.
         *
         * @param name the name.
         * @return this {@link Builder} instance.
         */
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds a details entry for the {@link Health} indicator.
         *
         * @param details the details.
         * @return this {@link Builder} instance.
         */
        public Builder details(final Map<String, ?> details) {
            this.details.putAll(details);
            return this;
        }

        /**
         * Adds a details entry for the {@link Health} indicator.
         *
         * @param key   the detail key.
         * @param value the detail value.
         * @return this {@link Builder} instance.
         */
        public Builder details(final String key, final Object value) {
            Objects.requireNonNull(key, "'key' should not be null");
            Objects.requireNonNull(value, "'value' should not be null");
            details.put(key, value);
            return this;
        }

        /**
         * Sets the exception for the {@link Health} indicator.
         *
         * @param ex The exception.
         * @return this {@link Builder} instance.
         */
        public Builder exception(final @NotNull Throwable ex) {
            Map<String, String> error = new HashMap<>(1);
            final String message = ex.getClass().getName() + ": " + ex.getMessage();
            error.put("error", message);
            if (LOG.isErrorEnabled()) {
                LOG.error("Health indicator [" + name + "] reported exception: " + message, ex);
            }
            return details(error);
        }

        /**
         * Sets the {@link HealthStatus} for the {@link Health} indicator.
         *
         * @param status the status.
         * @return this {@link Builder} instance.
         */
        public Builder status(final HealthStatus status) {
            Objects.requireNonNull(status, "status cannot be null");
            this.status = status;
            return this;
        }

        /**
         * Builds a new {@link Health} instance.
         *
         * @return the {@link Health} instance.
         */
        public Health build() {
            return new Health(name, status, details);
        }
    }
}