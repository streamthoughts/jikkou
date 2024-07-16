/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry;

import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.runtime.JikkouContext;
import org.junit.jupiter.api.BeforeEach;

public class BaseExtensionProviderIT extends AbstractIntegrationTest {

    protected JikkouApi api;

    @BeforeEach
    public void initApi() {
        Configuration configuration = Configuration.of("url", schemaRegistryUrl());

        api = JikkouContext.defaultContext()
            .newApiBuilder()
            .register(new SchemaRegistryExtensionProvider(), configuration)
            .build()
            .enableBuiltInAnnotations(false);
    }
}
