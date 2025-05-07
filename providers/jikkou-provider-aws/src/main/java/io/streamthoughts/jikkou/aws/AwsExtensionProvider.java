/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws;

import io.streamthoughts.jikkou.aws.models.AwsGlueSchema;
import io.streamthoughts.jikkou.aws.reconciler.AwsGlueSchemaCollector;
import io.streamthoughts.jikkou.aws.reconciler.AwsGlueSchemaController;
import io.streamthoughts.jikkou.core.annotation.Provider;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.spi.BaseExtensionProvider;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.glue.GlueClient;

/**
 * Extension provider for AWS.
 */
@Provider(
    name = "aws",
    description = "Extension provider for AWS",
    tags = {"AWS"}
)
public class AwsExtensionProvider extends BaseExtensionProvider {

    public interface Config {
        ConfigProperty<String> REGION = ConfigProperty
            .ofString("aws.client.region")
            .description("The AWS S3 Region, e.g. us-east-1");

        ConfigProperty<String> ACCESS_KEY_ID = ConfigProperty
            .ofString("aws.client.accessKeyId")
            .description("The AWS Access Key ID.")
            .required(false);

        ConfigProperty<String> ACCESS_SECRET_KEY = ConfigProperty
            .ofString("aws.client.secretAccessKey")
            .description("The AWS Secret Access Key.")
            .required(false);

        ConfigProperty<String> ACCESS_SESSION_TOKEN = ConfigProperty
            .ofString("aws.client.sessionToken")
            .description("The AWS session token.")
            .required(false);

        ConfigProperty<String> ENDPOINT_OVERRIDE = ConfigProperty
            .ofString("aws.client.endpointOverride")
            .description("The endpoint with which the SDK should communicate allowing you to use a different S3 compatible service")
            .required(false);

        ConfigProperty<List<String>> GLUE_REGISTRIES = ConfigProperty
            .ofList("aws.glue.registryNames")
            .description("The name of the registries. Used only for lookup.")
            .defaultValue(List.of());
    }
    /** {@inheritDoc} **/
    @Override
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        super.configure(configuration);
    }

    public GlueClient newGlueClient() {
        return AwsClients.newGlueClient(configuration);
    }

    /** {@inheritDoc} **/
    @Override
    public void registerExtensions(@NotNull ExtensionRegistry registry) {
        registry.register(AwsGlueSchemaCollector.class);
        registry.register(AwsGlueSchemaController.class);
    }


    /** {@inheritDoc} **/
    @Override
    public void registerResources(@NotNull ResourceRegistry registry) {
        registry.register(AwsGlueSchema.class);
        registry.register(ResourceChange.class, ResourceChange.getResourceTypeOf(AwsGlueSchema.class));
    }
}
