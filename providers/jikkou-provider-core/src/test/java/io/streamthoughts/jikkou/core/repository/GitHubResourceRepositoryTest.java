/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.repository;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GitHubResourceRepositoryTest {

    @Test
    void shouldLoadResourceFromPublicGithubRepository() {
        // Given
        GitHubResourceRepository repository = new GitHubResourceRepository();
        repository.init(new ExtensionContext() {
            @Override
            public String name() {
                return null;
            }

            @Override
            public Configuration configuration() {
                return Configuration.of(
                    GitHubResourceRepository.Config.REPOSITORY_CONFIG.key(), "streamthoughts/jikkou",
                    GitHubResourceRepository.Config.PATHS_CONFIG.key(), List.of("examples/topic")
                );
            }
            @Override
            public ExtensionContext contextForExtension(Class<? extends Extension> extension) {
                return null;
            }

            @Override
            public <T extends ExtensionProvider> T provider() {
                return null;
            }
        });
        // When
        List<? extends HasMetadata> resources = repository.all();

        // Then
        Assertions.assertNotNull(resources);
        Assertions.assertTrue(resources.size()> 2);
    }

    @Test
    void shouldLoadResourceFromPublicGithubRepositoryGivenPattern() {
        // Given
        GitHubResourceRepository repository = new GitHubResourceRepository();
        repository.init(new ExtensionContext() {
            @Override
            public String name() {
                return null;
            }

            @Override
            public Configuration configuration() {
                return Configuration.of(
                    GitHubResourceRepository.Config.REPOSITORY_CONFIG.key(), "streamthoughts/jikkou",
                    GitHubResourceRepository.Config.PATHS_CONFIG.key(), List.of("examples/topic"),
                    GitHubResourceRepository.Config.FILE_PATTERN_CONFIG.key(), "glob:**/kafka-topics.yaml"
                );
            }
            @Override
            public ExtensionContext contextForExtension(Class<? extends Extension> extension) {
                return null;
            }

            @Override
            public <T extends ExtensionProvider> T provider() {
                return null;
            }
        });
        // When
        List<? extends HasMetadata> resources = repository.all();

        // Then
        Assertions.assertNotNull(resources);
        Assertions.assertEquals(2, resources.size());
    }
}