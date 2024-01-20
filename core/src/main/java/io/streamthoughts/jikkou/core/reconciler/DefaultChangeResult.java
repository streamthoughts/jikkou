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
