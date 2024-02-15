/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.models;

/**
 * ApiResourceIdentifier.
 *
 * @param group     the API group.
 * @param version   the API version.
 * @param plural    the name of the resource (e.g. plural).
 */
public record ApiResourceIdentifier(String group,
                                    String version,
                                    String plural) {
}
