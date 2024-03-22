/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;

import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.List;

@Reflectable
public record KafkaTopicListResponse(
    List<KafkaTopicInfoGet> topics,
    List<Error> errors,
    String message){

    @ConstructorProperties({
        "topics",
        "errors",
        "message"
    })
    public KafkaTopicListResponse {
    }
}
