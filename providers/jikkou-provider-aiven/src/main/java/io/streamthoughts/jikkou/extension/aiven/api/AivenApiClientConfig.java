/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api;

public record AivenApiClientConfig(
    String apiUrl,
    String tokenAuth,
    String project,
    String service,
    boolean debugLoggingEnabled
) {
}
