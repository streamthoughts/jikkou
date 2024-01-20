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
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.core.selector.ExpressionSelectorFactory;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.core.selector.SelectorMatchingStrategy;
import picocli.CommandLine.Option;

public final class SelectorOptionsMixin {

    @Option(names = {"--selector", "-s"},
            description = "The selector expression used for including or excluding resources.")
    public String[] expressions;

    @Option(names = {"--selector-match"},
            defaultValue = "ALL",
            description = "The selector matching strategy. Valid values: ${COMPLETION-CANDIDATES} (default: ALL)")
    public SelectorMatchingStrategy selectorMatchingStrategy;

    public Selector getResourceSelector() {
        return selectorMatchingStrategy.combines(new ExpressionSelectorFactory().make(expressions));
    }
}
