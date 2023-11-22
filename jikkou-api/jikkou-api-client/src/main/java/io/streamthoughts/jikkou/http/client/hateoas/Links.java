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
package io.streamthoughts.jikkou.http.client.hateoas;

import io.micronaut.http.hateoas.Link;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public final class Links {

    /**
     * The _links field.
     */
    public static final String LINKS = "_links";

    private final Map<String, Link> links;

    /**
     * Creates a new {@link Links} instance.
     *
     * @param links The links
     */
    public Links(@NotNull Map<String, Link> links) {
        this.links = Objects.requireNonNull(links, "links cannot be null");
    }

    /**
     * Gets the Link for the specified key.
     *
     * @param key the key for selecting Link.
     * @return the optional Link.
     */
    public Optional<Link> findLinkByKey(String key) {
        return Optional.ofNullable(links.get(key));
    }

    public static Links of(Map<String, ?> resource) {
        if (!resource.containsKey(LINKS)) {
            return new Links(Collections.emptyMap());
        }

        Map<String, Link> links = new HashMap<>();
        Object o = resource.get(LINKS);
        if (o instanceof Map linksObjects) {
            for (Map.Entry<String, Object> linkObjects : ((Map<String, Object>) linksObjects).entrySet()) {
                String key = linkObjects.getKey();
                Object value = linkObjects.getValue();
                if (value instanceof Map object) {
                    String href = Optional.ofNullable(object.get("href")).map(Object::toString)
                            .orElseThrow(() -> new IllegalArgumentException());
                    Link link = Link.build(href)
                            .name(extractStringValOrGetNull(object, "name"))
                            .title(extractStringValOrGetNull(object, "title"))
                            .deprecation(extractStringValOrGetNull(object, "deprecation"))
                            .profile(extractStringValOrGetNull(object, "profile"))
                            .templated(Optional.ofNullable(extractStringValOrGetNull(object, "title")).map(Boolean::parseBoolean).orElse(false))
                            .build();
                    links.put(key, link);
                }
            }
        }
        return new Links(links);
    }

    @Nullable
    private static String extractStringValOrGetNull(Map object, String key) {
        return Optional.ofNullable(object.get(key)).map(Object::toString).orElse(null);
    }
}
