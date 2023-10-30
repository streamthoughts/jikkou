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
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Set;
import javax.validation.constraints.NotNull;

@JsonPropertyOrder({
        "name",
        "versions"
})
@Reflectable
public record ApiGroup(@JsonProperty("name") @NotNull String name,
                       @JsonProperty("versions") @NotNull Set<ApiGroupVersion> versions) {

    @ConstructorProperties({
            "name",
            "versions"
    })
    public ApiGroup {
    }
}
