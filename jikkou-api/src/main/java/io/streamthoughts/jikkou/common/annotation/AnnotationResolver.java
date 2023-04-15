/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.common.annotation;

import static io.streamthoughts.jikkou.common.utils.Classes.getAllSuperTypes;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AnnotationResolver {

    private static final PackageAnnotationFilter JAVA_PACKAGES = new PackageAnnotationFilter("java", "javax");
    private static final PackageAnnotationFilter KOTLIN_PACKAGES = new PackageAnnotationFilter("kotlin");

    public static boolean isAnnotationOfType(final Annotation annotation,
                                             final Class<?> type) {
        return new TypeAnnotationFilter(annotation).matches(type);
    }

    public static boolean isAnnotationContainer(final Class<? extends Annotation> annotationType) {
        try {
            final Method value = annotationType.getDeclaredMethod("value");
            Class<?> returnType = value.getReturnType();
            return returnType.isArray() && returnType.getComponentType().isAnnotation();
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }

    public static List<Annotation> findAllAnnotations(final Class<?> cls) {
        final var annotations = new ArrayList<Annotation>();
        for (final Class<?> t : getAllSuperTypes(cls)) {
            // add all declared annotations
            var declared = Arrays.stream(t.getDeclaredAnnotations())
                .filter(Predicate.not(JAVA_PACKAGES::matches))
                .filter(Predicate.not(KOTLIN_PACKAGES::matches))
                .flatMap(AnnotationResolver::mayUnwrapAnnotationContainer)
                .toList();
            annotations.addAll(declared);

            // Then, lookup annotations on each declared annotation
            declared.stream()
                .flatMap(annotation -> findAllAnnotations(annotation.annotationType()).stream())
                .forEach(annotations::add);
        }
        return annotations;
    }

    private static Stream<Annotation> mayUnwrapAnnotationContainer(final Annotation annotation) {
        if (!isAnnotationContainer(annotation.annotationType()))
            return Stream.of(annotation);

        try {
            Method value = annotation.annotationType().getDeclaredMethod("value");
            return Arrays.stream((Annotation[])value.invoke(annotation));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Annotation> List<T> findAllAnnotationsByType(final Method method,
                                                                          final Class<T> annotationType) {
        final var annotations = new ArrayList<T>();
        annotations.addAll(Arrays.asList(method.getDeclaredAnnotationsByType(annotationType)));
        annotations.addAll(findAllAnnotationsByType(method.getDeclaredAnnotations(), annotationType));
        return annotations;
    }

    public static <T extends Annotation> List<T> findAllAnnotationsByType(final Class<?> cls,
                                                                          final Class<T> annotationType) {
        final var annotations = new ArrayList<T>();
        for (final Class<?> t : getAllSuperTypes(cls)) {
            annotations.addAll(Arrays.asList(t.getDeclaredAnnotationsByType(annotationType)));
            annotations.addAll(findAllAnnotationsByType(t.getDeclaredAnnotations(), annotationType));
        }
        return annotations;
    }

    private static <T extends Annotation> List<T> findAllAnnotationsByType(final Annotation[] annotations,
                                                                           final Class<T> annotationType) {
        final var typeAnnotationFilter = new TypeAnnotationFilter(annotationType);
        return Arrays.stream(annotations)
            .filter(Predicate.not(JAVA_PACKAGES::matches))
            .filter(Predicate.not(KOTLIN_PACKAGES::matches))
            .filter(Predicate.not(typeAnnotationFilter::matches))
            .flatMap(annotation -> findAllAnnotationsByType(annotation.annotationType(), annotationType).stream())
            .collect(Collectors.toList());
    }

    public static <A extends Annotation> boolean isAnnotatedWith(final Method method,
                                                                 final Class<A> annotation) {

        return method.getDeclaredAnnotation(annotation) != null;
    }

    public static <A extends Annotation> boolean isAnnotatedWith(final Class<?> component,
                                                                 final Class<A> annotation) {
        for (Class<?> t : getAllSuperTypes(component)) {
            A[] declared = t.getDeclaredAnnotationsByType(annotation);
            if (declared.length > 0) {
                return true;
            }
        }
        return false;
    }

    public interface AnnotationFilter {

        default boolean matches(final Annotation annotation) {
            return matches(annotation.annotationType());
        }

        default boolean matches(final Class<?> annotationType) {
            return matches(annotationType.getName());
        }

        boolean matches(final String typeName);
    }

    public static class PackageAnnotationFilter implements AnnotationFilter {

        private final String[] prefixes;

        PackageAnnotationFilter(final String... packages) {
            prefixes = Arrays.stream(packages)
               .filter(Predicate.not(String::isBlank))
               .map(pkg -> pkg + ".")
               .toArray(String[]::new);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(final String typeName) {
            return Arrays.stream(prefixes).anyMatch(typeName::startsWith);
        }
    }

    public static class TypeAnnotationFilter implements AnnotationFilter {

        private final Class<?> annotationType;

        TypeAnnotationFilter(final Annotation annotation) {
            this(annotation.annotationType());
        }

        TypeAnnotationFilter(final Class<?> annotationType) {
            this.annotationType = Objects.requireNonNull(annotationType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(final String typeName) {
            return typeName.equals(annotationType.getName());
        }
    }

}
