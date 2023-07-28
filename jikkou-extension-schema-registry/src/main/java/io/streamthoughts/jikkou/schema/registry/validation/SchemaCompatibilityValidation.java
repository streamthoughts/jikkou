/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.schema.registry.validation;

import io.streamthoughts.jikkou.api.annotations.AcceptsResource;
import io.streamthoughts.jikkou.api.annotations.ExtensionEnabled;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.validation.ResourceValidation;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApiFactory;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.streamthoughts.jikkou.schema.registry.api.restclient.RestClientException;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import org.jetbrains.annotations.NotNull;

@ExtensionEnabled(value = false)
@AcceptsResource(type = V1SchemaRegistrySubject.class)
public class SchemaCompatibilityValidation implements ResourceValidation<V1SchemaRegistrySubject> {

    public static final int LATEST_VERSION = -1;

    private SchemaRegistryClientConfig config;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        this.config = new SchemaRegistryClientConfig(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(@NotNull V1SchemaRegistrySubject resource) throws ValidationException {
        String subjectName = resource.getMetadata().getName();
        V1SchemaRegistrySubjectSpec spec = resource.getSpec();
        if (spec == null) return;

        SubjectSchemaRegistration registration = new SubjectSchemaRegistration(
                spec.getSchema().value(),
                spec.getSchemaType(),
                spec.getReferences()
        );
        SchemaRegistryApi api = SchemaRegistryApiFactory.create(config);
        try {
            var check = api.testCompatibility(subjectName, LATEST_VERSION, true, registration);
            if (!check.isCompatible()) {
                throw new ValidationException(String.format(
                        "Schema for subject '%s' is not compatible with latest version: %s",
                        subjectName,
                        check.getMessages()
                )
                        , this);
            }
        } catch (RestClientException e) {
            throw new JikkouRuntimeException("Failed to test schema compatibility: " + e.getLocalizedMessage());
        } finally {
            api.close();
        }
    }

    private static void rethrowException(Exception e) {

    }
}
