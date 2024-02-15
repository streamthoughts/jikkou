/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;

/**
 * A globally unique identifier of the schema
 *
 * @param id a schema's id
 */
@Reflectable
public record SubjectSchemaId(int id) {

    @ConstructorProperties({
            "id",
    })
    public SubjectSchemaId {}

}
