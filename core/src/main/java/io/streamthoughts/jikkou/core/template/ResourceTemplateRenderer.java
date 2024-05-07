/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.template;

import io.streamthoughts.jikkou.core.config.Configurable;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for rendering resource template.
 */
public interface ResourceTemplateRenderer extends Configurable {

    /**
     * Render the given resource template.
     *
     * @param template  the template to be rendered.
     * @param bindings
     *
     * @return          the resource rendered.
     */
    String render(@NotNull String template,
                  @NotNull TemplateBindings bindings);
}
