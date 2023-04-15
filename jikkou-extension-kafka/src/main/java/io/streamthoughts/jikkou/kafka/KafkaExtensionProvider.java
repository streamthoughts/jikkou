/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka;

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaAclCollector;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaAclController;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaBrokerCollector;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaQuotaCollector;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaQuotaController;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaTopicCollector;
import io.streamthoughts.jikkou.kafka.control.AdminClientKafkaTopicController;
import io.streamthoughts.jikkou.kafka.health.KafkaBrokerHealthIndicator;
import io.streamthoughts.jikkou.kafka.transformations.PrincipalAuthorizationTransformation;
import io.streamthoughts.jikkou.kafka.transformations.TopicConfigMapsTransformation;
import io.streamthoughts.jikkou.kafka.validations.ClientQuotaValidation;
import io.streamthoughts.jikkou.kafka.validations.NoDuplicatePrincipalAllowedValidation;
import io.streamthoughts.jikkou.kafka.validations.NoDuplicatePrincipalRoleValidation;
import io.streamthoughts.jikkou.kafka.validations.NoDuplicateTopicsAllowedValidation;
import io.streamthoughts.jikkou.kafka.validations.TopicConfigKeysValidation;
import io.streamthoughts.jikkou.kafka.validations.TopicMaxReplicationFactorValidation;
import io.streamthoughts.jikkou.kafka.validations.TopicMinNumPartitionsValidation;
import io.streamthoughts.jikkou.kafka.validations.TopicMinReplicationFactorValidation;
import io.streamthoughts.jikkou.kafka.validations.TopicNamePrefixValidation;
import io.streamthoughts.jikkou.kafka.validations.TopicNameRegexValidation;
import io.streamthoughts.jikkou.kafka.validations.TopicNameSuffixValidation;
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
        factory.register(KafkaBrokerHealthIndicator.class);
        factory.register(AdminClientKafkaAclController.class);
        factory.register(AdminClientKafkaTopicController.class);
        factory.register(AdminClientKafkaQuotaController.class);

        // descriptors
        factory.register(AdminClientKafkaBrokerCollector.class);
        factory.register(AdminClientKafkaQuotaCollector.class);
        factory.register(AdminClientKafkaTopicCollector.class);
        factory.register(AdminClientKafkaAclCollector.class);

        // transformations
        factory.register(TopicConfigMapsTransformation.class);
        factory.register(PrincipalAuthorizationTransformation.class);

        // validations
        factory.register(ClientQuotaValidation.class);
        factory.register(NoDuplicatePrincipalRoleValidation.class);
        factory.register(NoDuplicateTopicsAllowedValidation.class);
        factory.register(NoDuplicatePrincipalAllowedValidation.class);
        factory.register(TopicConfigKeysValidation.class);
        factory.register(TopicMinReplicationFactorValidation.class);
        factory.register(TopicMaxReplicationFactorValidation.class);
        factory.register(TopicMinNumPartitionsValidation.class);
        factory.register(TopicNameRegexValidation.class);
        factory.register(TopicNamePrefixValidation.class);
        factory.register(TopicNameSuffixValidation.class);
    }
}
