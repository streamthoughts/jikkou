/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChangeMetadataTest {

    @Test
    void shouldGetEmptyGIvenNoError() {
        Assertions.assertTrue(new ChangeMetadata().getError().isEmpty());
    }

    @Test
    void shouldIncludeRootCauseInErrorMessage() {
        // Simulate ExceptionInInitializerError wrapping a real cause
        RuntimeException root = new RuntimeException("HADOOP_HOME is unset");
        ExceptionInInitializerError wrapper = new ExceptionInInitializerError(root);

        ChangeMetadata metadata = ChangeMetadata.of(wrapper);

        String message = metadata.getError().orElseThrow().message();
        Assertions.assertTrue(message.contains("RuntimeException"), "Should mention root cause type: " + message);
        Assertions.assertTrue(message.contains("HADOOP_HOME is unset"), "Should include root cause message: " + message);
    }

    @Test
    void shouldHandleSimpleExceptionWithoutCause() {
        RuntimeException error = new RuntimeException("Something went wrong");

        ChangeMetadata metadata = ChangeMetadata.of(error);

        String message = metadata.getError().orElseThrow().message();
        Assertions.assertEquals("RuntimeException: Something went wrong", message);
    }

}