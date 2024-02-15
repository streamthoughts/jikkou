/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.hateoas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LinksTest {

    @Test
    void shouldCreateLinksFromMap() throws JsonProcessingException {
        var json = """ 
                 {
                    "_links": {
                       "self": { "href": "/orders" },
                       "next": { "href": "/orders?page=2" },
                       "find": { "href": "/orders{?id}", "templated": true }
                     }
                }
                """;
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(json, Map.class);
        Links links = Links.of(map);
        Assertions.assertNotNull(links);
        Assertions.assertTrue(links.findLinkByKey("self").isPresent());
        Assertions.assertTrue(links.findLinkByKey("next").isPresent());
        Assertions.assertTrue(links.findLinkByKey("find").isPresent());
    }
}