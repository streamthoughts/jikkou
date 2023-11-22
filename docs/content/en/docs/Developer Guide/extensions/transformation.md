---
title: "Develop Custom Transformations"
linkTitle: "Develop Custom Transformations"
weight: 3
description: >
  Learn how to develop custom resource transformations.
---

{{% pageinfo color="dark-pink"%}}
This section covers the core classes to develop transformation extensions.
{{% /pageinfo %}}

## Interface

To create a custom `transformation`, you will need to implement the Java
interface: `io.streamthoughts.jikkou.core.transformation.Transformation`.

```java

/**
 * This interface is used to transform or filter resources.
 *
 * @param <T> The resource type supported by the transformation.
 */
public interface Transformation<T extends HasMetadata> extends Interceptor {

    /**
     * Executes the transformation on the specified {@link HasMetadata} object.
     *
     * @param resource  The {@link HasMetadata} to be transformed.
     * @param resources The {@link ResourceListObject} involved in the current operation.
     * @param context   The {@link ReconciliationContext}.
     * @return The list of resources resulting from the transformation.
     */
    @NotNull Optional<T> transform(@NotNull T resource,
                                   @NotNull HasItems resources,
                                   @NotNull ReconciliationContext context);
}
```

## Examples

The transformation class below shows how to filter resource having an annotation `exclude: true`.

```java
import java.util.Optional;

@Named("ExcludeIgnoreResource")
@Title("ExcludeIgnoreResource allows filtering resources whose 'metadata.annotations.ignore' property is equal to 'true'")
@Description("The ExcludeIgnoreResource transformation is used to exclude from the"
        + " reconciliation process any resource whose 'metadata.annotations.ignore'"
        + " property is equal to 'true'. This transformation is automatically enabled."
)
@Enabled
@Priority(HasPriority.HIGHEST_PRECEDENCE)
public final class ExcludeIgnoreResourceTransformation implements Transformation<HasMetadata> {

    /** {@inheritDoc}**/
    @Override
    public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata resource,
                                                    @NotNull HasItems resources,
                                                    @NotNull ReconciliationContext context) {
        return Optional.of(resource)
                .filter(r -> HasMetadata.getMetadataAnnotation(resource, "ignore")
                        .map(NamedValue::getValue)
                        .map(Value::asBoolean)
                        .orElse(false)
                 );
    }
}
```