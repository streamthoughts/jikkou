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
package io.streamthoughts.jikkou.schema.registry.validation;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.schema.registry.avro.AvroSchema;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.jetbrains.annotations.NotNull;

@SupportedResource(kind = "SchemaRegistrySubject")
public class AvroSchemaValidation implements Validation<V1SchemaRegistrySubject> {

    public static final ConfigProperty<Boolean> RECORD_FIELD_MUST_HAVE_DOC = ConfigProperty
            .ofBoolean("fieldsMustHaveDoc")
            .orElse(false);

    public static final ConfigProperty<Boolean> RECORD_FIELDS_MUST_BE_NULLABLE = ConfigProperty
            .ofBoolean("fieldsMustBeNullable")
            .orElse(false);

    public static final ConfigProperty<Boolean> RECORD_FIELDS_MUST_BE_OPTIONAL = ConfigProperty
            .ofBoolean("fieldsMustBeOptional")
            .orElse(false);

    private boolean recordFieldsMustHaveDoc;
    private boolean recordFieldsMustBeNullable;
    private boolean recordFieldsMustBeOptional;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        recordFieldsMustHaveDoc = RECORD_FIELD_MUST_HAVE_DOC.getOptional(config)
                .orElseThrow(getConfigExceptionSupplier(RECORD_FIELD_MUST_HAVE_DOC));

        recordFieldsMustBeNullable = RECORD_FIELDS_MUST_BE_NULLABLE.getOptional(config)
                .orElseThrow(getConfigExceptionSupplier(RECORD_FIELDS_MUST_BE_NULLABLE));

