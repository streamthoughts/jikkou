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
package io.streamthoughts.jikkou.core.io;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.Resource;
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

    record TypeKey(String kind, String group, String version) {
    }

    private static final String KIND = "kind";
    private static final String API_VERSION = "apiVersion";

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

        TypeKey key = getKey(node);
        if (key != null) {
            LOG.debug("Looking for specialized Resource for group={} apiVersion={}, kind={}",
                    key.group(),
                    key.version(),
                    key.kind()
            );
            resourceType = mapping.getForKey(key);

        } else if (!resolvers.isEmpty()) {
            LOG.debug("Looking for specialized resolver for untyped Resource");
            for (ResourceTypeResolver r : resolvers) {
                resourceType = r.resolvesType(node);
                if (resourceType != null) {
                    break;
                }
            }
        }

        if (resourceType == null) {
            LOG.debug("No specific resource found");
            return jp.getCodec().treeToValue(node, GenericResource.class);
        } else if (Resource.class.isAssignableFrom(resourceType)) {
            LOG.debug("Read specific resource for apiVersion={}, kind={}",
                    Resource.getApiVersion(resourceType),
                    Resource.getKind(resourceType)
            );
            return jp.getCodec().treeToValue(node, resourceType);
        }

        LOG.warn("Failed get resource type from JsonNode");
        return null;
    }

    private static TypeKey getKey(JsonNode node) {
        JsonNode apiVersion = node.get(API_VERSION);
        JsonNode kind = node.get(KIND);

        return mapping.createKey(
                apiVersion != null ? apiVersion.textValue() : null,
                kind != null ? kind.textValue() : null);
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

        private final Map<TypeKey, Class<? extends Resource>> mappings = new ConcurrentHashMap<>();

        public Class<? extends Resource> getForKey(TypeKey key) {
            if (key == null) {
                return null;
            }

            return mappings.get(key);
        }

        public void registerKind(final Class<? extends Resource> clazz) {
            var apiVersion = Resource.getApiVersion(clazz);
            var kind = Resource.getKind(clazz);
            registerKind(apiVersion, kind, clazz);
        }

        public void registerKind(final String apiVersion,
                                 final String kind,
                                 final Class<? extends Resource> clazz) {
            TypeKey key = createKey(apiVersion, kind);
            LOG.info("Register class {} for group='{}' apiVersion='{}', kind='{}'",
                    clazz.getSimpleName(),
                    key.group(),
                    key.version(),
                    key.kind()
            );
            mappings.put(key, clazz);
        }

        TypeKey createKey(String apiVersion, String kind) {
            if (kind == null) {
                return null;
            } else if (apiVersion == null) {
                return new TypeKey(kind, null, null);
            } else {
                String[] versionParts = new String[]{null, apiVersion};
                if (apiVersion.contains("/")) {
                    versionParts = apiVersion.split("/", 2);
                }
                return new TypeKey(kind, versionParts[0], versionParts[1]);
            }
        }
    }
}
