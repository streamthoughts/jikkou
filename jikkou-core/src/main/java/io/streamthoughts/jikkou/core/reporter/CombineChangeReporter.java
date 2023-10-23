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
package io.streamthoughts.jikkou.core.reporter;

import io.streamthoughts.jikkou.core.change.Change;
import io.streamthoughts.jikkou.core.change.ChangeResult;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CombineChangeReporter implements ChangeReporter {

    private static final Logger LOG = LoggerFactory.getLogger(CombineChangeReporter.class);

    private final List<ChangeReporter> reporters;

    /**
     * Creates a new {@link CombineChangeReporter} instance.
     *
     * @param reporters a list of reporter.
     */
    public CombineChangeReporter(@NotNull final List<ChangeReporter> reporters) {
        this.reporters = new ArrayList<>(reporters);
    }

    /** {@inheritDoc} **/
    @Override
    public void report(List<ChangeResult<Change>> results) {
        for (ChangeReporter reporter : reporters) {
            try {
                reporter.report(results);
            } catch (Exception e) {
                LOG.error("Failed to report applied changes using: '{}'", reporter.getName(), e);
            }
        }
    }
}
