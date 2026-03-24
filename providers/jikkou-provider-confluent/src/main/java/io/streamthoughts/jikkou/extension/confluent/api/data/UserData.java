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
 * Confluent Cloud IAM v2 User.
 *
 * @param id       User ID (e.g. {@code u-12345}).
 * @param email    User email.
 * @param fullName User full name.
 */
@Reflectable
public record UserData(
    @JsonProperty("id") String id,
    @JsonProperty("email") String email,
    @JsonProperty("full_name") String fullName
) {
}
