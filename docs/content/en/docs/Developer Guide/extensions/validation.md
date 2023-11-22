---
title: "Develop Custom Validations"
linkTitle: "Develop Custom Validations"
weight: 2
description: >
  Learn how to develop custom resource validations.
---

{{% pageinfo color="dark-pink"%}}
This section covers the core classes to develop validation extensions.
{{% /pageinfo %}}

## Interface

To create a custom `validation`, you will need to implement the Java
interface: `io.streamthoughts.jikkou.core.validation.Validation`.

This interface defines two methods, with a default implementation for each, to give you the option of validating either
all resources accepted by validation at once, or each resource one by one.

```java
public interface Validation<T extends HasMetadata> extends Interceptor {

    /**
     * Validates the specified resource list.
     *
     * @param resources              The list of resources to be validated.
     * @return The ValidationResult.
     */
    default ValidationResult validate(@NotNull final List<T> resources) {
        // code omitted for clarity
    }

    /**
     * Validates the specified resource.
     *
     * @param resource               The resource to be validated.
     * @return The ValidationResult.
     */
    default ValidationResult validate(@NotNull final T resource) {
        // code omitted for clarity
    }
}
```

## Examples

The validation class below shows how to validate that any resource has a specific non-empty label.

```java

@Title("HasNonEmptyLabelValidation allows validating that resources have a non empty label.")
@Description("This validation can be used to ensure that all resources are associated to a specific label. The labe key is passed through the configuration of the extension.")
@Example(
        title = "Validate that resources have a non-empty label with key 'owner'.",
        full = true,
        code = {"""
                validations:
                - name: "resourceMustHaveNonEmptyLabelOwner"
                  type: "com.example.jikkou.validation.HasNonEmptyLabelValidation"
                  priority: 100
                  config:
                    key: owner
                """
        }
)
@SupportedResources(value = {}) // an empty list implies that the extension supports any resource-type
public final class HasNonEmptyLabelValidation implements Validation {

    // The required config property.
    static final ConfigProperty<String> LABEL_KEY_CONFIG = ConfigProperty.ofString("key");

    private String key;

    /**
     * Empty constructor - required.
     */
    public HasNonEmptyLabelValidation() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull final Configuration config) {
        // Get the key from the configuration.
        this.key = LABEL_KEY_CONFIG
                .getOptional(config)
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for %s",
                                LABEL_KEY_CONFIG.key(),
                                TopicNamePrefixValidation.class.getSimpleName()
                        )
                ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(final @NotNull HasMetadata resource) {
        Optional<String> label = resource.getMetadata()
                .findLabelByKey(this.key)
                .map(NamedValue::getValue)
                .map(Value::asString)
                .filter(String::isEmpty);
        // Failure
        if (label.isEmpty()) {
            String error = String.format(
                    "Resource for name '%s' have no defined or empty label for key: '%s'",
                    resource.getMetadata().getName(),
                    this.key
            );
            return ValidationResult.failure(new ValidationError(getName(), resource, error));
        }
        // Success
        return ValidationResult.success();
    }
}
```