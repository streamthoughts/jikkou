/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.List;


/**
 * A default change result.
 *
 * @param end
 * @param status      The change status.
 * @param change      The resource change.
 * @param description The description of the change.
 * @param errors      The errors.
 */
@JsonPropertyOrder({
        "end",
        "status",
        "data",
        "description",
        "errors"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize
@Reflectable
public record DefaultChangeResult(@JsonProperty("end") Instant end,
                                  @JsonProperty("status") Status status,
                                  @JsonProperty("change") ResourceChange change,
                                  @JsonProperty("description") TextDescription description,
                                  @JsonProperty("errors") List<ChangeError> errors) implements ChangeResult {

    @ConstructorProperties({
            "end",
            "errors",
            "status",
            "data",
            "description"
    })
    public DefaultChangeResult {
    }
}
