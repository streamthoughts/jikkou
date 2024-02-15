/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.printer;

import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import java.util.List;

public interface Printer {
    /**
     * Print the specified execution results to stdout and terminate the application with the appropriate exit name.
     *
     * @param result the reconciliation changes to print.
     * @param executionTimeMs the execution time in milliseconds.
     * @return the exit name.
     */
    int print(ApiChangeResultList result, long executionTimeMs);

    static int getNumberOfFailedChange(final List<ChangeResult> results) {
        return (int) results.stream()
                .filter(ChangeResult::isFailed)
                .count();

    }
}
