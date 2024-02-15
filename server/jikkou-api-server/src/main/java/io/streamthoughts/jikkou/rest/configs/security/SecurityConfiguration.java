/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.configs.security;

import io.micronaut.context.annotation.ConfigurationProperties;
import java.util.List;

/**
 * Immutable configuration for property 'jikkou.security'.
 */
@ConfigurationProperties("jikkou.security")
public interface SecurityConfiguration {

    List<BasicAuthCredentials> getBasicAuth();
}
