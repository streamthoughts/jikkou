/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.completion;

import java.util.Arrays;
import java.util.Iterator;

public class ContextNameCompletions implements Iterable<String> {

    /** {@inheritDoc} */
    @Override
    public Iterator<String> iterator() {
        return Arrays.asList("jikkou", "context-name-completions").iterator();
    }
}
