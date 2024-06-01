/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ApiVersion implements Comparable<ApiVersion> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("v(\\d+)(([a-z]+\\d+))?");

    private final int version;
    private final Qualifier qualifier;

    public static ApiVersion of(final String apiVersion) {
        Matcher matcher = VERSION_PATTERN.matcher(apiVersion);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version, cannot parse '" + apiVersion + "'");
        }

        try {
            int version = Integer.parseInt(matcher.group(1));
            if (matcher.group(2) != null) {
                String qualifier = matcher.group(3);
                return new ApiVersion(version, new Qualifier(qualifier));
            } else {
                return new ApiVersion(version, null);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid version, cannot parse '" + apiVersion + "'");
        }
    }

    private ApiVersion(@NotNull Integer version,
                       @Nullable Qualifier qualifier) {
        this.version = Objects.requireNonNull(version, "version cannot be null");
        this.qualifier = qualifier;
    }

    public int version() {
        return version;
    }

    public Qualifier qualifier() {
        return qualifier;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int compareTo(@NotNull ApiVersion that) {

        int compareMajor = Integer.compare(that.version, this.version);
        if (compareMajor != 0) {
            return compareMajor;
        }

        if (that.qualifier == null && this.qualifier == null) {
            return 0;
        } else if (that.qualifier == null) {
            return 1;
        } else if (this.qualifier == null) {
            return -1;
        }
        return this.qualifier.compareTo(that.qualifier);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiVersion that = (ApiVersion) o;
        return version == that.version && Objects.equals(qualifier, that.qualifier);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(version, qualifier);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return Stream.of("v", version, qualifier)
            .filter(Objects::nonNull)
            .map(Objects::toString)
            .collect(Collectors.joining());
    }

    /**
     * Static helper for returning the latest version from a list of {@link ApiVersion}.
     *
     * @param versions the list of version.
     * @return the latest version.
     */
    public static ApiVersion getLatest(final ApiVersion... versions) {
        if (versions.length == 0) throw new IllegalArgumentException("empty list");
        return Stream.of(versions).sorted().findFirst().get();
    }

    static final class Qualifier implements Comparable<Qualifier> {

        private static final List<String> DEFAULT_QUALIFIER_NAME;

        static {
            // order is important
            DEFAULT_QUALIFIER_NAME = new ArrayList<>();
            DEFAULT_QUALIFIER_NAME.add("alpha");
            DEFAULT_QUALIFIER_NAME.add("beta");
        }

        private final String qualifier;
        private final String label;
        private final int priority;
        private final int number;

        /**
         * Creates a new {@link Qualifier} instance.
         *
         * @param qualifier the qualifier string.
         */
        private Qualifier(final String qualifier) {
            Objects.requireNonNull(qualifier, "qualifier cannot be null");
            this.qualifier = qualifier;
            label = getUniformQualifier(qualifier);
            priority = DEFAULT_QUALIFIER_NAME.indexOf(label);
            if (priority < 0) {
                throw new IllegalArgumentException("Qualifier not supported '" + label + "'");
            }
            number = (label.length() < qualifier.length()) ? getQualifierNumber(qualifier) : 0;
        }

        public String label() {
            return label;
        }

        public int number() {
            return number;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object that) {
            if (this == that) return true;
            if (!(that instanceof Qualifier)) return false;
            return qualifier.equals(((Qualifier) that).qualifier);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(qualifier);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final Qualifier that) {
            int compare = Integer.compare(that.priority, this.priority);
            return (compare != 0) ? compare : Integer.compare(that.number, this.number);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return qualifier;
        }
    }

    private static int getQualifierNumber(final String qualifier) {
        StringBuilder label = new StringBuilder();
        char[] chars = qualifier.toCharArray();
        for (char c : chars) {
            if (Character.isDigit(c)) {
                label.append(c);
            }
        }
        return Integer.parseInt(label.toString());
    }

    private static String getUniformQualifier(final String qualifier) {
        StringBuilder label = new StringBuilder();
        char[] chars = qualifier.toCharArray();
        for (char c : chars) {
            if (Character.isLetter(c)) {
                label.append(c);
            } else {
                break;
            }
        }
        return label.toString().toLowerCase(Locale.ROOT);
    }
}