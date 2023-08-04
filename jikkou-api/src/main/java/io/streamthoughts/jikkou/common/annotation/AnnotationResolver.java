/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.common.annotation;

import static io.streamthoughts.jikkou.common.utils.Classes.getAllSuperTypes;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class AnnotationResolver {

    private static final PackageAnnotationFilter JAVA_PACKAGES = new PackageAnnotationFilter("java", "javax");
    private static final PackageAnnotationFilter KOTLIN_PACKAGES = new PackageAnnotationFilter("kotlin");


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
