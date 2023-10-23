/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.extension.aiven.validation;

import io.streamthoughts.jikkou.core.annotation.AcceptsResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.resource.validation.ResourceValidation;
import io.streamthoughts.jikkou.core.resource.validation.ValidationResult;
import io.streamthoughts.jikkou.extension.aiven.AivenResourceProvider;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.AivenAsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import io.streamthoughts.jikkou.schema.registry.validation.SchemaCompatibilityValidation;
import org.jetbrains.annotations.NotNull;

@AcceptsResource(
        apiVersion = AivenResourceProvider.SCHEMA_REGISTRY_API_VERSION,
        kind = AivenResourceProvider.SCHEMA_REGISTRY_KIND
)
public class AivenSchemaCompatibilityValidation implements ResourceValidation<V1SchemaRegistrySubject> {

    private AivenApiClientConfig config;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        this.config = new AivenApiClientConfig(config);
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
