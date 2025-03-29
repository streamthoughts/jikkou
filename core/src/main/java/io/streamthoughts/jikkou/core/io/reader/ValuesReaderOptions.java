/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io.reader;

public record ValuesReaderOptions(
    String pattern
) {

    public static ValuesReaderOptions of(String pattern) {
        return new ValuesReaderOptions(pattern);
    }
}
