/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
