/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.util.Set;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.Test;

class ProviderProcessorTest {

    private static final String VALID_PROVIDER_TEMPLATE = """
            package com.example;

            import io.jikkou.core.annotation.Provider;
            import io.jikkou.core.extension.ExtensionRegistry;
            import io.jikkou.core.resource.ResourceRegistry;
            import io.jikkou.spi.ExtensionProvider;
            import org.jetbrains.annotations.NotNull;

            @Provider(name = "%s")
            public class %s implements ExtensionProvider {
                public %s() {}
                @Override public void registerExtensions(@NotNull ExtensionRegistry registry) {}
                @Override public void registerResources(@NotNull ResourceRegistry registry) {}
            }
            """;

    @Test
    void shouldReturnProviderAnnotationType() {
        ProviderProcessor processor = new ProviderProcessor();
        Set<String> types = processor.getSupportedAnnotationTypes();
        assertEquals(Set.of("io.jikkou.core.annotation.Provider"), types);
    }

    @Test
    void shouldGenerateServiceFileForValidProvider() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.TestProvider",
                VALID_PROVIDER_TEMPLATE.formatted("test-provider", "TestProvider", "TestProvider"));

        Compilation compilation = javac()
                .withProcessors(new ProviderProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, ProviderProcessor.PROVIDER_RESOURCE_FILE)
                .contentsAsUtf8String()
                .contains("com.example.TestProvider");
    }

    @Test
    void shouldNotIncludeAbstractProviderInServiceFile() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.AbstractProvider",
                """
                package com.example;

                import io.jikkou.core.annotation.Provider;
                import io.jikkou.spi.ExtensionProvider;

                @Provider(name = "abstract-provider")
                public abstract class AbstractProvider implements ExtensionProvider {
                }
                """);

        Compilation compilation = javac()
                .withProcessors(new ProviderProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
        assertServiceFileDoesNotContain(compilation, "com.example.AbstractProvider");
    }

    @Test
    void shouldNotIncludeProviderWithoutNoArgConstructor() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.NoArgProvider",
                """
                package com.example;

                import io.jikkou.core.annotation.Provider;
                import io.jikkou.core.extension.ExtensionRegistry;
                import io.jikkou.core.resource.ResourceRegistry;
                import io.jikkou.spi.ExtensionProvider;
                import org.jetbrains.annotations.NotNull;

                @Provider(name = "no-arg-provider")
                public class NoArgProvider implements ExtensionProvider {

                    public NoArgProvider(String arg) {}

                    @Override public void registerExtensions(@NotNull ExtensionRegistry registry) {}
                    @Override public void registerResources(@NotNull ResourceRegistry registry) {}
                }
                """);

        Compilation compilation = javac()
                .withProcessors(new ProviderProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
        assertServiceFileDoesNotContain(compilation, "com.example.NoArgProvider");
    }

    @Test
    void shouldNotIncludeNonExtensionProviderClass() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.NotAProvider",
                """
                package com.example;

                import io.jikkou.core.annotation.Provider;

                @Provider(name = "not-a-provider")
                public class NotAProvider {
                    public NotAProvider() {}
                }
                """);

        Compilation compilation = javac()
                .withProcessors(new ProviderProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
        assertServiceFileDoesNotContain(compilation, "com.example.NotAProvider");
    }

    private static void assertServiceFileDoesNotContain(Compilation compilation, String className) {
        var serviceFile = compilation.generatedFile(
                StandardLocation.CLASS_OUTPUT,
                ProviderProcessor.PROVIDER_RESOURCE_FILE);
        if (serviceFile.isEmpty()) {
            return; // No file generated = class not included, which is correct
        }
        try {
            String content = serviceFile.get().getCharContent(false).toString();
            assertFalse(content.contains(className),
                    "Class " + className + " should not appear in service file, but found: " + content);
        } catch (java.io.FileNotFoundException e) {
            // File reference exists but content does not — effectively empty, which is correct
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldGenerateServiceFileWithMultipleProviders() {
        JavaFileObject source1 = JavaFileObjects.forSourceString(
                "com.example.ProviderA",
                VALID_PROVIDER_TEMPLATE.formatted("provider-a", "ProviderA", "ProviderA"));

        JavaFileObject source2 = JavaFileObjects.forSourceString(
                "com.example.ProviderB",
                VALID_PROVIDER_TEMPLATE.formatted("provider-b", "ProviderB", "ProviderB"));

        Compilation compilation = javac()
                .withProcessors(new ProviderProcessor())
                .compile(source1, source2);

        assertThat(compilation).succeeded();

        var serviceFile = compilation.generatedFile(
                StandardLocation.CLASS_OUTPUT,
                ProviderProcessor.PROVIDER_RESOURCE_FILE);
        assertTrue(serviceFile.isPresent());

        try {
            String content = serviceFile.get().getCharContent(false).toString();
            assertTrue(content.contains("com.example.ProviderA"));
            assertTrue(content.contains("com.example.ProviderB"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldHandleInnerClassProvider() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.Outer",
                """
                package com.example;

                import io.jikkou.core.annotation.Provider;
                import io.jikkou.core.extension.ExtensionRegistry;
                import io.jikkou.core.resource.ResourceRegistry;
                import io.jikkou.spi.ExtensionProvider;
                import org.jetbrains.annotations.NotNull;

                public class Outer {

                    @Provider(name = "inner-provider")
                    public static class InnerProvider implements ExtensionProvider {
                        public InnerProvider() {}
                        @Override public void registerExtensions(@NotNull ExtensionRegistry registry) {}
                        @Override public void registerResources(@NotNull ResourceRegistry registry) {}
                    }
                }
                """);

        Compilation compilation = javac()
                .withProcessors(new ProviderProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, ProviderProcessor.PROVIDER_RESOURCE_FILE)
                .contentsAsUtf8String()
                .contains("com.example.Outer$InnerProvider");
    }
}
