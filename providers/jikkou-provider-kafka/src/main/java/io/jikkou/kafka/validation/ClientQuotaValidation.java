/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.validation;

import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.exceptions.ValidationException;
import io.jikkou.core.validation.Validation;
import io.jikkou.core.validation.ValidationError;
import io.jikkou.core.validation.ValidationResult;
import io.jikkou.kafka.models.V1KafkaClientQuota;
import io.jikkou.kafka.models.V1KafkaClientQuotaSpec;
import org.jetbrains.annotations.NotNull;

/**
 * Validation for {@link V1KafkaClientQuota}.
 */
@Title("Validate Kafka client quotas")
@Description("Validates Kafka client quota resources to ensure they are well-formed.")
@SupportedResource(type = V1KafkaClientQuota.class)
public class ClientQuotaValidation implements Validation<V1KafkaClientQuota> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(@NotNull V1KafkaClientQuota resource) throws ValidationException {
        V1KafkaClientQuotaSpec spec = resource.getSpec();

        if (spec == null) {
            return ValidationResult.failure(
                    new ValidationError(getName(), resource, "spec is missing"));
        }
        if (spec.getType() == null) {
            return ValidationResult.failure(
                    new ValidationError(getName(), resource, "spec.type is missing"));
        }

        try {
            spec.getType().validate(spec.getEntity());
        } catch (Exception e) {
            return ValidationResult.failure(new ValidationError(getName(), resource, e.getMessage()));
        }

        return ValidationResult.success();
    }

}
