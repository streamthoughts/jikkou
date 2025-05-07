/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.config.Configuration;
import java.net.URI;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.GlueClientBuilder;

/**
 * AWS Client configuration class.
 */
public final class AwsClients {

    private static final Logger LOG = LoggerFactory.getLogger(AwsClients.class);

    static GlueClient newGlueClient(final Configuration configuration) {
        GlueClientBuilder builder = GlueClient.builder()
            .credentialsProvider(newCredentialsProvider(configuration));

        Optional<String> region = AwsExtensionProvider.Config.REGION.getOptional(configuration);
        region.ifPresent(s -> builder.region(Region.of(AwsExtensionProvider.Config.REGION.get(configuration))));

        Optional<String> endpointOverride = AwsExtensionProvider.Config.ENDPOINT_OVERRIDE.getOptional(configuration);
        endpointOverride.ifPresent(s -> builder.endpointOverride(URI.create(s)));

        return builder.build();
    }

    /**
     * Static factory method for constructing a new {@link AwsCredentialsProvider} from the given {@link Configuration}.
     *
     * @param config The configuration.
     * @return a new {@link AwsCredentialsProvider} instance.
     */
    private static AwsCredentialsProvider newCredentialsProvider(final Configuration config) {
        final String accessKeyId = AwsExtensionProvider.Config.ACCESS_KEY_ID.getOptional(config).orElse(null);
        final String secretKey = AwsExtensionProvider.Config.ACCESS_SECRET_KEY.getOptional(config).orElse(null);
        final String sessionToken = AwsExtensionProvider.Config.ACCESS_SESSION_TOKEN.getOptional(config).orElse(null);

        // StaticCredentialsProvider
        if (Strings.isNotBlank(accessKeyId) && Strings.isNotBlank(secretKey)) {
            AwsCredentials credentials;
            if (Strings.isNotBlank(sessionToken)) {
                LOG.info("Creating new credentials provider using the access key id, "
                    + "the secret access key, and the session token that were passed "
                    + "through the AWS provider's configuration");
                credentials = AwsSessionCredentials.create(accessKeyId, secretKey, sessionToken);
            } else {
                LOG.info("Creating new credentials provider using the access key id, and "
                    + "the secret access key that were passed "
                    + "through the AWS provider's configuration");
                credentials = AwsBasicCredentials.create(accessKeyId, secretKey);
            }
            return StaticCredentialsProvider.create(credentials);
        }

        // Otherwise, use DefaultCredentialsProvider
        return DefaultCredentialsProvider.builder().build();
    }
}
