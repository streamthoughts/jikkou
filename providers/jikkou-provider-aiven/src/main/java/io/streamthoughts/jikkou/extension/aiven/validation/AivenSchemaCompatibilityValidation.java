/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.validation;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.extension.aiven.ApiVersions;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.AivenAsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import io.streamthoughts.jikkou.schema.registry.validation.SchemaCompatibilityValidation;
import org.jetbrains.annotations.NotNull;

@SupportedResource(
        apiVersion = ApiVersions.KAFKA_REGISTRY_API_VERSION,
        kind = ApiVersions.SCHEMA_REGISTRY_KIND
)
public class AivenSchemaCompatibilityValidation implements Validation<V1SchemaRegistrySubject> {

    private AivenApiClientConfig config;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        this.config = new AivenApiClientConfig(context.appConfiguration());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(@NotNull V1SchemaRegistrySubject resource) throws ValidationException {
        V1SchemaRegistrySubjectSpec spec = resource.getSpec();
        if (spec == null) return ValidationResult.success();

        return SchemaCompatibilityValidation.validate(
                resource,
                new AivenAsyncSchemaRegistryApi(AivenApiClientFactory.create(config)),
                this
        );
    }
}
