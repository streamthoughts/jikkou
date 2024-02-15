/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.exceptions;

import java.util.Set;

public class DuplicateMetadataNameException extends JikkouRuntimeException {

    private final Set<String> duplicates;

    public DuplicateMetadataNameException(Set<String> duplicates) {
        super("Found duplicate resources for metadata.name: " + duplicates);
        this.duplicates = duplicates;
    }

    public Set<String> duplicates() {
        return duplicates;
    }
}
