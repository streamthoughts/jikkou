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

import io.streamthoughts.jikkou.annotation.AcceptsResource;
import io.streamthoughts.jikkou.annotation.ExtensionEnabled;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.validation.ResourceValidation;
import io.streamthoughts.jikkou.common.utils.Strings;
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

@AcceptsResource(type = V1SchemaRegistrySubject.class)
@ExtensionEnabled(value = false)
public class AvroSchemaValidation implements ResourceValidation<V1SchemaRegistrySubject> {

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
    public void validate(@NotNull V1SchemaRegistrySubject resource) throws ValidationException {
        V1SchemaRegistrySubjectSpec spec
                = resource.getSpec();
        if (isAvroSchema(resource) && hasNotReferences(resource)) {
            try {
                AvroSchema avroSchema = new AvroSchema(spec.getSchema().value());

                List<ValidationException> exceptions = new ArrayList<>();
                if (recordFieldsMustHaveDoc) {
                    exceptions.addAll(validateDocFields(resource, avroSchema.schema()));
                }

                if (recordFieldsMustBeNullable) {
                    exceptions.addAll(validateFieldsAreNullable(resource, avroSchema.schema()));
                }

                if (recordFieldsMustBeOptional) {
                    exceptions.addAll(validateFieldsAreOptional(resource, avroSchema.schema()));
                }

                if (!exceptions.isEmpty()) {
                    throw new ValidationException(exceptions);
                }

            } catch (AvroRuntimeException e) {
                throw new ValidationException(
                        String.format(
                                "Failed to parse subject schema '%s'. Cause: %s",
                                resource.getMetadata().getName(),
                                e.getLocalizedMessage()
                        ),
                        this
                );
            }
        }
    }

    private List<ValidationException> validateDocFields(V1SchemaRegistrySubject subject, Schema schema) {
        return validateDocFields(subject, schema, "");
    }

    private List<ValidationException> validateDocFields(V1SchemaRegistrySubject subject,
                                                        Schema schema,
                                                        String parentPath) {
        List<ValidationException> errors = new ArrayList<>();
        if (schema.getType() == Schema.Type.RECORD) {
            if (Strings.isBlank(schema.getDoc())) {
                errors.add(new ValidationException(
                        String.format(
                                "Invalid subject schema '%s'. Missing 'doc' field for record: '%s'",
                                subject.getMetadata().getName(),
                                schema.getFullName()
                        ),
                        this
                ));
            }
            for (Schema.Field field : schema.getFields()) {
                String fieldPath = parentPath.isEmpty() ? field.name() : parentPath + "." + field.name();
                if (Strings.isBlank(field.doc())) {
                    errors.add(new ValidationException(
                            String.format(
                                    "Invalid subject schema '%s'. Missing 'doc' field for field: '%s'",
                                    subject.getMetadata().getName(),
                                    fieldPath
                            ),
                            this)
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

    private List<ValidationException> validateFieldsAreNullable(V1SchemaRegistrySubject subject,
                                                                Schema schema) {
        return validateFieldsAreNullable(subject, schema, "");
    }

    private List<ValidationException> validateFieldsAreNullable(V1SchemaRegistrySubject subject,
                                                                Schema schema,
                                                                String parentPath) {
        List<ValidationException> errors = new ArrayList<>();
        if (schema.getType() == Schema.Type.RECORD) {
            for (Schema.Field field : schema.getFields()) {
                String fieldPath = parentPath.isEmpty() ? field.name() : parentPath + "." + field.name();
                if (!isFieldNullable(field.schema())) {
                    errors.add(new ValidationException(
                            String.format(
                                    "Invalid subject schema '%s'. Non-nullable field found: %s",
                                    subject.getMetadata().getName(),
                                    fieldPath
                            ), this)
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

    private List<ValidationException> validateFieldsAreOptional(V1SchemaRegistrySubject subject,
                                                                Schema schema) {
        return validateFieldsAreOptional(subject, schema, "");
    }

    private List<ValidationException> validateFieldsAreOptional(V1SchemaRegistrySubject subject,
                                                                Schema schema,
                                                                String parentPath) {
        List<ValidationException> errors = new ArrayList<>();
        if (schema.getType() == Schema.Type.RECORD) {
            for (Schema.Field field : schema.getFields()) {
                String fieldPath = parentPath.isEmpty() ? field.name() : parentPath + "." + field.name();
                if (!field.hasDefaultValue()) {
                    errors.add(new ValidationException(
                            String.format(
                                    "Invalid subject schema '%s'. Missing default value for field: %s",
                                    subject.getMetadata().getName(),
                                    fieldPath
                            ),
                            this)
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
