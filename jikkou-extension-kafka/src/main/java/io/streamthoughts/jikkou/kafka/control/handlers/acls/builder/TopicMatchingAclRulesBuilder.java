/*
 * Copyright 2020 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.control.handlers.acls.builder;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.kafka.control.handlers.acls.KafkaAclBindingBuilder;
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

public class TopicMatchingAclRulesBuilder extends AbstractKafkaAclBindingBuilder implements KafkaAclBindingBuilder {

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
        this.client = Objects.requireNonNull(client, "client cannot be null");;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<KafkaAclBinding> toKafkaAclBindings(V1KafkaPrincipalAuthorization resource) {
        return getListTopics()
                .thenApply(topics -> topics.stream()
                        .flatMap(topic -> createAclBindingsForMatchingTopics(resource, topic).stream())
                        .toList()
                )
                .join();
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

    private Collection<KafkaAclBinding> createAclBindingsForMatchingTopics(final V1KafkaPrincipalAuthorization resource,
                                                                           final TopicListing topic) {
        List<V1KafkaPrincipalAcl> permissions = resource.getSpec().getAcls();
        if (permissions == null) {
            return Collections.emptyList();
        }

        permissions = permissions
                .stream()
                .filter(it -> it.getResource().getType() == ResourceType.TOPIC)
                .filter(it -> it.getResource().getPatternType() == PatternType.MATCH)
                .toList();

        boolean annotatedWithDelete = JikkouMetadataAnnotations.isAnnotatedWithDelete(resource);

        String principal = resource.getMetadata().getName();
        return buildAclBindings(principal,
                annotatedWithDelete,
                filterPermissionMatchingTopic(permissions, topic),
                topic.name(),
                PatternType.LITERAL,
                ResourceType.TOPIC);
    }

    /**
     * Keeps only #@link AclResourcePermission matching the specified topic.
     *
     * @param acls  the acls to be filtered
     * @param topic the topic to be used.
     * @return a new list of {@link V1KafkaPrincipalAcl} instances.
     */
    private Collection<V1KafkaPrincipalAcl> filterPermissionMatchingTopic(final Collection<V1KafkaPrincipalAcl> acls,
                                                                          final TopicListing topic) {
        return acls.stream()
                .filter(acl -> {
                    String regex = acl.getResource().getPattern();
                    regex = regex.substring(1, regex.length() - 1);
                    Matcher matcher = Pattern.compile(regex).matcher(topic.name());
                    return matcher.matches();
                }).collect(Collectors.toSet());
    }
}
