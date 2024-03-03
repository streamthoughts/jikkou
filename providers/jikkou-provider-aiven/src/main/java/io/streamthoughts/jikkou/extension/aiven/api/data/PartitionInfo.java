/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;

@Reflectable
public record PartitionInfo(
    @JsonProperty("earliest_offset") Integer earliestOffset,
    @JsonProperty("latest_offset") Integer latestOffset,
    @JsonProperty("partition") Integer partition,
    @JsonProperty("size") Integer size
) {
}
