/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reporter;

import io.streamthoughts.jikkou.core.extension.ExtensionDecorator;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import java.util.List;

/**
 * This class can be used to decorate a change reporter with a different name.
 *
 * @see ChangeReporterDecorator
 */
public class ChangeReporterDecorator extends ExtensionDecorator<ChangeReporter, ChangeReporterDecorator> implements ChangeReporter {

    /**
     * Creates a new {@link ChangeReporterDecorator} instance.
     *
     * @param extension the extension; must not be null.
     */
    public ChangeReporterDecorator(ChangeReporter extension) {
        super(extension);
    }

    @Override
    public void report(List<ChangeResult> results) {
        this.extension.report(results);
    }
}
