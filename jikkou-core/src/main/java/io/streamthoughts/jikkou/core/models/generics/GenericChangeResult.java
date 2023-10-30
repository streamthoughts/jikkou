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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.reconcilier.ChangeDescription;
import io.streamthoughts.jikkou.core.reconcilier.ChangeError;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import java.beans.ConstructorProperties;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize
@Reflectable
public record GenericChangeResult(@JsonProperty("end") Long end,
                                  @JsonProperty("errors") List<ChangeError> errors,
                                  @JsonProperty("status") Status status,
                                  @JsonProperty("data") GenericResourceChange data,
                                  @JsonProperty("description") ChangeDescription description)implements ChangeResult<GenericChange> {

    @ConstructorProperties({
            "end",
            "errors",
            "status",
            "data",
            "description"
    })
    public GenericChangeResult {
    }
}
