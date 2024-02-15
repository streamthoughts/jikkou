/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models.change;

import io.streamthoughts.jikkou.core.reconciler.TextDescription;

/**
 * Provides a default serializable/deserializable implementation of the {@link TextDescription} interface.
 *
 * @param textual the textual description.
 */
public record DefaultTextDescription(String textual) implements TextDescription { }
