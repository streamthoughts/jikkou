/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.core.transform;

import io.streamthoughts.jikkou.core.annotation.AcceptsResources;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.exceptions.InvalidResourceException;
import io.streamthoughts.jikkou.core.models.ConfigMap;
import io.streamthoughts.jikkou.core.models.ConfigMapList;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.HasConfigRefs;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasPriority;
import io.streamthoughts.jikkou.core.models.HasSpec;
import io.streamthoughts.jikkou.core.resource.transform.ResourceTransformation;
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
@AcceptsResources(value = {})
public class ConfigMapsTransformation implements ResourceTransformation<HasMetadata> {


    /**
     * {@inheritDoc
     */
    @Override
    public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata resource,
                                                    @NotNull HasItems list) {
        HasMetadata result = resource;
        if (HasSpec.class.isAssignableFrom(resource.getClass())) {
            Object spec = ((HasSpec) resource).getSpec();
            if (spec instanceof HasConfigRefs) {
                result = doTransform((HasSpec<HasConfigRefs>) resource, list);
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
                    .ifPresent(config -> configsFromConfigMaps.putAll(config.toMap()));
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
