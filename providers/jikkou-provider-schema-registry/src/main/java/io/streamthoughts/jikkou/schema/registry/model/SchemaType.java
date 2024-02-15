/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.streamthoughts.jikkou.common.utils.Enums;
import org.jetbrains.annotations.Nullable;

public enum SchemaType {

    AVRO, PROTOBUF, JSON;

    @JsonCreator
    public static SchemaType getForNameIgnoreCase(final @Nullable String str) {
        if (str == null) return AVRO;
        return Enums.getForNameIgnoreCase(str, SchemaType.class);
    }

    public static SchemaType defaultType() {
        return SchemaType.AVRO;
    }

}
