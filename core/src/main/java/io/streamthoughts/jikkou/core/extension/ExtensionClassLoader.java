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
import java.util.Collections;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default ClassLoader for loading components using a 'child-first strategy'. In other words, this ClassLoader
 * attempts to find the class in its own context before delegating to the parent ClassLoader.
 */
public class ExtensionClassLoader extends URLClassLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionClassLoader.class);

    static {
        ClassLoader.registerAsParallelCapable();
    }

    // The default list of packages to delegate loading to parent classloader.
    private static final Pattern DEFAULT_PACKAGES_TO_IGNORE = Pattern.compile("(?:"
            + "|io.streamthoughts.azkarra"
            + "|org\\.apache\\.kafka"
            + "|org.slf4j"
            + ")\\..*$");

    private final ClassLoader parent;

    private final URL extensionLocation;

    private final ClassLoader systemClassLoader;

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
     * @param extensionLocation the top-level component location.
     * @param urls              the URLs from which to load classes and resources.
     * @param parent            the parent {@link ClassLoader}.
     */
    private ExtensionClassLoader(final URL extensionLocation,
                                 final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
        this.parent = parent;
        this.extensionLocation = extensionLocation;
        this.systemClassLoader = getSystemClassLoader();
    }

    public String location() {
        return extensionLocation.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {

        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> loadedClass = findLoadedClass(name);

            if (loadedClass == null) {
                // protect from impersonation of system classes (e.g: java.*)
                loadedClass = mayLoadFromSystemClassLoader(name);
            }

            if (loadedClass == null && shouldLoadFromUrls(name)) {
                try {
                    // find the class from given jar urls
                    loadedClass = findClass(name);
                } catch (final ClassNotFoundException e) {
                    LOG.trace(
                            "Class '{}' not found in extensionLocation {}. Delegating to parent",
                            name,
                            extensionLocation
                    );
                }
            }

            if (loadedClass == null) {
                // If still not found, then delegate to parent classloader.
                loadedClass = super.loadClass(name, resolve);
            }

            if (resolve) {      // marked to resolve
                resolveClass(loadedClass);
            }
            return loadedClass;
        }
    }

    private static boolean shouldLoadFromUrls(final String name) {
        return !DEFAULT_PACKAGES_TO_IGNORE.matcher(name).matches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration<URL> getResources(String name) throws IOException {
        Objects.requireNonNull(name);

        final Enumeration<URL>[] e = (Enumeration<URL>[]) new Enumeration<?>[3];

        // First, load resources from system class loader
        e[0] = getResourcesFromSystem(name);

        // load resource from this classloader
        e[1] =  findResources(name);

        // then try finding resources from parent class-loaders
        e[2] = getParent().getResources(name);

        return new CompoundEnumeration(e);
    }

    private Enumeration<URL> getResourcesFromSystem(final String name) throws IOException {
        if (systemClassLoader != null) {
            return systemClassLoader.getResources(name);
        }
        return Collections.emptyEnumeration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getResource(final String name) {
        Objects.requireNonNull(name);
        URL res = null;
        if (systemClassLoader != null)
            res = systemClassLoader.getResource(name);

        if (res == null)
            res = findResource(name);

        if (res == null)
            res = getParent().getResource(name);

        return res;
    }

    private Class<?> mayLoadFromSystemClassLoader(final String name) {
        Class<?> loadedClass = null;
        try {
            if (systemClassLoader != null) {
                loadedClass = systemClassLoader.loadClass(name);
            }
        } catch (final ClassNotFoundException ex) {
            // silently ignored
        }
        return loadedClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ExtensionClassLoader[location=" + extensionLocation + "] ";
    }

    public URL extensionLocation() {
        return extensionLocation;
    }

    static final class CompoundEnumeration<E> implements Enumeration<E> {
        private final Enumeration<E>[] enums;
        private int index;

        CompoundEnumeration(Enumeration<E>[] enums) {
            this.enums = enums;
        }

        private boolean next() {
            while (index < enums.length) {
                if (enums[index] != null && enums[index].hasMoreElements()) {
                    return true;
                }
                index++;
            }
            return false;
        }

        public boolean hasMoreElements() {
            return next();
        }

        public E nextElement() {
            if (!next()) {
                throw new NoSuchElementException();
            }
            return enums[index].nextElement();
        }
    }
}
