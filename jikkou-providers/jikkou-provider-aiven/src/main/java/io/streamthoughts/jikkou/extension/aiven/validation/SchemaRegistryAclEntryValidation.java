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

import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1SchemaRegistryAclEntry.class)
@Enabled
public class SchemaRegistryAclEntryValidation implements Validation<V1SchemaRegistryAclEntry> {

    private static final Pattern RESOURCE_PATTERN = Pattern.compile(
            "^(Config|Subject):([A-Za-z|0-9|\\.\\-_*?]*)$"
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(@NotNull V1SchemaRegistryAclEntry resource) {
        String schemaRegistryResource = resource.getSpec().getResource();
        if (schemaRegistryResource != null) {
            Matcher matcher = RESOURCE_PATTERN.matcher(schemaRegistryResource);
            if (!matcher.matches()) {
                return ValidationResult.failure(new ValidationError(
                        getName(),
                        resource,
                        "Invalid input for resource (" + schemaRegistryResource + "): Config: or " +
                                "Subject:<subject_name> where subject_name must " +
                                "consist of alpha-numeric characters, underscores, dashes, dots and glob characters '*' and '?'"
                ));
            }
        }
        return ValidationResult.success();
    }
}
