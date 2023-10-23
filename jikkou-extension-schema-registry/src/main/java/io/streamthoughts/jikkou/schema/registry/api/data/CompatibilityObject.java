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
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import org.jetbrains.annotations.NotNull;

/**
 * CompatibilityObject.
 *
 * @param compatibility a compatibility level string.
 */
@Reflectable
public record CompatibilityObject(@NotNull String compatibility) {

    /**
     * Creates a new {@link CompatibilityObject} instance.
     */
    @ConstructorProperties({
            "compatibility",
    })
    public CompatibilityObject { }

    /**
     * Gets the compatibility level.
     *
     * @return a compatibility level string.
     */
    @Override
    @JsonProperty("compatibility")
    public String compatibility() {
        return compatibility;
    }

}
