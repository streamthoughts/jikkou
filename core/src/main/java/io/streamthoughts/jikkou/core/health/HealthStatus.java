/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import jakarta.validation.constraints.NotNull;
import java.beans.ConstructorProperties;
import java.util.Objects;

/**
 * The status of a health indicator.
 *
 * @param name        The name of the status.
 * @param description The detailed information about the status
 */
@JsonPropertyOrder({
        "name",
        "description"
})
@Reflectable
public record HealthStatus(@NotNull @JsonProperty("name") String name,
                           @NotNull @JsonProperty("description") String description) {

    public static final HealthStatus UNKNOWN = new HealthStatus("UNKNOWN", null);

    public static final HealthStatus UP = new HealthStatus("UP", null);
    public static final HealthStatus DOWN = new HealthStatus("DOWN", null);

    /**
     * Creates a new {@link HealthStatus} instance.
     */
    @ConstructorProperties({
            "name",
            "description"
    })
    public HealthStatus {
        Objects.requireNonNull(name, "name cannot be null");
    }
}