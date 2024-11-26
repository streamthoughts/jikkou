/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.validation;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.http.client.RestClientException;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.DefaultAsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApiFactory;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityCheck;
import io.streamthoughts.jikkou.schema.registry.api.data.ErrorCode;
import io.streamthoughts.jikkou.schema.registry.api.data.ErrorResponse;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@SupportedResource(type = V1SchemaRegistrySubject.class)
public class SchemaCompatibilityValidation implements Validation<V1SchemaRegistrySubject> {

    private SchemaRegistryClientConfig config;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        this.config = new SchemaRegistryClientConfig(context.appConfiguration());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(@NotNull V1SchemaRegistrySubject resource) throws ValidationException {
        V1SchemaRegistrySubjectSpec spec = resource.getSpec();
        if (spec == null) return ValidationResult.success();
        return validate(resource, new DefaultAsyncSchemaRegistryApi(SchemaRegistryApiFactory.create(config)), this);
    }

    public static ValidationResult validate(@NotNull V1SchemaRegistrySubject resource,
                                            @NotNull AsyncSchemaRegistryApi api,
                                            @NotNull Validation<?> validation) throws ValidationException {

        String subjectName = resource.getMetadata().getName();
        V1SchemaRegistrySubjectSpec spec = resource.getSpec();

        SubjectSchemaRegistration registration = new SubjectSchemaRegistration(
                spec.getSchema().value(),
                spec.getSchemaType(),
                spec.getReferences()
        );
        try {
            Mono<CompatibilityCheck> future = api.testCompatibilityLatest(subjectName, true, registration);
            CompatibilityCheck check = future.block();
            if (!check.isCompatible()) {
                return ValidationResult.failure(new ValidationError(
                        validation.getName(),
                        resource,
                        String.format(
                                "Schema for subject '%s' is not compatible with latest version: %s",
                                subjectName,
                                check.messages()
                        )
                ));
            }
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof RestClientException clientException) {
                ErrorResponse response = clientException.getResponseEntity(ErrorResponse.class);
                List<Integer> shippableErrors = List.of(ErrorCode.SUBJECT_NOT_FOUND, ErrorCode.VERSION_NOT_FOUND);
                if (!shippableErrors.contains(response.errorCode())) {
                    fail(response.message());
                }
            }
        } finally {
            api.close();
        }

        return ValidationResult.success();
    }

    private static void fail(String error) {
        throw new JikkouRuntimeException("Failed to test schema compatibility: " + error);
    }
}
