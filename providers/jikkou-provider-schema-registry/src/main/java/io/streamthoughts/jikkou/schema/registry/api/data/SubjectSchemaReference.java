/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;

@Reflectable
@JsonPropertyOrder({"name", "subject", "version"})
public record SubjectSchemaReference(
    @JsonProperty("name") String name,
    @JsonProperty("subject") String subject,
    @JsonProperty("version") int version) {

}
