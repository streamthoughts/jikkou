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
    public DefaultChangeComputerBuilder<I, T, R> withKeyMapper(KeyMapper<T, I> keyMapper) {
        this.keyMapper = keyMapper;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultChangeComputerBuilder<I, T, R> withChangeFactory(ChangeFactory<I, T, R> changeFactory) {
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
