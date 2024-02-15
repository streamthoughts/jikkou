/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest;

import io.micronaut.runtime.Micronaut;

/**
 * Jikkou API Server.
 */
public class JikkouApiServer {

    public static void main(String[] args) {
        Micronaut.build(args)
                .eagerInitSingletons(true)
                .mainClass(JikkouApiServer.class)
                .start();
    }
}
