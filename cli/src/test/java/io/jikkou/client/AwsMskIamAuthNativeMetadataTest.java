/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AwsMskIamAuthNativeMetadataTest {

    private static final String METADATA_RESOURCE =
            "META-INF/native-image/software.amazon.msk/aws-msk-iam-auth/reachability-metadata.json";

    private static List<String> reflectionTypes;

    @BeforeAll
    static void loadMetadata() throws IOException {
        try (InputStream is = AwsMskIamAuthNativeMetadataTest.class
                .getClassLoader()
                .getResourceAsStream(METADATA_RESOURCE)) {
            Assertions.assertNotNull(is, "Missing resource: " + METADATA_RESOURCE);
            JsonNode root = new ObjectMapper().readTree(is);
            reflectionTypes = new ArrayList<>();
            root.get("reflection").forEach(node -> reflectionTypes.add(node.get("type").asText()));
        }
    }

    @Test
    void shouldResolveAllRegisteredReflectionTypes_whenLoadedFromClasspath() {
        // Guards against stale entries (e.g. classes renamed or removed after an AWS SDK upgrade).
        for (String type : reflectionTypes) {
            Assertions.assertDoesNotThrow(
                    () -> Class.forName(type, false, getClass().getClassLoader()),
                    "Class registered for reflection is not on the classpath: " + type);
        }
    }

    @Test
    void shouldRegisterProfileCredentialsProviderFactories_whenUsingAwsProfiles() {
        // The AWS SDK loads these factories via Class.forName to resolve SSO and assume-role profiles.
        Assertions.assertTrue(reflectionTypes.contains(
                "software.amazon.awssdk.services.sso.auth.SsoProfileCredentialsProviderFactory"));
        Assertions.assertTrue(reflectionTypes.contains(
                "software.amazon.awssdk.services.sts.internal.StsProfileCredentialsProviderFactory"));
    }
}
