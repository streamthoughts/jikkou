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

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import java.util.Map;

public class ExtensionContextDecorator implements ExtensionContext {

    private final ExtensionContext delegate;

    /**
     * Creates a new {@link ExtensionContextDecorator} instance.
     * @param context   The ExtensionContext.
     */
    public ExtensionContextDecorator(ExtensionContext context) {
        this.delegate = context;
    }

    /** {@inheritDoc} **/
    @Override
    public String name() {
        return delegate.name();
    }
    /** {@inheritDoc} **/
    @Override
    public Configuration appConfiguration() {
        return delegate.appConfiguration();
    }
    /** {@inheritDoc} **/
    @Override
    public Map<String, ConfigProperty> configProperties() {
        return delegate.configProperties();
    }
    /** {@inheritDoc} **/
    @Override
    public <T> ConfigProperty<T> configProperty(String key) {
        return delegate.configProperty(key);
    }
    /** {@inheritDoc} **/
    @Override
    public ExtensionContext contextForExtension(Class<? extends Extension> extension) {
        return delegate.contextForExtension(extension);
    }
}
