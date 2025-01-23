/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * List Kafka quotas - HTTP Response
 *
 * see <a href="https://api.aiven.io/doc/#tag/Service:_Kafka/operation/ServiceKafkaQuotaList">...</a>
 *
 * @param quotas  List of Kafka quot entries.
 * @param errors  List of errors occurred during request processing.
 * @param message Printable result of the request.
 */
@Reflectable
public record ListKafkaQuotaResponse(
    @JsonProperty("quotas") List<KafkaQuotaEntry> quotas,
    @JsonProperty("errors") List<Error> errors,
    @JsonProperty("message") String message) {

    @Override
    public List<Error> errors() {
        return Optional.ofNullable(errors).orElseGet(Collections::emptyList);
    }
}
