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

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaAclCollector;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaAclController;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaBrokerCollector;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaQuotaCollector;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaQuotaController;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaTableCollector;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaTableController;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaTopicCollector;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaTopicController;
import io.streamthoughts.jikkou.kafka.health.KafkaBrokerHealthIndicator;
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

public class KafkaExtensionProvider implements ExtensionProvider {

    /** {@inheritDoc} **/
    @Override
    public String getExtensionName() {
        return "kafka";
    }

    /** {@inheritDoc} **/
    @Override
    public void registerExtensions(@NotNull ExtensionFactory factory,
                                   @NotNull Configuration configuration) {

        // controllers
        factory.register(KafkaBrokerHealthIndicator.class, KafkaBrokerHealthIndicator::new);
        factory.register(AdminClientKafkaAclController.class, AdminClientKafkaAclController::new);
        factory.register(AdminClientKafkaTopicController.class, AdminClientKafkaTopicController::new);
        factory.register(AdminClientKafkaQuotaController.class, AdminClientKafkaQuotaController::new);

        // collectors
        factory.register(AdminClientKafkaBrokerCollector.class, AdminClientKafkaBrokerCollector::new);
        factory.register(AdminClientKafkaQuotaCollector.class, AdminClientKafkaQuotaCollector::new);
        factory.register(AdminClientKafkaTopicCollector.class, AdminClientKafkaTopicCollector::new);
        factory.register(AdminClientKafkaAclCollector.class, AdminClientKafkaAclCollector::new);
        factory.register(AdminClientKafkaTableCollector.class, AdminClientKafkaTableCollector::new);
        factory.register(AdminClientKafkaTableController.class, AdminClientKafkaTableController::new);

        // transformations
        factory.register(KafkaPrincipalAuthorizationTransformation.class, KafkaPrincipalAuthorizationTransformation::new);
        factory.register(KafkaTopicMaxRetentionMsTransformation.class, KafkaTopicMaxRetentionMsTransformation::new);
        factory.register(KafkaTopicMaxReplicasTransformation.class, KafkaTopicMaxReplicasTransformation::new);
        factory.register(KafkaTopicMaxNumPartitionsTransformation.class, KafkaTopicMaxNumPartitionsTransformation::new);
        factory.register(KafkaTopicMinRetentionMsTransformation.class, KafkaTopicMinRetentionMsTransformation::new);
        factory.register(KafkaTopicMinReplicasTransformation.class, KafkaTopicMinReplicasTransformation::new);
        factory.register(KafkaTopicMinInSyncReplicasTransformation.class, KafkaTopicMinInSyncReplicasTransformation::new);

        // validations
        factory.register(ClientQuotaValidation.class, ClientQuotaValidation::new);
        factory.register(NoDuplicatePrincipalRoleValidation.class, NoDuplicatePrincipalRoleValidation::new);
        factory.register(NoDuplicateTopicsAllowedValidation.class, NoDuplicateTopicsAllowedValidation::new);
        factory.register(NoDuplicatePrincipalAllowedValidation.class, NoDuplicatePrincipalAllowedValidation::new);
        factory.register(TopicConfigKeysValidation.class, TopicConfigKeysValidation::new);
        factory.register(TopicMinReplicationFactorValidation.class, TopicMinReplicationFactorValidation::new);
        factory.register(TopicMaxReplicationFactorValidation.class, TopicMaxReplicationFactorValidation::new);
        factory.register(TopicMinNumPartitionsValidation.class, TopicMinNumPartitionsValidation::new);
        factory.register(TopicNameRegexValidation.class, TopicNameRegexValidation::new);
        factory.register(TopicNamePrefixValidation.class, TopicNamePrefixValidation::new);
        factory.register(TopicNameSuffixValidation.class, TopicNameSuffixValidation::new);

        // reporters
        factory.register(KafkaChangeReporter.class, KafkaChangeReporter::new);
    }
}
