/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Schema Registry Subject Version.
 *
 * @param subject    Name of the subject that this schema is registered under
 * @param id         Globally unique identifier of the schema.
 * @param version    Version of the returned schema.
 * @param schemaType The schema format: AVRO is the default (if no schema type is shown on the response, the type is AVRO), PROTOBUF, JSON
 * @param schema     The schema string
 * @param references The schema references
 */
@Builder
@Jacksonized
@Reflectable
public record SubjectSchemaVersion(String subject,
                                   int id,
                                   int version,
                                   String schemaType,
                                   String schema,
                                   List<SubjectSchemaReference> references) {
}
