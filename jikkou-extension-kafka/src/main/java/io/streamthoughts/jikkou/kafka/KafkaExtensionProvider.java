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
package io.streamthoughts.jikkou.kafka;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.kafka.health.KafkaBrokerHealthIndicator;
import io.streamthoughts.jikkou.kafka.reconcilier.AdminClientKafkaAclCollector;
import io.streamthoughts.jikkou.kafka.reconcilier.AdminClientKafkaAclController;
import io.streamthoughts.jikkou.kafka.reconcilier.AdminClientKafkaBrokerCollector;
import io.streamthoughts.jikkou.kafka.reconcilier.AdminClientKafkaQuotaCollector;
import io.streamthoughts.jikkou.kafka.reconcilier.AdminClientKafkaQuotaController;
import io.streamthoughts.jikkou.kafka.reconcilier.AdminClientKafkaTableCollector;
import io.streamthoughts.jikkou.kafka.reconcilier.AdminClientKafkaTableController;
import io.streamthoughts.jikkou.kafka.reconcilier.AdminClientKafkaTopicCollector;
import io.streamthoughts.jikkou.kafka.reconcilier.AdminClientKafkaTopicController;
import io.streamthoughts.jikkou.kafka.reporter.KafkaChangeReporter;
import io.streamthoughts.jikkou.kafka.transform.KafkaPrincipalAuthorizationTransformation;
import io.streamthoughts.jikkou.kafka.transform.KafkaTopicMaxNumPartitionsTransformation;
import io.streamthoughts.jikkou.kafka.transform.KafkaTopicMaxReplicasTransformation;
import io.streamthoughts.jikkou.kafka.transform.KafkaTopicMaxRetentionMsTransformation;
import io.streamthoughts.jikkou.kafka.transform.KafkaTopicMinInSyncReplicasTransformation;
import io.streamthoughts.jikkou.kafka.transform.KafkaTopicMinReplicasTransformation;
import io.streamthoughts.jikkou.kafka.transform.KafkaTopicMinRetentionMsTransformation;
import io.streamthoughts.jikkou.kafka.validation.ClientQuotaValidation;
import io.streamthoughts.jikkou.kafka.validation.NoDuplicatePrincipalAllowedValidation;
import io.streamthoughts.jikkou.kafka.validation.NoDuplicatePrincipalRoleValidation;
import io.streamthoughts.jikkou.kafka.validation.NoDuplicateTopicsAllowedValidation;
import io.streamthoughts.jikkou.kafka.validation.TopicConfigKeysValidation;
import io.streamthoughts.jikkou.kafka.validation.TopicMaxReplicationFactorValidation;
import io.streamthoughts.jikkou.kafka.validation.TopicMinNumPartitionsValidation;
import io.streamthoughts.jikkou.kafka.validation.TopicMinReplicationFactorValidation;
import io.streamthoughts.jikkou.kafka.validation.TopicNamePrefixValidation;
import io.streamthoughts.jikkou.kafka.validation.TopicNameRegexValidation;
import io.streamthoughts.jikkou.kafka.validation.TopicNameSuffixValidation;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import org.jetbrains.annotations.NotNull;

public final class KafkaExtensionProvider implements ExtensionProvider {

    /** {@inheritDoc} **/
    @Override
    public String getName() {
        return "kafka";
    }

    /** {@inheritDoc} **/
    @Override
    public void registerExtensions(@NotNull ExtensionRegistry registry,
                                   @NotNull Configuration configuration) {

        // controllers
        registry.register(KafkaBrokerHealthIndicator.class, KafkaBrokerHealthIndicator::new);
        registry.register(AdminClientKafkaAclController.class, AdminClientKafkaAclController::new);
        registry.register(AdminClientKafkaTopicController.class, AdminClientKafkaTopicController::new);
        registry.register(AdminClientKafkaQuotaController.class, AdminClientKafkaQuotaController::new);

        // collectors
        registry.register(AdminClientKafkaBrokerCollector.class, AdminClientKafkaBrokerCollector::new);
        registry.register(AdminClientKafkaQuotaCollector.class, AdminClientKafkaQuotaCollector::new);
        registry.register(AdminClientKafkaTopicCollector.class, AdminClientKafkaTopicCollector::new);
        registry.register(AdminClientKafkaAclCollector.class, AdminClientKafkaAclCollector::new);
        registry.register(AdminClientKafkaTableCollector.class, AdminClientKafkaTableCollector::new);
        registry.register(AdminClientKafkaTableController.class, AdminClientKafkaTableController::new);

        // transformations
        registry.register(KafkaPrincipalAuthorizationTransformation.class, KafkaPrincipalAuthorizationTransformation::new);
        registry.register(KafkaTopicMaxRetentionMsTransformation.class, KafkaTopicMaxRetentionMsTransformation::new);
        registry.register(KafkaTopicMaxReplicasTransformation.class, KafkaTopicMaxReplicasTransformation::new);
        registry.register(KafkaTopicMaxNumPartitionsTransformation.class, KafkaTopicMaxNumPartitionsTransformation::new);
        registry.register(KafkaTopicMinRetentionMsTransformation.class, KafkaTopicMinRetentionMsTransformation::new);
        registry.register(KafkaTopicMinReplicasTransformation.class, KafkaTopicMinReplicasTransformation::new);
        registry.register(KafkaTopicMinInSyncReplicasTransformation.class, KafkaTopicMinInSyncReplicasTransformation::new);

        // validations
        registry.register(ClientQuotaValidation.class, ClientQuotaValidation::new);
        registry.register(NoDuplicatePrincipalRoleValidation.class, NoDuplicatePrincipalRoleValidation::new);
        registry.register(NoDuplicateTopicsAllowedValidation.class, NoDuplicateTopicsAllowedValidation::new);
        registry.register(NoDuplicatePrincipalAllowedValidation.class, NoDuplicatePrincipalAllowedValidation::new);
        registry.register(TopicConfigKeysValidation.class, TopicConfigKeysValidation::new);
        registry.register(TopicMinReplicationFactorValidation.class, TopicMinReplicationFactorValidation::new);
        registry.register(TopicMaxReplicationFactorValidation.class, TopicMaxReplicationFactorValidation::new);
        registry.register(TopicMinNumPartitionsValidation.class, TopicMinNumPartitionsValidation::new);
        registry.register(TopicNameRegexValidation.class, TopicNameRegexValidation::new);
        registry.register(TopicNamePrefixValidation.class, TopicNamePrefixValidation::new);
        registry.register(TopicNameSuffixValidation.class, TopicNameSuffixValidation::new);

        // reporters
        registry.register(KafkaChangeReporter.class, KafkaChangeReporter::new);
    }
}
