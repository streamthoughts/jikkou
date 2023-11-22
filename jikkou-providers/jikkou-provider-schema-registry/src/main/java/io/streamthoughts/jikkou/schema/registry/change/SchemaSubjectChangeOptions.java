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
package io.streamthoughts.jikkou.schema.registry.change;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

/**
 * Represents options that can be used to apply a change.
 */
@Getter
@AllArgsConstructor
@With
public class SchemaSubjectChangeOptions {

    private boolean isPermanentDeleteEnabled;

    private boolean isSchemaOptimizationEnabled;

    /**
     * Creates a new {@link SchemaSubjectChangeOptions} instance.
     */
    public SchemaSubjectChangeOptions() {
        this(false, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaSubjectChangeOptions that = (SchemaSubjectChangeOptions) o;
        return isPermanentDeleteEnabled == that.isPermanentDeleteEnabled &&
                isSchemaOptimizationEnabled == that.isSchemaOptimizationEnabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isPermanentDeleteEnabled, isSchemaOptimizationEnabled);
    }

    @Override
    public String toString() {
        return "SchemaSubjectChangeOptions{" +
                "isPermanentDeleteEnabled=" + isPermanentDeleteEnabled +
                ", isSchemaOptimizationEnabled=" + isSchemaOptimizationEnabled +
                '}';
    }
}
