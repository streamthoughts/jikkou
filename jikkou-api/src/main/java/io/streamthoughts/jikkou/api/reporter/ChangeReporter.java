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
package io.streamthoughts.jikkou.api.reporter;

import io.streamthoughts.jikkou.annotation.Reflectable;
import io.streamthoughts.jikkou.api.change.Change;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.Extension;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to report changes applied by Jikkou to a third-party system.
 */
@Reflectable
public interface ChangeReporter extends Extension, AutoCloseable {

    /**
     * Reports the given change results.
     *
     * @param results  the change-results to be reported.
     */
    void report(final List<ChangeResult<Change>> results);

    /**
     * Configure this {@code ChangeReporter}.
     *
     * @param configuration  the {@link Configuration} instance used to configure this instance.
     */
    @Override
    default void configure(final @NotNull Configuration configuration) {}

    /** Close any resource used by this reporter **/
    @Override
    default void close() {}
}
