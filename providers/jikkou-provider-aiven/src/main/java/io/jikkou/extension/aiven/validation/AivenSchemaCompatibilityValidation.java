/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.aiven.validation;

import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.exceptions.ValidationException;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.validation.Validation;
import io.jikkou.core.validation.ValidationResult;
import io.jikkou.extension.aiven.AivenExtensionProvider;
import io.jikkou.extension.aiven.ApiVersions;
import io.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.jikkou.extension.aiven.api.AivenAsyncSchemaRegistryApi;
import io.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import io.jikkou.schema.registry.validation.SchemaCompatibilityValidation;
import org.jetbrains.annotations.NotNull;

@Title("Validate Aiven schema compatibility")
@Description("Validates schema compatibility settings for Aiven Schema Registry subject resources.")
@SupportedResource(
    apiVersion = ApiVersions.KAFKA_AIVEN_V1BETA1,
    kind = ApiVersions.SCHEMA_REGISTRY_KIND
)
public class AivenSchemaCompatibilityValidation implements Validation<V1SchemaRegistrySubject> {

    private AivenApiClientConfig apiClientConfig;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        this.apiClientConfig = context.<AivenExtensionProvider>provider().apiClientConfig();
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
                new AivenAsyncSchemaRegistryApi(AivenApiClientFactory.create(apiClientConfig)),
                this
        );
    }
}
