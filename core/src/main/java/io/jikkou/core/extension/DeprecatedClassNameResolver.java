/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves deprecated class names from the old {@code io.streamthoughts.jikkou} package prefix
 * to the new {@code io.jikkou} prefix.
 *
 * <p>This class provides backward compatibility for user configurations that reference
 * extensions by their old fully qualified class names. A deprecation warning is logged
 * when an old prefix is detected.</p>
 *
 * <p>Support for old class names will be removed in the next major version.</p>
 */
public final class DeprecatedClassNameResolver {

    private static final Logger LOG = LoggerFactory.getLogger(DeprecatedClassNameResolver.class);

    // IMPORTANT: Do not refactor these string literals — the old prefix must remain as-is
    // for backward compatibility with user configurations.
    private static final String OLD_PREFIX = "io.streamthoughts.jikkou.";
    private static final String NEW_PREFIX = "io.jikkou.";

    private DeprecatedClassNameResolver() {}

    /**
     * Resolves a potentially deprecated class name to the current name.
     * If the given class name starts with the old prefix, it is rewritten to use
     * the new prefix and a deprecation warning is logged.
     *
     * @param className the class name (FQCN or alias) to resolve. May be {@code null}.
     * @return the resolved class name with the new prefix, or the original if no match.
     */
    public static String resolve(String className) {
        if (className != null && className.startsWith(OLD_PREFIX)) {
            String resolved = NEW_PREFIX + className.substring(OLD_PREFIX.length());
            LOG.warn("Deprecated class name '{}' detected in configuration. "
                + "Please update to '{}'. Support for the old 'io.streamthoughts.jikkou' "
                + "package prefix will be removed in the next major version.",
                className, resolved);
            return resolved;
        }
        return className;
    }

    /**
     * Returns {@code true} if the given class name uses the deprecated package prefix.
     *
     * @param className the class name to check. May be {@code null}.
     * @return {@code true} if deprecated, {@code false} otherwise.
     */
    public static boolean isDeprecated(String className) {
        return className != null && className.startsWith(OLD_PREFIX);
    }

    /**
     * Converts a new-prefix class name to its deprecated old-prefix equivalent.
     * Used internally to register backward-compatible aliases.
     *
     * @param className the class name with the new prefix. May be {@code null}.
     * @return the old-prefix equivalent, or the original if it doesn't start with the new prefix.
     */
    public static String toDeprecatedName(String className) {
        if (className != null && className.startsWith(NEW_PREFIX)) {
            return OLD_PREFIX + className.substring(NEW_PREFIX.length());
        }
        return className;
    }
}
