/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reporter;

import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
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
    public void report(List<ChangeResult> results) {
        for (ChangeReporter reporter : reporters) {
            try {
                reporter.report(results);
            } catch (Exception e) {
                LOG.error("Failed to report applied changes using: '{}'", reporter.getName(), e);
            }
        }
    }
}
