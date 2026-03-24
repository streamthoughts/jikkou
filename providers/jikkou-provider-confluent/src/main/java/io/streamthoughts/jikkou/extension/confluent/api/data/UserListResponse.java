/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.confluent.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.List;

/**
 * Response from {@code GET /iam/v2/users}.
 *
 * @param metadata Pagination metadata.
 * @param data     List of users.
 */
@Reflectable
public record UserListResponse(
    @JsonProperty("metadata") ListMetadata metadata,
    @JsonProperty("data") List<UserData> data
) {
}
