/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;

import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * List Schema Registry ACL entries - HTTP Response
 *
 * see <a href="https://api.aiven.io/doc/#tag/Service:_Kafka/operation/ServiceSchemaRegistryAclList">...</a>
 *
 * @param acl     List of Schema Registry ACL entries.
 * @param errors  List of errors occurred during request processing
 * @param message Printable result of the request
 */
@Reflectable
public record ListSchemaRegistryAclResponse(List<SchemaRegistryAclEntry> acl, List<Error> errors, String message) {

    @ConstructorProperties({
            "acl",
            "errors",
            "message"
    })
    public ListSchemaRegistryAclResponse {
    }

    @Override
    public List<Error> errors() {
        return Optional.ofNullable(errors).orElseGet(Collections::emptyList);
    }
}
