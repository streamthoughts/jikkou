/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws;

import io.streamthoughts.jikkou.core.config.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

class AwsExtensionProviderTest {

    @Test
    void shouldCreateGlueClient() {
        Configuration configuration = Configuration.of(
            AwsExtensionProvider.Config.REGION.key(), Region.EU_CENTRAL_1.toString(),
            AwsExtensionProvider.Config.ACCESS_KEY_ID.key(), "dummy_access_key",
            AwsExtensionProvider.Config.ACCESS_SECRET_KEY.key(), "dummy_secret_key"
        );

        AwsExtensionProvider provider = new AwsExtensionProvider();
        provider.configure(configuration);

        Assertions.assertNotNull(provider.newGlueClient());
    }
}