/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.config.Configuration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an immutable list of {@link ApiConfigurator}
 */
public class ApiConfiguratorList implements ApiConfigurator {

    private final List<ApiConfigurator> configurators;

    /**
     * Creates a new {@link ApiConfiguratorList} instance.
     */
    public ApiConfiguratorList() {
        this(Collections.emptyList());
    }

    /**
     * Creates a new {@link ApiConfiguratorList} instance.
     */
    public ApiConfiguratorList(final Collection<ApiConfigurator> configurators) {
        this.configurators = new ArrayList<>(configurators);
    }

    public ApiConfiguratorList with(final ApiConfigurator... configurators) {
        if (configurators.length == 0) {
            return this;
        }
        LinkedList<ApiConfigurator> all = new LinkedList<>(this.configurators);
        for (ApiConfigurator f : configurators) {
            if (f != null) {
                all.add(f);
            }
        }
        return new ApiConfiguratorList(all);
    }

    public ApiConfiguratorList with(final Iterable<ApiConfigurator> configurators) {
        LinkedList<ApiConfigurator> all = new LinkedList<>(this.configurators);
        configurators.forEach(field -> {
            if (field != null) {
                all.add(field);
            }
        });
        return new ApiConfiguratorList(all);
    }

    public ApiConfiguratorList with(final ApiConfigurator configurator) {
        LinkedList<ApiConfigurator> all = new LinkedList<>(configurators);
        all.add(configurator);
        return new ApiConfiguratorList(all);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(final B builder,
                                                                                   final Configuration configuration) {
        B result = builder;
        for (ApiConfigurator configurator : configurators) {
            result = configurator.configure(builder, configuration);
        }
        return result;
    }
}