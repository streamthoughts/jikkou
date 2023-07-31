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
package io.streamthoughts.jikkou.extension.aiven.validation;

import io.streamthoughts.jikkou.api.annotations.AcceptsResource;
import io.streamthoughts.jikkou.api.annotations.ExtensionEnabled;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.validation.ResourceValidation;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

@AcceptsResource(type = V1SchemaRegistryAclEntry.class)
@ExtensionEnabled(value = true)
public class SchemaRegistryAclEntryValidation implements ResourceValidation<V1SchemaRegistryAclEntry> {

    private static final Pattern RESOURCE_PATTERN = Pattern.compile(
            "^(Config|Subject):([A-Za-z|0-9|\\-_*?]*)$"
    );
    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(@NotNull V1SchemaRegistryAclEntry resource) throws ValidationException {
        Matcher matcher = RESOURCE_PATTERN.matcher(resource.getSpec().getResource());
        if (!matcher.matches()) {
            throw new ValidationException(
                    "Invalid input for resource: Config: or Subject:<subject_name> where subject_name must " +
                     "consist of alpha-numeric characters, underscores, dashes, dots and glob characters '*' and '?'",
                    this);
        }
    }
}
