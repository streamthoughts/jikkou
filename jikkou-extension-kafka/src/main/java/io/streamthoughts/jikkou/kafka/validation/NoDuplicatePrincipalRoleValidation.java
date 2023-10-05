/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.annotation.AcceptsResource;
import io.streamthoughts.jikkou.api.error.DuplicateMetadataNameException;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.api.validation.ResourceValidation;
import io.streamthoughts.jikkou.api.validation.ValidationError;
import io.streamthoughts.jikkou.api.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalRole;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@AcceptsResource(type = V1KafkaPrincipalRole.class)
public class NoDuplicatePrincipalRoleValidation implements ResourceValidation<V1KafkaPrincipalRole> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(@NotNull List<V1KafkaPrincipalRole> resources) throws ValidationException {
        GenericResourceListObject<V1KafkaPrincipalRole> list = new GenericResourceListObject<>(resources);
        try {
            list.verifyNoDuplicateMetadataName();
            return ValidationResult.success();
        } catch (DuplicateMetadataNameException e) {
            return ValidationResult.failure(new ValidationError(
                    getName(),
                    "Duplicate V1KafkaPrincipalRole for metadata.name: " + e.duplicates())
            );
        }
    }
}
