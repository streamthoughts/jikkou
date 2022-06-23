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
package io.streamthoughts.jikkou.api;

import static java.util.Arrays.stream;

import io.streamthoughts.jikkou.api.model.Nameable;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceByNameFilter implements ResourceFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceByNameFilter.class);

    // The regex patterns to use for excluding resources.
    private final Pattern[] excludes;

   // The regex patterns to use for including resources.
    private final Pattern[] includes;

    /**
     * Creates a new {@link ResourceByNameFilter} instance.
     */
    public ResourceByNameFilter() {
        this(null, null);
    }

    private ResourceByNameFilter(@Nullable final Pattern[] excludes,
                                 @Nullable final Pattern[] includes) {
        this.excludes = excludes;
        this.includes = includes;
    }

    /**
     * Creates a new {@link ResourceByNameFilter} with the given excluding patterns.
     *
     * @param excludes  list of {@link Pattern} to exclude resources.
     * @return a new {@link ResourceByNameFilter}.
     */
    public ResourceByNameFilter withExcludes(@Nullable final Pattern[] excludes) {
        return new ResourceByNameFilter(excludes, includes);
    }

    /**
     * Creates a new {@link ResourceByNameFilter} with the given including patterns.
     *
     * @param includes  list of {@link Pattern} to include resources.
     * @return a new {@link ResourceByNameFilter}.
     */
    public ResourceByNameFilter withIncludes(@Nullable final Pattern[] includes) {
        return new ResourceByNameFilter(excludes, includes);
    }

    /** {@inheritDoc} **/
    @Override
    public Predicate<String> getPredicateByName() {
        return this::test;
    }

    private boolean test(final Nameable resourceName) {
        final boolean candidate = test(resourceName.getName());
        if (!candidate) {
            LOG.info("Excluded resource with name '{}'.", resourceName);
        }
        return candidate;
    }

    private final boolean test(final String resourceName) {
        final boolean candidate = includePredicate().test(resourceName) && !excludePredicate().test(resourceName);
        if (!candidate) {
            LOG.info("Excluded resource with name '{}'.", resourceName);
        }
        return candidate;
    }

    private Predicate<String> includePredicate() {
        return s -> Optional.ofNullable(includes)
                .map(patterns -> patterns.length == 0 || stream(patterns).anyMatch(m -> m.matcher(s).matches()))
                .orElse(true);
    }

    private Predicate<String> excludePredicate() {
        return s -> Optional.ofNullable(excludes)
                .map(patterns -> patterns.length != 0 && stream(patterns).anyMatch(m -> m.matcher(s).matches()))
                .orElse(false);
    }

}
