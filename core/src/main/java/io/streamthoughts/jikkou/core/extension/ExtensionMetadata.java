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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ExtensionMetadata {

    private static final Function<Object, Object> IDENTITY = object -> object;

    private final Collection<ExtensionAttribute> attributes;

    /**
     * Creates a new {@link ExtensionMetadata} instance.
     */
    public ExtensionMetadata() {
        this(new LinkedList<>());
    }

    /**
     * Creates a new {@link ExtensionMetadata} instance.
     *
     * @param attributes    the component attributes.
     */
    private ExtensionMetadata(final Collection<ExtensionAttribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * Gets all attributes.
     *
     * @return      the list of {@link ExtensionAttribute}.
     */
    public Collection<ExtensionAttribute> attributes() {
        return Collections.unmodifiableCollection(attributes);
    }

    /**
     * Adds the specified attribute.
     *
     * @param attribute the attribute to add.
     */
    public void addAttribute(final ExtensionAttribute attribute) {
        attributes.add(Objects.requireNonNull(attribute, "attribute cannot be null"));
    }

    /**
     * Gets all attributes for the given name.
     *
     * @param name  the name of attributes.
     * @return      the list of matching {@link ExtensionAttribute}.
     */
    public Collection<ExtensionAttribute> attributesForName(final String name) {
        return attributes.stream()
                .filter(attr -> attr.name().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    /**
     * Finds the value for the specified attribute name and member.
     *
     * @param name      the name of the attribute.
     * @param member    the member of the attribute.
     * @return          the {@link Optional} value.
     */
    public Optional<Object> value(final String name, final String member) {
        return findValues(name, member, IDENTITY).findFirst();
    }

    /**
     * Finds all values for the specified attribute name and member.
     *
     * @param name      the name of the attribute.
     * @param member    the member of the attribute.
     * @return          the {@link Optional} value.
     */
    public Collection<Object> values(final String name, final String member) {
        return findValues(name, member, IDENTITY).collect(Collectors.toList());
    }

    /**
     * Gets the string value for the specified attribute name and member.
     *
     * @param name      the name of the attribute.
     * @param member    the member of the attribute.
     * @return          the string value if found, otherwise {@code null}.
     */
    public Collection<String> stringValues(final String name, final String member) {
        return findValues(name, member, Object::toString).collect(Collectors.toList());
    }

    /**
     * Gets the string value for the specified attribute name and member.
     *
     * @param name      the name of the attribute.
     * @param member    the member of the attribute.
     * @return          the string value if found, otherwise {@code null}.
     */
    public String stringValue(final String name, final String member) {
        return findValues(name, member, Object::toString).findFirst().orElse(null);
    }

    /**
     * Gets the array value for the specified attribute name and member.
     *
     * @param name      the name of the attribute.
     * @param member    the member of the attribute.
     *
     * @return          the array value if found, otherwise {@code null}.
     */
    public String[] arrayValue(final String name, final String member) {
        return findValues(name, member, v -> (String[])v).findFirst().orElse(new String[0]);
    }

    /**
     * Checks whether the metadata contains the given value for the given attribute name and member.
     *
     * @param name      the name of the attribute.
     * @param member    the member of the attribute.
     * @param value     the member value.
     *
     * @return          {@code true} if the value exist, otherwise {@code false}.
     */
    public boolean contains(final String name, final String member, Object value) {
        Collection<ExtensionAttribute> attributes = attributesForName(name);
        for (ExtensionAttribute attribute : attributes) {
            if (attribute.contains(member, value)) {
                return true;
            }
        }
        return false;
    }

    private <T> Stream<T> findValues(final String name, final String member, final Function<Object, T> mapper) {
        return attributesForName(name).stream()
                .map(attribute -> attribute.value(member))
                .filter(Objects::nonNull)
                .map(mapper);
    }
    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtensionMetadata that = (ExtensionMetadata) o;
        return Objects.equals(attributes, that.attributes);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(attributes);
    }

    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return "[ attributes=" + attributes + ']';
    }
}
