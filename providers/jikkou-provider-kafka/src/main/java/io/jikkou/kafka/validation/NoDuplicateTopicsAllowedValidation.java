/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.validation;

import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.Enabled;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.exceptions.DuplicateMetadataNameException;
import io.jikkou.core.exceptions.ValidationException;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.models.Resources;
import io.jikkou.core.validation.Validation;
import io.jikkou.core.validation.ValidationError;
import io.jikkou.core.validation.ValidationResult;
import io.jikkou.kafka.models.V1KafkaTopic;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Title("Validate no duplicate topics")
@Description("Validates that no duplicate Kafka topic names exist in the resource definitions.")
@Enabled
@SupportedResource(type = V1KafkaTopic.class)
public class NoDuplicateTopicsAllowedValidation implements Validation<V1KafkaTopic> {

    /** {@inheritDoc} */
    @Override
    public ValidationResult validate(@NotNull List<V1KafkaTopic> resources) throws ValidationException {
        ResourceList<V1KafkaTopic> list = ResourceList.of(resources);
        try {
            Resources.verifyNoDuplicateMetadataName(list.getItems());
            return ValidationResult.success();
        } catch (DuplicateMetadataNameException e) {
            return ValidationResult.failure(new ValidationError(
                    getName(),
                    "Duplicate V1KafkaTopic for metadata.name: " + e.duplicates())
            );
        }
    }
}
