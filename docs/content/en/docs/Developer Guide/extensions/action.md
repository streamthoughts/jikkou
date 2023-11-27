---
title: "Develop Custom Action"
linkTitle: "Develop Custom Actions"
weight: 3
description: >
  Learn how to develop custom actions.
---

{{% pageinfo color="dark-pink"%}}
This section covers the core classes to develop action extensions.
{{% /pageinfo %}}

## Interface

To create a custom `action`, you will need to implement the Java
interface: `io.streamthoughts.jikkou.core.action.Action`.

```java
/**
 * Interface for executing a one-shot action on a specific type of resources.
 *
 * @param <T> The type of the resource.
 */
@Category(ExtensionCategory.ACTION)
public interface Action<T extends HasMetadata> extends HasMetadataAcceptable, Extension {

    /**
     * Executes the action.
     *
     * @param configuration The configuration
     * @return The ExecutionResultSet
     */
    @NotNull ExecutionResultSet<T> execute(@NotNull Configuration configuration);
}
```

## Examples

The `Action` class below shows how to implement a custom action accepting options`.

```java
@Named(EchoAction.NAME)
@Title("Print the input.")
@Description("The EchoAction allows printing the text provided in input.")
@ExtensionSpec(
        options = {
                @ExtensionOptionSpec(
                        name = INPUT_CONFIG_NAME,
                        description = "The input text to print.",
                        type = String.class,
                        required = true
                )
        }
)
public final class EchoAction extends ContextualExtension implements Action<HasMetadata> {
    public static final String NAME = "EchoAction";
    public static final String INPUT_CONFIG_NAME = "input";
    @Override
    public @NotNull ExecutionResultSet<HasMetadata> execute(@NotNull Configuration configuration) {

        String input = extensionContext().<String>configProperty(INPUT_CONFIG_NAME).get(configuration);

        return ExecutionResultSet
                .newBuilder()
                .result(ExecutionResult
                        .newBuilder()
                        .status(ExecutionStatus.SUCCEEDED)
                        .data(new EchoOut(input))
                        .build())
                .build();
    }

    @Kind("EchoOutput")
    @ApiVersion("core.jikkou.io/v1")
    @Reflectable
    record EchoOut(@JsonProperty("out") String out) implements HasMetadata {

        @Override
        public ObjectMeta getMetadata() {
            return new ObjectMeta();
        }

        @Override
        public HasMetadata withMetadata(ObjectMeta objectMeta) {
            throw new UnsupportedOperationException();
        }
    }
}
```