/*
 * Copyright 2020 The original authors
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
package io.streamthoughts.jikkou.kafka.change.handlers.acls.builder;

import io.streamthoughts.jikkou.CoreAnnotations;
import io.streamthoughts.jikkou.kafka.change.handlers.acls.KafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.internals.KafkaUtils;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAcl;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopicMatchingAclRulesBuilder extends AbstractKafkaAclBindingBuilder implements KafkaAclBindingBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(TopicMatchingAclRulesBuilder.class);

    private AdminClient client;
    private CompletableFuture<Collection<TopicListing>> listTopics;

    /**
     * Creates a new {@link TopicMatchingAclRulesBuilder}.
     */
    TopicMatchingAclRulesBuilder() {}

    /**
     * Creates a new {@link TopicMatchingAclRulesBuilder} instance.
     *
     * @param client the kafka admin client to be used.
     */
    public TopicMatchingAclRulesBuilder(final AdminClient client) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<KafkaAclBinding> toKafkaAclBindings(V1KafkaPrincipalAuthorization resource) {
        List<V1KafkaPrincipalAcl> acls = getAcceptedAclsFromResource(resource);
        if (acls.isEmpty()) return Collections.emptyList();

        String principal = resource.getMetadata().getName();
        return getListTopics()
                .thenApply(topics -> topics.stream()
                        .flatMap(topic -> {
                            return buildAclBindings(principal,
                                    filterPermissionMatchingTopic(acls, topic),
                                    topic.name(),
                                    PatternType.LITERAL,
                                    ResourceType.TOPIC,
                                    CoreAnnotations.isAnnotatedWithDelete(resource)
                            ).stream();
                        })
                        .toList()
                )
                .join();
    }

    private List<V1KafkaPrincipalAcl> getAcceptedAclsFromResource(V1KafkaPrincipalAuthorization resource) {
         return resource.getSpec().getAcls()
                .stream()
                .filter(it -> it.getResource().getType() == ResourceType.TOPIC)
                .filter(it -> it.getResource().getPatternType() == PatternType.MATCH)
                .toList();
    }

    /**
     * Only for testing purpose.
     *
     * @param topics the list of topics.
     */
    void setListTopics(final CompletableFuture<Collection<TopicListing>> topics) {
        this.listTopics = topics;
    }

    private CompletableFuture<Collection<TopicListing>> getListTopics() {
        if (listTopics == null) {
            listTopics = KafkaUtils.listTopics(client);
        }
        return listTopics;
    }

    /**
     * Keeps only {@link V1KafkaPrincipalAcl} matching the topic.
     *
     * @param items the list of ACLs to be filtered
     * @param topic the topic to be used.
     * @return a new list of {@link V1KafkaPrincipalAcl} instances.
     */
    private Collection<V1KafkaPrincipalAcl> filterPermissionMatchingTopic(final Collection<V1KafkaPrincipalAcl> items,
                                                                          final TopicListing topic) {
        return items.stream().filter(acl -> {
            String regex = acl.getResource().getPattern();
            Matcher matcher = Pattern.compile(regex).matcher(topic.name());
            boolean matches = matcher.matches();
            LOG.info("Matching topic '{}' with resource pattern '{}': {}", topic.name(), regex, matches);
            return matches;
        }).collect(Collectors.toSet());
    }
}
