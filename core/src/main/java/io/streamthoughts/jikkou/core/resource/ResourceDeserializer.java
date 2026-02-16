/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.resource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Reflectable
public final class ResourceDeserializer extends JsonDeserializer<Resource> {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceDeserializer.class);

    private static final Mapping mapping = new Mapping();

    private static final List<ResourceTypeResolver> resolvers = new LinkedList<>();

    /**
     * Creates a new {@link ResourceDeserializer} instance.
     */
    public ResourceDeserializer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = parser.readValueAsTree();
        if (node.isObject()) {
            return fromObjectNode(parser, node);
        } else {
            return null;
        }
    }

    private static Resource fromObjectNode(JsonParser jp, JsonNode node) throws IOException {
        Class<? extends Resource> resourceType = null;
        boolean resolvedByFallback = false;

        ResourceType key = ResourceType.of(node);

        if (key != null) {
            LOG.debug("Looking for specialized resource for: group={} apiVersion={}, kind={}.",
                key.group(),
                key.apiVersion(),
                key.kind()
            );
            resourceType = mapping.getForKey(key);
            // debug
            if (resourceType == null) {
                LOG.debug("Cannot found specialized resource for: group={} apiVersion={}, kind={}.",
                    key.group(),
                    key.apiVersion(),
                    key.kind()
                );
            }
        }

        if (resourceType == null) {
            if (!resolvers.isEmpty()) {
                LOG.debug("Looking for ResourceTypeResolver for untyped resource.");
                for (ResourceTypeResolver r : resolvers) {
                    resourceType = r.resolvesType(node);
                    if (resourceType != null) {
                        resolvedByFallback = true;
                        break;
                    }
                }
            } else {
                LOG.debug("No resource type resolved registered.");
            }
        }

        if (resourceType == null) {
            if (key != null) {
                LOG.debug("No specific resource found for group={} apiVersion={}, kind={}. Use GenericResource.",
                    key.group(),
                    key.apiVersion(),
                    key.kind()
                );
            }
            return jp.getCodec().treeToValue(node, GenericResource.class);
        } else if (Resource.class.isAssignableFrom(resourceType)) {
            String resolvedApiVersion = Resource.getApiVersion(resourceType);
            LOG.debug("Read specific resource for apiVersion={}, kind={}.",
                resolvedApiVersion,
                Resource.getKind(resourceType)
            );
            // Normalize the apiVersion in the node when the type was resolved by a fallback
            // resolver (e.g., LatestApiVersionResourceTypeResolver). This ensures that the
            // deserialized resource carries the canonical apiVersion of the resolved class,
            // not the (potentially outdated) version from the YAML input.
            if (resolvedByFallback && resolvedApiVersion != null && node.isObject()) {
                ((ObjectNode) node).put("apiVersion", resolvedApiVersion);
            }
            return jp.getCodec().treeToValue(node, resourceType);
        }
        LOG.warn("Failed get resource type from JsonNode");
        return null;
    }

    /**
     * Registers a specific Resource Definition resolver.
     */
    public static void registerResolverType(final @NotNull ResourceTypeResolver resolver) {
        resolvers.add(resolver);
    }

    /**
     * Registers a Resource Definition Kind
     */
    public static void registerKind(final Class<? extends Resource> clazz) {
        mapping.registerKind(clazz);
    }

    /**
     * Registers a Resource Definition Kind
     */
    public static void registerKind(final String apiVersion,
                                    final String kind, final
                                    Class<? extends Resource> clazz) {
        mapping.registerKind(apiVersion, kind, clazz);
    }

    static class Mapping {

        private final Map<ResourceType, Class<? extends Resource>> mappings = new ConcurrentHashMap<>();

        public Class<? extends Resource> getForKey(final ResourceType type) {
            if (type == null) {
                return null;
            }

            return mappings.get(type);
        }

        public void registerKind(final Class<? extends Resource> clazz) {
            var apiVersion = Resource.getApiVersion(clazz);
            var kind = Resource.getKind(clazz);
            registerKind(apiVersion, kind, clazz);
        }

        public void registerKind(final String apiVersion,
                                 final String kind,
                                 final Class<? extends Resource> clazz) {
            ResourceType type = ResourceType.of(kind, apiVersion);
            LOG.info("Register class {} for group='{}' apiVersion='{}', kind='{}'",
                clazz.getSimpleName(),
                type.group(),
                type.apiVersion(),
                type.kind()
            );
            mappings.put(type, clazz);
        }
    }
}
