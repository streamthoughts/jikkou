/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.Set;

@InterfaceStability.Evolving
public interface HasConfigRefs extends HasConfigs {

    Set<String> getConfigMapRefs();

    void setConfigMapRefs(final Set<String> configMapsRefs);

}
