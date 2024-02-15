/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest;

import io.streamthoughts.jikkou.rest.data.Info;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    void shouldGetProjectInfo() {
        Info info = Project.info();
        Assertions.assertNotNull(info.version());
        Assertions.assertNotNull(info.buildTimestamp());
        Assertions.assertNotNull(info.commitId());
    }
}