/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.reconciler;

import io.jikkou.core.CoreExtensionProvider;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.ReconciliationMode;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.models.ApiChangeResultList;
import io.jikkou.core.models.Configs;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.ResourceList;
import io.jikkou.kafka.KafkaExtensionProvider;
import io.jikkou.kafka.collections.V1KafkaShareGroupList;
import io.jikkou.kafka.models.V1KafkaConsumerGroup;
import io.jikkou.kafka.models.V1KafkaConsumerGroupSpec;
import io.jikkou.kafka.models.V1KafkaShareGroup;
import io.jikkou.kafka.models.V1KafkaShareGroupSpec;
import io.jikkou.kafka.reconciler.service.KafkaAdminService;
import io.jikkou.runtime.JikkouContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.GroupState;
import org.apache.kafka.common.config.ConfigResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration tests for share group support against a real broker.
 * <p>
 * Share groups (KIP-932) require the broker to be started with {@code group.share.enable=true}
 * and the {@code share} rebalance protocol enabled. This test starts its own broker with those
 * settings, and is gated behind the {@code it.kafka.shareGroups} system property so the default
 * CI build (which uses a broker without share-group support) is not broken.
 * <p>
 * Run with: {@code ./mvnw failsafe:integration-test -pl providers/jikkou-provider-kafka
 * -Dit.test=AdminClientShareGroupIT -Dit.kafka.shareGroups=true}
 */
@Testcontainers
@Tag("integration")
@EnabledIfSystemProperty(named = "it.kafka.shareGroups", matches = "true")
public class AdminClientShareGroupIT {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientShareGroupIT.class);

    private static final String APACHE_KAFKA_VERSION = "4.3.0";

    @Container
    final KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("apache/kafka").withTag(APACHE_KAFKA_VERSION))
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
        .withEnv("KAFKA_GROUP_COORDINATOR_REBALANCE_PROTOCOLS", "classic,consumer,share")
        .withEnv("KAFKA_GROUP_SHARE_ENABLE", "true")
        .withEnv("KAFKA_SHARE_COORDINATOR_STATE_TOPIC_REPLICATION_FACTOR", "1")
        .withEnv("KAFKA_SHARE_COORDINATOR_STATE_TOPIC_MIN_ISR", "1")
        .withLogConsumer(new Slf4jLogConsumer(LOG));

    private JikkouApi api;

    @BeforeEach
    public void initApi() {
        Map<String, Object> clientConfig = Map.of(
            CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        api = JikkouContext.defaultContext()
            .newApiBuilder()
            .register(new CoreExtensionProvider())
            .register(new KafkaExtensionProvider(), Configuration.of("client", clientConfig))
            .build()
            .enableBuiltInAnnotations(false);
    }

    private AdminClient adminClient() {
        return AdminClient.create(Map.of(
            CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()));
    }

    private String readGroupConfig(String groupId, String key) throws Exception {
        try (AdminClient client = adminClient()) {
            ConfigResource resource = new ConfigResource(ConfigResource.Type.GROUP, groupId);
            Config config = client.describeConfigs(List.of(resource)).all().get().get(resource);
            return config.get(key) == null ? null : config.get(key).value();
        }
    }

    @Test
    public void shouldReadDynamicGroupConfigsFilteringDefaults() throws Exception {
        String groupId = "it-share-group-configs";
        try (AdminClient client = adminClient()) {
            // GIVEN a dynamic group config set on the GROUP resource
            ConfigResource resource = new ConfigResource(ConfigResource.Type.GROUP, groupId);
            client.incrementalAlterConfigs(Map.of(resource, List.of(
                new AlterConfigOp(new ConfigEntry("share.auto.offset.reset", "earliest"),
                    AlterConfigOp.OpType.SET)))).all().get();

            // WHEN reading the configs through our service
            KafkaAdminService service = new KafkaAdminService(client);
            Map<String, Configs> configs = service.describeGroupConfigs(List.of(groupId));

            // THEN only the user-set DYNAMIC_GROUP_CONFIG entry is returned
            Configs groupConfigs = configs.get(groupId);
            Assertions.assertNotNull(groupConfigs, "expected configs for group " + groupId);
            Assertions.assertEquals("earliest",
                String.valueOf(groupConfigs.get("share.auto.offset.reset").value()));
            // Broker defaults must be filtered out (only the single dynamic entry remains).
            Assertions.assertEquals(1, groupConfigs.size(),
                "expected only DYNAMIC_GROUP_CONFIG entries, got: " + groupConfigs);
        }
    }

    @Test
    public void shouldListShareGroupsWithoutError() {
        try (AdminClient client = adminClient()) {
            KafkaAdminService service = new KafkaAdminService(client);
            // No active share consumers, so the listing is expected to be empty but must not error.
            V1KafkaShareGroupList list = service.listShareGroups(Set.<GroupState>of(), false);
            Assertions.assertNotNull(list);
            Assertions.assertNotNull(list.getItems());
        }
    }

    @Test
    public void shouldReconcileShareGroupConfigsThroughApi() throws Exception {
        // Regression for: "Cannot find controller for resource type: kind='KafkaShareGroupChange'".
        String groupId = "it-reconcile-share-group";
        V1KafkaShareGroup group = V1KafkaShareGroup.builder()
            .withApiVersion("kafka.jikkou.io/v1")
            .withKind("KafkaShareGroup")
            .withMetadata(ObjectMeta.builder().withName(groupId).build())
            .withSpec(V1KafkaShareGroupSpec.builder()
                .withConfigs(Configs.of("share.auto.offset.reset", "earliest"))
                .build())
            .build();

        ReconciliationContext context = ReconciliationContext.builder().dryRun(false).build();

        ApiChangeResultList first = api.reconcile(
            ResourceList.of(group), ReconciliationMode.FULL, context);
        Assertions.assertFalse(first.results().isEmpty(), "expected a change result");
        Assertions.assertEquals("earliest", readGroupConfig(groupId, "share.auto.offset.reset"));

        // Idempotent: re-reconciling reports no change (drift detection via describeGroupConfigs).
        ApiChangeResultList second = api.reconcile(
            ResourceList.of(group), ReconciliationMode.FULL, context);
        Assertions.assertTrue(second.results().stream().noneMatch(r -> r.isChanged()),
            "expected no change on second reconciliation");
    }

    @Test
    public void shouldReconcileConsumerGroupConfigsForNonExistentGroup() throws Exception {
        // Regression for GroupIdNotFoundException when planning configs for a group that does
        // not exist yet on a Kafka 4.x broker.
        String groupId = "it-reconcile-consumer-group";
        V1KafkaConsumerGroup group = V1KafkaConsumerGroup.builder()
            .withApiVersion("kafka.jikkou.io/v1")
            .withKind("KafkaConsumerGroup")
            .withMetadata(ObjectMeta.builder().withName(groupId).build())
            .withSpec(V1KafkaConsumerGroupSpec.builder()
                // Must be within [group.consumer.min/max.session.timeout.ms] (default 45000..60000).
                .withConfigs(Configs.of("consumer.session.timeout.ms", "50000"))
                .build())
            .build();

        ReconciliationContext context = ReconciliationContext.builder().dryRun(false).build();

        ApiChangeResultList result = api.reconcile(
            ResourceList.of(group), ReconciliationMode.FULL, context);
        Assertions.assertFalse(result.results().isEmpty(), "expected a change result");
        Assertions.assertEquals("50000", readGroupConfig(groupId, "consumer.session.timeout.ms"));
    }
}
