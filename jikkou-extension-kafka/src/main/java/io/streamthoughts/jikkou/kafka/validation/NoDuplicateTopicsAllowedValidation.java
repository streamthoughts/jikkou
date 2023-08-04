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
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@AcceptsResource(type = V1KafkaTopic.class)
public class NoDuplicateTopicsAllowedValidation implements ResourceValidation<V1KafkaTopic> {

    /** {@inheritDoc} */
    @Override
    public void validate(@NotNull List<V1KafkaTopic> resources) throws ValidationException {
        GenericResourceListObject list = new GenericResourceListObject(resources);
        try {
            list.verifyNoDuplicateMetadataName();
        } catch (DuplicateMetadataNameException e) {
            throw new ValidationException("Duplicate V1KafkaTopic for metadata.name: " + e.duplicates(), this);
        }
    }
}
