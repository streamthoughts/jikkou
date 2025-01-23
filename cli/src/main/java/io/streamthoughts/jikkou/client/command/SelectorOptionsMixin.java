/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.core.selector.SelectorFactory;
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
        return selectorMatchingStrategy.combines(new SelectorFactory().make(expressions));
    }
}
