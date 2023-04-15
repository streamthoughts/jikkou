/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.validations;

import io.streamthoughts.jikkou.api.annotations.SupportedResource;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.validation.ResourceValidation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaSpec;
import org.jetbrains.annotations.NotNull;

/**
 * Validation for {@link V1KafkaClientQuota}.
 */
@SupportedResource(type = V1KafkaClientQuota.class)
public class ClientQuotaValidation implements ResourceValidation<V1KafkaClientQuota> {

    /** {@inheritDoc} */
    @Override
    public void validate(@NotNull V1KafkaClientQuota resource) throws ValidationException {
        V1KafkaClientQuotaSpec spec = resource.getSpec();

        if (spec == null) {
            throw new ValidationException("spec is missing", this);
        }
        if (spec.getType() == null) {
            throw new ValidationException("spec.type is missing", this);
        }

        try {
            spec.getType().validate(spec.getEntity());
        } catch (Exception e) {
            throw new ValidationException(e.getMessage(), this);
        }
    }

}
