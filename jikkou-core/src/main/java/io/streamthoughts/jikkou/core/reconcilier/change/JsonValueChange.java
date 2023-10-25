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
package io.streamthoughts.jikkou.core.reconcilier.change;

import io.streamthoughts.jikkou.common.utils.Json;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonValueChange extends ValueChange<String> {

    /**
     * Static helper method to create a new {@link ValueChange} instance.
     *
     * @param value the before/after value.
     * @return a new {@link JsonValueChange}
     */
    public static JsonValueChange none(@Nullable String value) {
        String canonicalize = Json.normalize(value);
        return new JsonValueChange(ValueChange.none(canonicalize));
    }

    /**
     * Static helper method to create a new {@link ValueChange} instance.
     *
     * @param value the target value.
     * @return a new {@link JsonValueChange}
     */
    public static JsonValueChange withAfterValue(@Nullable final String value) {
        return with(null, value);
    }

    /**
     * Static helper method to create a new {@link ValueChange} instance.
     *
     * @param value the previous value.
     * @return a new {@link JsonValueChange}
     */
    public static JsonValueChange withBeforeValue(@Nullable final String value) {
        return with(value, null);
    }

    /**
     * Static helper method to create a new {@link ValueChange} instance.
     *
     * @param before the previous value.
     * @param after  the target value.
     * @return a new {@link JsonValueChange}
     */
    public static JsonValueChange with(@Nullable String before,
                                       @Nullable String after) {
        return new JsonValueChange(ValueChange.with(Json.normalize(before), Json.normalize(after)));
    }

    /**
     * Creates a new {@link JsonValueChange} instance.
     */
    protected JsonValueChange(@NotNull ValueChange<String> change) {
        super(change);
    }

}
