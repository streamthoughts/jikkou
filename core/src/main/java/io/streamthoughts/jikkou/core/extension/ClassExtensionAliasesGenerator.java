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
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.action.Action;
import io.streamthoughts.jikkou.core.converter.Converter;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.core.validation.Validation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * {@link ClassExtensionAliasesGenerator} can be used to generate aliases from extension class.
 */
public final class ClassExtensionAliasesGenerator implements ExtensionAliasesGenerator {

    private static final ClassAliasExtractor DEFAULT_ALIAS_EXTRACTOR = new ClassAliasExtractor() {
        @Override
        public boolean accept(Class<?> extensionClass) {
            return true;
        }

        @Override
        public String extractAlias(Class<?> extensionClass) {
            return extensionClass.getSimpleName();
        }
    };

    private final List<ClassAliasExtractor> extractors;

    /**
     * Creates a new {@link ClassExtensionAliasesGenerator} instance.
     */
    public ClassExtensionAliasesGenerator() {
        extractors = new ArrayList<>();
        addClassAliasExtractor(DEFAULT_ALIAS_EXTRACTOR);
        addClassAliasExtractor(
                new DropClassNameSuffixExtractor("Extension", cls -> true));
        addClassAliasExtractor(
                new DropClassNameSuffixExtractor("Validation", Validation.class::isAssignableFrom));
        addClassAliasExtractor(
                new DropClassNameSuffixExtractor("Transformation", Transformation.class::isAssignableFrom));
        addClassAliasExtractor(
                new DropClassNameSuffixExtractor("Converter", Converter.class::isAssignableFrom));
        addClassAliasExtractor(
                new DropClassNameSuffixExtractor("Action", Action.class::isAssignableFrom));
    }

    public void addClassAliasExtractor(final ClassAliasExtractor extractor) {
        Objects.requireNonNull(extractor, "extractor cannot be null");
        this.extractors.add(extractor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAliasesFor(final ExtensionDescriptor<?> descriptor,
                                     final List<ExtensionDescriptor<?>> allDescriptors) {
        final Set<String> aliases = computeAllAliasesFor(descriptor, extractors);
        boolean match = false;
        for (ExtensionDescriptor<?> other : allDescriptors) {
            final Set<String> otherAliases = computeAllAliasesFor(other, extractors);
            if (aliases.equals(otherAliases)) {
                if (match) {
                    return Collections.emptySet();
                }
                match = true;
            } else {
                for (final String otherAlias : otherAliases) {
                    if (aliases.contains(otherAlias)) {
                        if (match) {
                            return Collections.emptySet();
                        }
                        match = true;
                        break;
                    }
                }
            }
        }
        return aliases;
    }

    private Set<String> computeAllAliasesFor(final ExtensionDescriptor<?> extension,
                                             final Collection<? extends ClassAliasExtractor> extractors) {

        final Class<?> providerClass = extension.type();
        final String simpleName = providerClass.getSimpleName();
        final Set<String> aliases = new HashSet<>();
        aliases.add(simpleName);
        for (ClassAliasExtractor extractor : extractors) {
            if (extractor.accept(providerClass)) {
                aliases.add(extractor.extractAlias(providerClass));
            }
        }
        return new TreeSet<>(aliases);
    }

    public interface ClassAliasExtractor {

        boolean accept(final Class<?> extensionClass);

        String extractAlias(final Class<?> extensionClass);
    }

    public static class DropClassNameSuffixExtractor implements ClassAliasExtractor {

        private final String suffix;
        private final Predicate<Class<?>> accept;

        DropClassNameSuffixExtractor(final String suffix,
                                     final Predicate<Class<?>> accept) {
            this.suffix = suffix;
            this.accept = accept;
        }

        @Override
        public boolean accept(Class<?> extensionClass) {
            return accept.test(extensionClass);
        }

        @Override
        public String extractAlias(final Class<?> extensionClass) {
            return Strings.pruneSuffix(extensionClass.getSimpleName(), suffix);
        }
    }
}