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

import io.streamthoughts.jikkou.core.extension.ExtensionDecorator;
import io.streamthoughts.jikkou.core.reconcilier.Change;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
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
    public void report(List<ChangeResult<Change>> results) {
        this.extension.report(results);
    }
}
