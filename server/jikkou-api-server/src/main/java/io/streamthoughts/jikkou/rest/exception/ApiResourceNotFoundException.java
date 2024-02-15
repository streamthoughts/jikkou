/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.exception;

import io.streamthoughts.jikkou.rest.models.ApiResourceIdentifier;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public final class ApiResourceNotFoundException extends JikkouServerException {

    private final ApiResourceIdentifier identifier;

    public ApiResourceNotFoundException(@NotNull ApiResourceIdentifier identifier) {
        super(String.format(
                "Resource type for apiVersion '%s/%s' and name '%s' is unknown.",
                identifier.group(),
                identifier.version(),
                identifier.plural()
        ));
        this.identifier = Objects.requireNonNull(identifier, "identifier must not be null");
    }

    public ApiResourceIdentifier identifier() {
        return identifier;
    }
}
