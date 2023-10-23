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
                                   List<String> references) {
}
