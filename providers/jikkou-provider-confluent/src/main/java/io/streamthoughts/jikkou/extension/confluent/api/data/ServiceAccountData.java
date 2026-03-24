/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.confluent.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;

/**
 * Confluent Cloud IAM v2 Service Account.
 *
 * @param id          Service account ID (e.g. {@code sa-12345}).
 * @param displayName Service account display name.
 * @param description Service account description.
 */
@Reflectable
public record ServiceAccountData(
    @JsonProperty("id") String id,
    @JsonProperty("display_name") String displayName,
    @JsonProperty("description") String description
) {
}
