/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EitherTest {

    @Test
    void shouldCreateEitherLeft() {
        Either<String, Object> value = Either.left("value");
        Assertions.assertTrue(value.isLeft());
    }

    @Test
    void shouldCreateEitherRight() {
        Either<String, Object> value = Either.right("value");
        Assertions.assertTrue(value.isRight());
    }
}