/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reporter;

import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.annotations.Category;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import java.util.List;

/**
 * Interface used to report changes applied by Jikkou to a third-party system.
 */
@Reflectable
@Category(ExtensionCategory.REPORTER)
public interface ChangeReporter extends Extension {

    /**
     * Reports the given change results.
     *
     * @param results the change-results to be reported.
     */
    void report(final List<ChangeResult> results);

}
