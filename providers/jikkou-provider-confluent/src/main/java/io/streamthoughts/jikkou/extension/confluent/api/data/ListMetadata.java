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
 * Pagination metadata from Confluent Cloud list responses.
 *
 * @param totalSize  Total number of results.
 * @param pageToken  Token for the next page, or {@code null} if no more pages.
 */
@Reflectable
public record ListMetadata(
    @JsonProperty("total_size") Integer totalSize,
    @JsonProperty("next") String pageToken
) {
}
