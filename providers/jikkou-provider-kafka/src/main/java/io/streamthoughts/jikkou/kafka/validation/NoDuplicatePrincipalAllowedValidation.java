/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.exceptions.DuplicateMetadataNameException;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Enabled
@SupportedResource(type = V1KafkaPrincipalAuthorization.class)
public class NoDuplicatePrincipalAllowedValidation implements Validation<V1KafkaPrincipalAuthorization> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(@NotNull List<V1KafkaPrincipalAuthorization> resources) throws ValidationException {
        ResourceList<V1KafkaPrincipalAuthorization> list = ResourceList.of(resources);
        try {
            list.verifyNoDuplicateMetadataName();
            return ValidationResult.success();
        } catch (DuplicateMetadataNameException e) {
            return ValidationResult.failure(new ValidationError(
                    getName(),
                    "Duplicate V1KafkaPrincipalAcl for metadata.name: " + e.duplicates())
            );
        }
    }
}
