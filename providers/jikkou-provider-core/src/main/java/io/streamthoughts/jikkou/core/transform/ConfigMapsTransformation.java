/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.annotation.SupportedResources;
import io.streamthoughts.jikkou.core.exceptions.InvalidResourceException;
import io.streamthoughts.jikkou.core.models.ConfigMap;
import io.streamthoughts.jikkou.core.models.ConfigMapList;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.HasConfigRefs;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasPriority;
import io.streamthoughts.jikkou.core.models.HasSpec;
import java.util.HashMap;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * This transformation is used to apply all config-maps to topic objects.
 *
 * @see ConfigMap
 */

@Named("ConfigMapsTransformation")
@Description("Resolves ConfigMap objects for resources supporting ConfigRefs")
@Enabled
@Priority(HasPriority.HIGHEST_PRECEDENCE)
@SupportedResources
public class ConfigMapsTransformation implements Transformation<HasMetadata> {


    /**
     * {@inheritDoc
     */
    @Override
    public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata resource,
                                                    @NotNull HasItems resources,
                                                    @NotNull ReconciliationContext context) {
        HasMetadata result = resource;
        if (HasSpec.class.isAssignableFrom(resource.getClass())) {
            Object spec = ((HasSpec) resource).getSpec();
            if (spec instanceof HasConfigRefs) {
                result = doTransform((HasSpec<HasConfigRefs>) resource, resources);
            }
        }
        return Optional.of(result);
    }

    private HasMetadata doTransform(final HasSpec<HasConfigRefs> resource,
                                    final HasItems list) {
        HasConfigRefs spec = resource.getSpec();
        var configMapRefs = spec.getConfigMapRefs();

        if (configMapRefs == null || configMapRefs.isEmpty()) {
            return resource;
        }

        ConfigMapList configMapList = ConfigMapList.builder()
                .withItems(list.getAllByClass(ConfigMap.class))
                .build();

        var configsFromConfigMaps = new HashMap<String, Object>();

        for (String configMapRef : configMapRefs) {
            ConfigMap configMap = configMapList.findByName(configMapRef)
                    .orElseThrow(() -> new InvalidResourceException(String.format(
                            "Failed to process resource '%s/%s'. Cannot find ConfigMap for '%s'.",
                            resource.getApiVersion(),
                            resource.getKind(),
                            configMapRef)
                    ));
            Optional.ofNullable(configMap.getData())
                    .ifPresent(configsFromConfigMaps::putAll);
        }

        var allConfigs = Optional
                .ofNullable(spec.getConfigs())
                .orElse(Configs.empty());
        allConfigs.addAll(configsFromConfigMaps);

        spec.setConfigMapRefs(null);
        spec.setConfigs(allConfigs);

        return resource;
    }
}
