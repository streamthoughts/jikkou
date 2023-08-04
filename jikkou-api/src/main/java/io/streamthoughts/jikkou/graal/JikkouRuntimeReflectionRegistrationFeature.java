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
package io.streamthoughts.jikkou.graal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.streamthoughts.jikkou.annotation.Reflectable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

/**
 * Registers any fields, methods, constructors and types
 * annotated with {@link Reflectable} for runtime reflective access.
 */
public class JikkouRuntimeReflectionRegistrationFeature implements Feature {

    public static final String JIKKOU_BASE_PACKAGE = "io.streamthoughts.jikkou";

    private final Reflections reflections;

    public JikkouRuntimeReflectionRegistrationFeature() {
        reflections = new Reflections(
                JIKKOU_BASE_PACKAGE,
                Scanners.SubTypes,
                Scanners.MethodsAnnotated,
                Scanners.TypesAnnotated,
                Scanners.ConstructorsAnnotated
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void beforeAnalysis(Feature.BeforeAnalysisAccess access) {
        try {
            // Lookup for reflective access on Types.
            getAllTypesAnnotatedWithReflectable()
                    .forEach(JikkouRuntimeReflectionRegistrationFeature::registerReflectiveAccess);

            // Lookup for reflective access on Methods.
            reflections.getMethodsAnnotatedWith(Reflectable.class)
                    .forEach(JikkouRuntimeReflectionRegistrationFeature::registerReflectiveAccess);

            // Lookup for reflective access on Field.
            reflections.getFieldsAnnotatedWith(Reflectable.class)
                    .forEach(JikkouRuntimeReflectionRegistrationFeature::registerReflectiveAccess);

            // Lookup for reflective access on Field.
            reflections.getConstructorsAnnotatedWith(Reflectable.class)
                    .forEach(JikkouRuntimeReflectionRegistrationFeature::registerReflectiveAccess);

            registerJacksonDeserializer();
            registerJacksonSerializer();
            registerReflectiveAccess(LinkedHashSet.class);

        } catch (Exception e) {
            throw new RuntimeException("native-image build-time configuration failed", e);
        }
    }

    private void registerJacksonDeserializer() {
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(JsonDeserialize.class);
        for (Class<?> type : annotated) {
            JsonDeserialize annotation = type.getAnnotation(JsonDeserialize.class);
            if (annotation != null) {
                if (annotation.builder() != null) {
                    registerReflectiveAccess(annotation.builder());
                }
                if (annotation.using() != null) {
                    registerReflectiveAccess(annotation.using());
                }
            }
        }
    }

    private void registerJacksonSerializer() {
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(JsonSerialize.class);
        for (Class<?> type : annotated) {
            JsonSerialize annotation = type.getAnnotation(JsonSerialize.class);
            if (annotation != null) {
                if (annotation.using() != null) {
                    registerReflectiveAccess(annotation.using());
                }
            }
        }
    }

    private static void registerReflectiveAccess(Constructor constructor) {
        RuntimeReflection.registerConstructorLookup(
                constructor.getDeclaringClass(),
                constructor.getParameterTypes()
        );
    }

    private static void registerReflectiveAccess(Field field) {
        RuntimeReflection.registerFieldLookup(
                field.getDeclaringClass(),
                field.getName()
        );
    }

    private static void registerReflectiveAccess(Method method) {
        RuntimeReflection.registerMethodLookup(
                method.getDeclaringClass(),
                method.getName(),
                method.getParameterTypes()
        );
    }

    private static void registerReflectiveAccess(Class<?> type) {
        RuntimeReflection.register(type.getDeclaredConstructors());
        RuntimeReflection.register(type.getDeclaredFields());
        RuntimeReflection.register(type.getDeclaredMethods());
        RuntimeReflection.register(type.getFields());
        RuntimeReflection.register(type.getConstructors());
        RuntimeReflection.register(type.getMethods());
    }

    public Set<Class<?>> getAllTypesAnnotatedWithReflectable() {
        return reflections.getTypesAnnotatedWith(Reflectable.class, true)
                .stream()
                .flatMap(type -> type.isInterface() ?
                        ((Set<Class<?>>) reflections.getSubTypesOf(type)).stream() :
                        Stream.of(type)
                )
                .filter(JikkouRuntimeReflectionRegistrationFeature::isConcreteClassType)
                .collect(Collectors.toSet());
    }

    private static boolean isConcreteClassType(Class<?> type) {
        int mod = type.getModifiers();
        return !Modifier.isAbstract(mod) && !Modifier.isInterface(mod);
    }
}