        recordFieldsMustBeOptional = RECORD_FIELDS_MUST_BE_OPTIONAL.getOptional(config)
                .orElseThrow(getConfigExceptionSupplier(RECORD_FIELDS_MUST_BE_OPTIONAL));
    }

    @NotNull
    private static Supplier<ConfigException> getConfigExceptionSupplier(ConfigProperty<?> property) {
        return () -> new ConfigException(
                String.format("The '%s' configuration property is required for %s",
                        property.key(),
                        AvroSchemaValidation.class.getSimpleName()
                )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(@NotNull V1SchemaRegistrySubject resource) throws ValidationException {
        V1SchemaRegistrySubjectSpec spec
                = resource.getSpec();
        if (isAvroSchema(resource) && hasNotReferences(resource)) {
            try {
                AvroSchema avroSchema = new AvroSchema(spec.getSchema().value());

                List<ValidationError> errors = new ArrayList<>();
                if (recordFieldsMustHaveDoc) {
                    errors.addAll(validateDocFields(resource, avroSchema.schema()));
                }

                if (recordFieldsMustBeNullable) {
                    errors.addAll(validateFieldsAreNullable(resource, avroSchema.schema()));
                }

                if (recordFieldsMustBeOptional) {
                    errors.addAll(validateFieldsAreOptional(resource, avroSchema.schema()));
                }

                if (!errors.isEmpty()) {
                    return new ValidationResult(errors);
                }


            } catch (AvroRuntimeException e) {
                String error = String.format(
                        "Failed to parse subject schema '%s'. Cause: %s",
                        resource.getMetadata().getName(),
                        e.getLocalizedMessage()
                );
                return ValidationResult.failure(new ValidationError(getName(), resource, error));
            }
        }
        return ValidationResult.success();
    }

    private List<ValidationError> validateDocFields(V1SchemaRegistrySubject subject, Schema schema) {
        return validateDocFields(subject, schema, "");
    }

    private List<ValidationError> validateDocFields(V1SchemaRegistrySubject subject,
                                                    Schema schema,
                                                    String parentPath) {
        List<ValidationError> errors = new ArrayList<>();
        if (schema.getType() == Schema.Type.RECORD) {
            if (Strings.isBlank(schema.getDoc())) {
                errors.add(new ValidationError(
                        getName(),
                        subject,
                        String.format(
                                "Invalid subject schema '%s'. Missing 'doc' field for record: '%s'",
                                subject.getMetadata().getName(),
                                schema.getFullName()
                        )
                ));
            }
            for (Schema.Field field : schema.getFields()) {
                String fieldPath = parentPath.isEmpty() ? field.name() : parentPath + "." + field.name();
                if (Strings.isBlank(field.doc())) {
                    errors.add(new ValidationError(
                            getName(),
                            subject,
                            String.format(
                                    "Invalid subject schema '%s'. Missing 'doc' field for field: '%s'",
                                    subject.getMetadata().getName(),
                                    fieldPath
                            ))
                    );
                }
                errors.addAll(validateDocFields(subject, field.schema(), fieldPath));
            }
        } else if (schema.getType() == Schema.Type.UNION) {
            for (Schema unionSchema : schema.getTypes()) {
                errors.addAll(validateDocFields(subject, unionSchema, parentPath));
            }
        }
        return errors;
    }

    private List<ValidationError> validateFieldsAreNullable(V1SchemaRegistrySubject subject,
                                                            Schema schema) {
        return validateFieldsAreNullable(subject, schema, "");
    }

    private List<ValidationError> validateFieldsAreNullable(V1SchemaRegistrySubject subject,
                                                            Schema schema,
                                                            String parentPath) {
        List<ValidationError> errors = new ArrayList<>();
        if (schema.getType() == Schema.Type.RECORD) {
            for (Schema.Field field : schema.getFields()) {
                String fieldPath = parentPath.isEmpty() ? field.name() : parentPath + "." + field.name();
                if (!isFieldNullable(field.schema())) {
                    errors.add(new ValidationError(
                            getName(),
                            subject,
                            String.format(
                                    "Invalid subject schema '%s'. Non-nullable field found: %s",
                                    subject.getMetadata().getName(),
                                    fieldPath
                            ))
                    );
                }
                errors.addAll(validateFieldsAreNullable(subject, field.schema(), fieldPath));
            }
        } else if (schema.getType() == Schema.Type.UNION) {
            for (Schema unionSchema : schema.getTypes()) {
                errors.addAll(validateFieldsAreNullable(subject, unionSchema, parentPath));
            }
        }
        return errors;
    }

    private List<ValidationError> validateFieldsAreOptional(V1SchemaRegistrySubject subject,
                                                            Schema schema) {
        return validateFieldsAreOptional(subject, schema, "");
    }

    private List<ValidationError> validateFieldsAreOptional(V1SchemaRegistrySubject subject,
                                                            Schema schema,
                                                            String parentPath) {
        List<ValidationError> errors = new ArrayList<>();
        if (schema.getType() == Schema.Type.RECORD) {
            for (Schema.Field field : schema.getFields()) {
                String fieldPath = parentPath.isEmpty() ? field.name() : parentPath + "." + field.name();
                if (!field.hasDefaultValue()) {
                    errors.add(new ValidationError(
                            getName(),
                            subject,
                            String.format(
                                    "Invalid subject schema '%s'. Missing default value for field: %s",
                                    subject.getMetadata().getName(),
                                    fieldPath
                            ))
                    );
                }
                errors.addAll(validateFieldsAreOptional(subject, field.schema(), fieldPath));
            }
        } else if (schema.getType() == Schema.Type.UNION) {
            for (Schema unionSchema : schema.getTypes()) {
                errors.addAll(validateFieldsAreOptional(subject, unionSchema, parentPath));
            }
        }
        return errors;
    }


    private boolean isFieldNullable(Schema fieldSchema) {
        return fieldSchema.getType() == Schema.Type.UNION && fieldSchema.getTypes()
                .stream()
                .anyMatch(schema -> schema.getType() == Schema.Type.NULL);
    }

    private static boolean hasNotReferences(V1SchemaRegistrySubject resources) {
        return resources.getSpec().getReferences() == null || resources.getSpec().getReferences().isEmpty();
    }

    private static boolean isAvroSchema(V1SchemaRegistrySubject resource) {
        return resource.getSpec().getSchemaType() == SchemaType.AVRO;
    }
}
