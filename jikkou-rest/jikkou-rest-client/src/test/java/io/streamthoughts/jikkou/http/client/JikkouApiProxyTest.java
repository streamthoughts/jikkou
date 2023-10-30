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
package io.streamthoughts.jikkou.http.client;

import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.extension.ExtensionFactory;
import io.streamthoughts.jikkou.core.models.ApiExtension;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiGroup;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
import io.streamthoughts.jikkou.core.models.ApiGroupVersion;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicator;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicatorList;
import io.streamthoughts.jikkou.core.models.ApiResource;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.ApiResourceVerbOptionList;
import io.streamthoughts.jikkou.core.models.ApiResourceVerbOptionSpec;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.Verb;
import io.streamthoughts.jikkou.http.client.exception.UnsupportedApiResourceException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JikkouApiProxyTest {

    static MockWebServer SERVER;

    static JikkouApiProxy API;

    @BeforeAll
    public static void beforeAll() throws IOException {
        SERVER = new MockWebServer();
        SERVER.start();
        ApiClient client = ApiClientBuilder.builder()
                .withBasePath(SERVER.url("/").toString())
                .withDebugging(true)
                .build();

        ExtensionFactory factory = new DefaultExtensionFactory(
                new DefaultExtensionRegistry(
                        new DefaultExtensionDescriptorFactory(),
                        new ClassExtensionAliasesGenerator()
                )
        );
        API = new JikkouApiProxy(factory, new DefaultJikkouApiClient(client));
    }

    @Test
    void shouldThrowForEmptyApiResourceList() {
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                          "kind": "ApiResourceList",
                          "apiVersion": "v1",
                          "groupVersion": "test.jikkou.io/v1",
                          "resources": []
                            }
                          ]
                        }
                        """
                ));
        UnsupportedApiResourceException exception = Assertions.assertThrows(UnsupportedApiResourceException.class, () ->
                API.getResources(new ResourceType("Test", "test.jikkou.io/v1", "v1"))
        );
        Assertions.assertNotNull(exception.getLocalizedMessage());
    }

    @Test
    void shouldGetApiGroupList() {
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                          "kind": "ApiGroupList",
                          "apiVersion": "v1",
                          "groups": [
                            {
                              "name": "test.jikkou.io",
                              "versions": [
                                {
                                  "groupVersion": "test.jikkou.io/v1",
                                  "version": "v1",
                                  "metadata": {
                                    "_links": {
                                      "self": {
                                        "href": "http://localhost:8080/apis/test.jikkou.io/v1",
                                        "templated": false
                                      }
                                    }
                                  }
                                }
                              ]
                            }
                          ]
                        }
                        """
                ));
        ApiGroupList result = API.listApiGroups();
        Assertions.assertEquals(getApiGroupList(), result);
    }

    @Test
    void shouldGetApiResourceList() {
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                          "kind": "ApiResourceList",
                          "apiVersion": "v1",
                          "groupVersion": "test.jikkou.io/v1",
                          "resources": [
                            {
                              "name": "tests",
                              "kind": "Test",
                              "singularName": "test",
                              "description": "The description of the resource",
                              "verbs": [
                                "apply",
                                "create",
                                "delete",
                                "list",
                                "update"
                              ],
                              "metadata": {
                                "_links": {
                                  "list": {
                                    "href": "http://localhost:8080/apis/test.jikkou.io/v1/test",
                                    "templated": false
                                  }
                                }
                              },
                              "verbsOptions": [
                                {
                                  "verb": "list",
                                  "options": [
                                    {
                                      "name": "value",
                                      "description": "The description of the option",
                                      "type": "string",
                                      "required": true
                                    }
                                  ]
                                }
                              ]
                            }
                          ]
                        }
                        """
                ));
        ApiResourceList result = API.listApiResources("test.jikkou.io", "v1");
        Assertions.assertEquals(getApiResourceList(), result);
    }

    @Test
    void shouldGetApiExtensionList() {
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                          "kind": "ApiExtensionList",
                          "apiVersion": "core.jikkou.io/v1",
                          "extensions": [
                            {
                              "name": "TestExtension",
                              "category": "Test",
                              "source": "<internal>",
                              "description": "Test extension"
                            }
                          ]
                        }
                        """
                ));
        ApiExtensionList result = API.getApiExtensions();
        ApiExtensionList expected = new ApiExtensionList(
                List.of(
                        new ApiExtension(
                                "TestExtension",
                                "Test",
                                "<internal>",
                                "Test extension"
                        )
                )
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void shouldGetApiHealthIndicatorList() {
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                          "kind": "ApiHealthIndicatorList",
                          "apiVersion": "core.jikkou.io/v1",
                          "indicators": [
                            {
                              "name": "test",
                              "description": "Test Health Indicator",
                              "metadata": {
                                "_links": {
                                  "self": {
                                    "href": "http://localhost:28082/apis/core.jikkou.io/v1/healths/test",
                                    "templated": false
                                  }
                                }
                              }
                            }
                          ]
                        }
                        """
                ));
        ApiHealthIndicatorList result = API.getApiHealthIndicators();
        ApiHealthIndicatorList expected = new ApiHealthIndicatorList(
                List.of(
                        new ApiHealthIndicator(
                                "test",
                                "Test Health Indicator",
                                Map.of(
                                        "_links", Map.of(
                                                "self", Map.of(
                                                        "href", "http://localhost:28082/apis/core.jikkou.io/v1/healths/test",
                                                        "templated", false
                                                )
                                        )
                                )
                        )
                )
        );
        Assertions.assertEquals(expected, result);
    }


    @NotNull
    private static ApiResourceList getApiResourceList() {
        return new ApiResourceList(
                "test.jikkou.io/v1",
                List.of(new ApiResource(
                        "tests",
                        "Test",
                        "test",
                        Collections.emptySet(),
                        "The description of the resource",
                        Set.of(
                                "apply",
                                "create",
                                "delete",
                                "list",
                                "update"
                        ),
                        List.of(new ApiResourceVerbOptionList(
                                Verb.LIST,
                                List.of(new ApiResourceVerbOptionSpec(
                                        "value",
                                        "The description of the option",
                                        String.class,
                                        null,
                                        true
                                ))
                        )),
                        Map.of(
                                "_links", Map.of(
                                        "list", Map.of(
                                                "href", "http://localhost:8080/apis/test.jikkou.io/v1/test",
                                                "templated", false
                                        )
                                )
                        )
                ))
        );
    }

    @NotNull
    private static ApiGroupList getApiGroupList() {
        ApiGroupVersion apiGroupVersion = new ApiGroupVersion(
                "test.jikkou.io/v1",
                "v1",
                Map.of(
                        "_links", Map.of(
                                "self", Map.of(
                                        "href", "http://localhost:8080/apis/test.jikkou.io/v1",
                                        "templated", false
                                )
                        )
                )
        );
        return new ApiGroupList(List.of(new ApiGroup("test.jikkou.io", Set.of(apiGroupVersion))));
    }

}