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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.annotation.Reflectable;
import java.util.Objects;

@Reflectable
public final class CompatibilityLevelObject {

    private final String compatibilityLevel;

    /**
     * Creates a new {@link CompatibilityLevelObject} instance.
     *
     * @param compatibilityLevel a compatibility level string.
     */
    @JsonCreator
    public CompatibilityLevelObject(@JsonProperty("compatibilityLevel") String compatibilityLevel) {
        this.compatibilityLevel = compatibilityLevel;
    }

    /**
     * Gets the compatibility level.
     *
     * @return  a compatibility level string.
     */
    @JsonProperty("compatibilityLevel")
    public String compatibilityLevel() {
        return compatibilityLevel;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompatibilityLevelObject that = (CompatibilityLevelObject) o;
        return Objects.equals(compatibilityLevel, that.compatibilityLevel);
    }
    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(compatibilityLevel);
    }
    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return "{" +
                "compatibilityLevel=" + compatibilityLevel +
                '}';
    }
}