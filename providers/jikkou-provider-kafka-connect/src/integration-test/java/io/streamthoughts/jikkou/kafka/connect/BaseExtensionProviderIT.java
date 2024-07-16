/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect;

import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import io.streamthoughts.jikkou.runtime.JikkouContext;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;

public class BaseExtensionProviderIT extends AbstractKafkaConnectorIT {

    protected JikkouApi api;

    @BeforeEach
    public void initApi() {
        Configuration configuration = Configuration.from(Map.of(
            "clusters", List.of(Map.of(
                KafkaConnectClientConfig.KAFKA_CONNECT_NAME.key(), KAFKA_CONNECTOR_NAME,
                KafkaConnectClientConfig.KAFKA_CONNECT_URL.key(), getConnectUrl(),
                KafkaConnectClientConfig.KAFKA_CONNECT_DEBUG_LOGGING_ENABLED.key(), true
            ))));

        api = JikkouContext.defaultContext()
            .newApiBuilder()
            .register(new KafkaConnectExtensionProvider(), configuration)
            .build()
            .enableBuiltInAnnotations(false);
    }
}