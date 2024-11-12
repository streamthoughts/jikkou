/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven;

import io.streamthoughts.jikkou.core.CoreExtensionProvider;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.runtime.JikkouContext;
import org.junit.jupiter.api.BeforeEach;

public class BaseExtensionProviderIT extends AbstractAivenIntegrationTest {

    protected JikkouApi api;

    @BeforeEach
    public void initApi() {
        AivenExtensionProvider provider = new AivenExtensionProvider();

        Configuration configuration = new Configuration
            .Builder()
            .with(AivenExtensionProvider.Config.API_URL.key(), SERVER.url("/"))
            .with(AivenExtensionProvider.Config.SERVICE.key(), "service")
            .with(AivenExtensionProvider.Config.TOKEN_AUTH.key(), "token")
            .with(AivenExtensionProvider.Config.DEBUG_LOGGING_ENABLED.key(), true)
            .with(AivenExtensionProvider.Config.PROJECT.key(), "project")
            .build();

        api = JikkouContext.defaultContext()
            .newApiBuilder()
            .register(new CoreExtensionProvider())
            .register(provider, configuration)
            .build()
            .enableBuiltInAnnotations(false);
    }
}
