/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaVersion;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * GET/ Schema Registry Subject version - HTTP-response
 *
 * @param errors   List of errors occurred during request processing
 * @param message  Printable result of the request
 */
@Reflectable
public record SubjectSchemaVersionResponse(@JsonProperty("version") @NotNull SubjectSchemaVersion version,
                                           @JsonProperty("errors") @Nullable List<Error> errors,
                                           @JsonProperty("message") @Nullable String message
) {

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<Error> errors() {
        return Optional.ofNullable(errors).orElseGet(Collections::emptyList);
    }

}
