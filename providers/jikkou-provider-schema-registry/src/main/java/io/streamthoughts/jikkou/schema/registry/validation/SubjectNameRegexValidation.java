/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.validation;

import io.streamthoughts.jikkou.core.annotation.Example;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.NotNull;

@Title("SubjectNameRegexValidation ensures that subject names conform to a specified regular expression.")
@Example(
        title = "Validate that subject names conform to a defined regex.",
        full = true,
        code = {"""
                validations:
                - name: "subjectMustHaveValidName"
                  type: "io.streamthoughts.jikkou.schema.registry.validation.SubjectNameRegexValidation"
                  priority: 100
                  config:
                    subjectNameRegex: "[a-zA-Z0-9\\\\._\\\\-]+"
                """
        }
)

@SupportedResource(type = V1SchemaRegistrySubject.class)
public class SubjectNameRegexValidation implements Validation<V1SchemaRegistrySubject> {

    public static final ConfigProperty<String> VALIDATION_SUBJECT_NAME_REGEX_CONFIG = ConfigProperty
            .ofString("subjectNameRegex");

    private Pattern pattern;

    /**
     * Empty constructor used by {@link Configuration}.
     */
    public SubjectNameRegexValidation() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        final Optional<String> regex = VALIDATION_SUBJECT_NAME_REGEX_CONFIG.getOptional(context.configuration());
        pattern = regex
                .map(pattern -> {
                    if (pattern.isEmpty()) {
                        throw new ConfigException(
                                String.format("The '%s' configuration property is set with an empty regexp",
                                        VALIDATION_SUBJECT_NAME_REGEX_CONFIG.key()
                                )
                        );
                    }
                    return pattern;
                })
                .map(this::compile)
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for %s",
                                VALIDATION_SUBJECT_NAME_REGEX_CONFIG.key(),
                                SubjectNameRegexValidation.class.getSimpleName()
                        )
                ));

    }

    private Pattern compile(final String regex) {
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new ConfigException(
                    String.format("The '%s' configuration property is set with an invalid regexp '%s'",
                            VALIDATION_SUBJECT_NAME_REGEX_CONFIG.key(),
                            regex
                    )
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(final @NotNull V1SchemaRegistrySubject resource) throws ValidationException {
        if (!pattern.matcher(resource.getMetadata().getName()).matches()) {
            String error = String.format(
                    "Name for subject '%s' does not match the configured regex: %s",
                    resource.getMetadata().getName(),
                    pattern
            );
            return ValidationResult.failure(new ValidationError(getName(), resource, error));
        }
        return ValidationResult.success();
    }
}
