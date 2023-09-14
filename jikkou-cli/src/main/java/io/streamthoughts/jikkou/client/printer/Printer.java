/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.client.printer;

import io.streamthoughts.jikkou.api.change.Change;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import java.util.List;

public interface Printer {
    /**
     * Print the specified execution results to stdout and terminate the application with the appropriate exit code.
     *
     * @param results the reconciliation changes to print.
     * @param dryRun  is dry-run enabled.
     * @param executionTimeMs the execution time in milliseconds.
     * @return the exit code.
     */
    int print(List<ChangeResult<Change>> results, boolean dryRun, long executionTimeMs);

    static int getNumberOfFailedChange(final List<ChangeResult<Change>> results) {
        return (int) results.stream()
                .filter(ChangeResult::isFailed)
                .count();

    }
}
