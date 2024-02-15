/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to keeps track of acquired shared resources and handles their allocation disposal.
 */
public final class SharedResources {

    private static final Logger LOG = LoggerFactory.getLogger(SharedResources.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final HashMap<String, LeasedResource<?>> leasedResources = new HashMap<>();


    /**
     * Gets a shared resource and registers a lease.
     * If the object does not yet exist, it will be created through the given initializer.
     *
     * @param name        the store name.
     * @param initializer the initializer function.
     * @param leaseHolder the lease to register.
     * @return the shared resource.
     */
    public <T extends AutoCloseable> T getOrCreateSharedResource(
            final String name,
            final ResourceInitializer<T> initializer,
            final Object leaseHolder
    ) {
        lock.lock();
        try {
            @SuppressWarnings("unchecked")
            LeasedResource<T> leasedResource = (LeasedResource<T>) this.leasedResources.get(name);
            if (leasedResource == null) {
                LOG.info("Initializing shared resource '{}'", name);
                T resource = initializer.apply();
                leasedResource = new LeasedResource<>(resource);
                this.leasedResources.put(name, leasedResource);
            }

            leasedResource.addLeaseHolder(leaseHolder);
            return leasedResource.getResource();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Releases a lease (identified by the lease holder object) for the given type. If no further
     * leases exist, the resource is disposed.
     *
     * @param name        the name of the resource.
     * @param leaseHolder the lease holder object.
     * @param <T>         type of the resource.
     */
    public <T extends AutoCloseable> void release(@NotNull final String name,
                                                  @NotNull final Object leaseHolder) throws Exception {

        release(name, leaseHolder, AutoCloseable::close);

    }

    /**
     * Releases a lease (identified by the lease holder object) for the given type. If no further
     * leases exist, the resource is disposed.
     *
     * @param name        the name of the resource.
     * @param leaseHolder the lease holder object.
     * @param <T>         type of the resource.
     */
    public <T extends AutoCloseable> void release(@NotNull final String name,
                                                  @NotNull final Object leaseHolder,
                                                  @NotNull final SharedResources.ThrowingConsumer<T, Exception> releaser
    ) throws Exception {
        lock.lock();
        try {
            @SuppressWarnings("unchecked")
            LeasedResource<T> leasedResource = (LeasedResource<T>) this.leasedResources.get(name);
            if (leasedResource == null) {
                return;
            }
            LOG.info("Releasing access on shared resource '{}' instance.", name);

            if (leasedResource.removeLeaseHolder(leaseHolder)) {
                LOG.info("Closing shared resource '{}'", name);
                leasedResources.remove(name);
                leasedResource.dispose(releaser);
            }
        } finally {
            lock.unlock();
        }
    }

    private final static class LeasedResource<T extends AutoCloseable> {

        private final T resource;
        private final HashSet<Object> leaseHolders = new HashSet<>();
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /**
         * Creates a new {@link LeasedResource} instance.
         *
         * @param resource the resource.
         */
        public LeasedResource(final T resource) {
            this.resource = Objects.requireNonNull(resource, "resource should not be null");
        }

        /**
         * @return the handle resource.
         */
        public T getResource() {
            return resource;
        }

        /**
         * Adds a new lease to the handle resource.
         *
         * @param leaseHolder the leaseholder object.
         */
        void addLeaseHolder(final Object leaseHolder) {
            leaseHolders.add(leaseHolder);
        }

        /**
         * Removes the given leaseholder.
         *
         * @param leaseHolder the leaseholder object.
         * @return {@code true} is not use anymore, and can be disposed.
         */
        boolean removeLeaseHolder(final Object leaseHolder) {
            leaseHolders.remove(leaseHolder);
            return leaseHolders.isEmpty();
        }

        /**
         * Disposes the resource handled.
         */
        public void dispose(ThrowingConsumer<T, Exception> releaser) throws Exception {
            if (closed.compareAndSet(false, true)) {
                releaser.accept(resource);
            }
        }
    }

    public interface ThrowingConsumer<T, E extends Throwable> {
        void accept(T object) throws E;
    }
}
