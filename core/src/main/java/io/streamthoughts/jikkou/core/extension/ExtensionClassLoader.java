/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A custom {@link ClassLoader} dedicated for loading Jikkou extension classes using a 'child-first strategy'.
 * <p>
 * This {@link ClassLoader} attempts to find the class in its own context before delegating to the parent ClassLoader.
 */
public class ExtensionClassLoader extends URLClassLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionClassLoader.class);

    static {
        ClassLoader.registerAsParallelCapable();
    }

    // The default list of packages to delegate loading to parent classloader.
    private static final Pattern PACKAGES_TO_EXCLUDE = Pattern.compile("(?:"
        + "java"
        + "|javax"
        + "|io\\.streamthoughts\\.jikkou\\.core"
        + "|io\\.streamthoughts\\.jikkou\\.common"
        + "|io\\.streamthoughts\\.jikkou\\.runtime"
        + "|io\\.streamthoughts\\.jikkou\\.spi"
        + "|org\\.apache\\.kafka"
        + "|org\\.slf4j"
        + ")\\..*$");

    private final URL extensionLocation;

    /**
     * Static helper method to create a new {@link ExtensionClassLoader} instance.
     *
     * @param extensionLocation The top-level location of the extension to be loaded.
     * @param urls              The list of {@link URL urls} from which to load classes and resources.
     * @param parent            The parent {@link ClassLoader}.
     * @return a new {@link ExtensionClassLoader}.
     */
    public static ExtensionClassLoader newClassLoader(final URL extensionLocation,
                                                      final URL[] urls,
                                                      final ClassLoader parent) {
        return AccessController.doPrivileged(
            (PrivilegedAction<ExtensionClassLoader>) () -> new ExtensionClassLoader(extensionLocation, urls, parent)
        );
    }

    /**
     * Creates a new {@link ExtensionClassLoader} instance.
     *
     * @param extensionLocation The top-level location of the extension to be loaded.
     * @param urls              The URLs from which to load classes and resources.
     * @param parent            The parent {@link ClassLoader}.
     */
    private ExtensionClassLoader(final URL extensionLocation,
                                 final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
        this.extensionLocation = extensionLocation;
    }

    /**
     * Gets the top-level location of the extension to be loaded.
     *
     * @return the string location.
     */
    public String location() {
        return extensionLocation.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> klass = findLoadedClass(name);
            if (klass == null) {
                try {
                    if (shouldLoadInIsolation(name)) {
                        klass = findClass(name);
                    }
                } catch (ClassNotFoundException e) {
                    // Not found in loader's path. Search in parents.
                    LOG.trace("Class '{}' not found in extension location '{}'. Delegating to parent", name, extensionLocation);
                }
            }
            if (klass == null) {
                klass = super.loadClass(name, false);
            }
            if (resolve) {
                resolveClass(klass);
            }
            return klass;
        }
    }

    private static boolean shouldLoadInIsolation(final String name) {
        return !PACKAGES_TO_EXCLUDE.matcher(name).matches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Objects.requireNonNull(name);
        List<URL> resources = new ArrayList<>();
        // First, attempt to find the resource locally
        for (Enumeration<URL> foundLocally = findResources(name); foundLocally.hasMoreElements(); ) {
            URL url = foundLocally.nextElement();
            if (url != null) {
                resources.add(url);
            }
        }
        // Explicitly call the parent implementation instead of super to avoid double-listing the local resources
        for (Enumeration<URL> foundByParent = getParent().getResources(name); foundByParent.hasMoreElements(); ) {
            URL url = foundByParent.nextElement();
            if (url != null)
                resources.add(url);
        }
        return Collections.enumeration(resources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getResource(final String name) {
        Objects.requireNonNull(name);

        URL url = findResource(name);
        if (url == null) {
            url = super.getResource(name);
        }
        return url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ExtensionClassLoader[location=" + extensionLocation + "] ";
    }
}
