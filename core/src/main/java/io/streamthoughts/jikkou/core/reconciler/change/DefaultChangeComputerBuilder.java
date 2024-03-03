/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.change;

/**
 * Service interface for computing the changes required for reconciling resources.
 *
 * @param <T> The type of the resource.
 */
public final class DefaultChangeComputerBuilder<I, T, R> implements ChangeComputerBuilder<I, T, R> {

    private boolean isDeleteOrphans = false;
    private KeyMapper<T, I> keyMapper;
    private ChangeFactory<I, T, R> changeFactory;

    /**
     * Creates a new {@link DefaultChangeComputerBuilder} instance.
     */
    public DefaultChangeComputerBuilder() {
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultChangeComputerBuilder<I, T, R> withDeleteOrphans(boolean isDeleteOrphans) {
        this.isDeleteOrphans = isDeleteOrphans;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultChangeComputerBuilder<I, T, R> withKeyMapper(final KeyMapper<T, I> keyMapper) {
        this.keyMapper = keyMapper;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultChangeComputerBuilder<I, T, R> withChangeFactory(final ChangeFactory<I, T, R> changeFactory) {
        this.changeFactory = changeFactory;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ChangeComputer<T, R> build() {
        return new DefaultChangeComputer<>(isDeleteOrphans, keyMapper, changeFactory);
    }
}
