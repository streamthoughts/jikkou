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
package io.streamthoughts.jikkou.client.beans;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AccessorsStyle;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.annotation.Bindable;
import jakarta.validation.constraints.NotBlank;
import java.util.Optional;

/**
 * Immutable configuration for property 'jikkou.proxy'.
 */
@ConfigurationProperties("proxy")
@AccessorsStyle(readPrefixes = "", writePrefixes = "")
@Requires(property = "proxy.enabled", value = "true")
public interface ProxyConfiguration {
   @NotBlank
   String url();

   @Bindable(defaultValue = "false")
   boolean enabled();

   @Bindable(defaultValue = "false")
   boolean debugging();

   Optional<Integer> connectTimeout();

   Optional<Integer> readTimeout();

   Optional<Integer> writeTimeout();

   @Bindable(defaultValue = "10000")
   Integer defaultTimeout();

   @Nullable
   Security security();

   @AccessorsStyle(readPrefixes = "", writePrefixes = "")
   @ConfigurationProperties("security")
   interface Security {

      Optional<String> accessToken();

      BasicAuth basicAuth();
   }

   @AccessorsStyle(readPrefixes = "", writePrefixes = "")
   @ConfigurationProperties("security.basic-auth")
   interface BasicAuth {

      Optional<String> username();

      Optional<String> password();
   }
}
