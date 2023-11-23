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
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Selectors.
 */
public final class Selectors {

    public static final Selector NO_SELECTOR = new Selector() {
        /** {@inheritDoc} **/
        @Override
        public boolean apply(@NotNull HasMetadata resource) {
            return true;
        }
    };

    /**
     * Returns an aggregated selector to only select resources that match all the specified selectors.
     *
     * @param selectors The selectors.
     * @return  a new {@link Selector}.
     */
    public static Selector allMatch(@NotNull List<Selector> selectors) {
        return new AllMatchSelector(selectors);
    }

    /**
     * Returns an aggregated selector to select resources that many any of the specified selectors.
     *
     * @param selectors The selectors.
     * @return  a new {@link Selector}.
     */
    public static Selector anyMatch(@NotNull List<Selector> selectors) {
        return new AnyMatchSelector(selectors);
    }

    /**
     * Returns an aggregated selector to only select resources that match none of the specified selectors.
     *
     * @param selectors The selectors.
     * @return  a new {@link Selector}.
     */
    public static Selector noneMatch(@NotNull List<Selector> selectors) {
        return new NoneMatchSelector(selectors);
    }
}
