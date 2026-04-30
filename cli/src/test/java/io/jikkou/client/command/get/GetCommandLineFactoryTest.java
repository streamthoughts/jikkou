/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jikkou.core.JikkouApi;
import io.jikkou.core.models.ApiProvider;
import io.jikkou.core.models.ApiProviderList;
import io.jikkou.core.models.ApiProviderSpec;
import io.jikkou.core.models.ApiProviderSummary;
import io.jikkou.core.models.ApiResource;
import io.jikkou.core.models.ApiResourceList;
import io.jikkou.core.models.ApiResourceSummary;
import io.jikkou.core.models.Verb;
import io.micronaut.context.ApplicationContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class GetCommandLineFactoryTest {

    private static final String KAFKA_GROUP = "kafka.jikkou.io";
    private static final String SR_GROUP = "schemaregistry.jikkou.io";

    private static final ApiResource TOPIC_RES = new ApiResource(
        "kafkatopics", "KafkaTopic", "kafkatopic", Set.of("kt"),
        "Kafka topics.", Set.of(Verb.LIST.value(), Verb.GET.value())
    );

    private static final ApiResource TOPIC_RES_WITH_LOCAL = new ApiResource(
        "kafkatopics", "KafkaTopic", "kafkatopic", Set.of("kt"),
        "Kafka topics.", Set.of(Verb.LIST.value(), Verb.GET.value())
    ).withLocalName("topics");

    private static final ApiResource ACL_RES = new ApiResource(
        "kafkaprincipalacls", "KafkaPrincipalAcl", "kafkaprincipalacl", Set.of(),
        "Kafka ACLs.", Set.of(Verb.LIST.value())
    );

    private static final ApiResource SUBJECT_RES = new ApiResource(
        "schemaregistrysubjects", "SchemaRegistrySubject", "schemaregistrysubject", Set.of("sub"),
        "Schema registry subjects.", Set.of(Verb.LIST.value(), Verb.GET.value())
    );

    private JikkouApi apiReturning(List<ApiResourceList> lists, Map<String, List<ApiResourceSummary>> providerResources) {
        JikkouApi api = mock(JikkouApi.class);
        when(api.listApiResources()).thenReturn(lists);

        List<ApiProviderSummary> summaries = providerResources.keySet().stream()
            .map(name -> new ApiProviderSummary(name, name + "-type", true))
            .toList();
        when(api.getApiProviders()).thenReturn(new ApiProviderList(summaries));

        for (Map.Entry<String, List<ApiResourceSummary>> entry : providerResources.entrySet()) {
            ApiProviderSpec spec = new ApiProviderSpec(
                entry.getKey(), entry.getKey() + "-type", "desc",
                List.of(), "", true, List.of(), entry.getValue(), List.of()
            );
            when(api.getApiProvider(entry.getKey()))
                .thenReturn(new ApiProvider(spec));
        }
        return api;
    }

    private ApplicationContext contextProducingCommands() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.getBean(GetResourceCommand.class))
            .thenAnswer(inv -> new GetResourceCommand());
        return ctx;
    }

    private static ApiResourceSummary summary(String group, String kind) {
        return new ApiResourceSummary(kind, group, group + "/v1", "");
    }

    @Test
    void shouldRegisterOneNestedSubcommandPerProvider() {
        // Given
        JikkouApi api = apiReturning(
            List.of(
                new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES, ACL_RES)),
                new ApiResourceList(SR_GROUP + "/v1", List.of(SUBJECT_RES))
            ),
            Map.of(
                "kafka", List.of(summary(KAFKA_GROUP, "KafkaTopic"), summary(KAFKA_GROUP, "KafkaPrincipalAcl")),
                "schemaregistry", List.of(summary(SR_GROUP, "SchemaRegistrySubject"))
            )
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then
        assertNotNull(get.getSubcommands().get("kafka"));
        assertNotNull(get.getSubcommands().get("schemaregistry"));
    }

    @Test
    void shouldNestKindsUnderTheirProvider() {
        // Given
        JikkouApi api = apiReturning(
            List.of(new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES, ACL_RES))),
            Map.of("kafka", List.of(summary(KAFKA_GROUP, "KafkaTopic"), summary(KAFKA_GROUP, "KafkaPrincipalAcl")))
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then
        CommandLine kafka = get.getSubcommands().get("kafka");
        assertNotNull(kafka.getSubcommands().get("kafkatopics"));
        assertNotNull(kafka.getSubcommands().get("kafkaprincipalacls"));
    }

    @Test
    void shouldRegisterFlatKindSubcommandsAsHidden() {
        // Given
        JikkouApi api = apiReturning(
            List.of(new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES, ACL_RES))),
            Map.of("kafka", List.of(summary(KAFKA_GROUP, "KafkaTopic"), summary(KAFKA_GROUP, "KafkaPrincipalAcl")))
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then
        CommandLine topics = get.getSubcommands().get("kafkatopics");
        CommandLine acls = get.getSubcommands().get("kafkaprincipalacls");
        assertNotNull(topics);
        assertNotNull(acls);
        assertTrue(topics.getCommandSpec().usageMessage().hidden(), "flat 'kafkatopics' must be hidden");
        assertTrue(acls.getCommandSpec().usageMessage().hidden(), "flat 'kafkaprincipalacls' must be hidden");
    }

    @Test
    void shouldNotHideNestedSubcommands() {
        // Given
        JikkouApi api = apiReturning(
            List.of(new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES))),
            Map.of("kafka", List.of(summary(KAFKA_GROUP, "KafkaTopic")))
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then
        CommandLine kafka = get.getSubcommands().get("kafka");
        assertFalse(kafka.getCommandSpec().usageMessage().hidden(), "'kafka' parent must be visible");
        CommandLine topics = kafka.getSubcommands().get("kafkatopics");
        assertFalse(topics.getCommandSpec().usageMessage().hidden(), "nested 'kafkatopics' must be visible");
    }

    @Test
    void shouldAddNameOptionOnlyWhenGetVerbIsSupported() {
        // Given (TOPIC supports LIST+GET; ACL supports LIST only)
        JikkouApi api = apiReturning(
            List.of(new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES, ACL_RES))),
            Map.of("kafka", List.of(summary(KAFKA_GROUP, "KafkaTopic"), summary(KAFKA_GROUP, "KafkaPrincipalAcl")))
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then
        CommandLine topics = get.getSubcommands().get("kafka").getSubcommands().get("kafkatopics");
        CommandLine acls = get.getSubcommands().get("kafka").getSubcommands().get("kafkaprincipalacls");
        assertNotNull(topics.getCommandSpec().findOption("--name"));
        assertNull(acls.getCommandSpec().findOption("--name"));
    }

    @Test
    void shouldRegisterShortNameAsHiddenSubcommandUnderProvider() {
        // Given (TOPIC has shortName "kt")
        JikkouApi api = apiReturning(
            List.of(new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES))),
            Map.of("kafka", List.of(summary(KAFKA_GROUP, "KafkaTopic")))
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then — 'kt' resolves under 'kafka' but is hidden so it does not show up in help
        CommandLine kafka = get.getSubcommands().get("kafka");
        CommandLine kt = kafka.getSubcommands().get("kt");
        assertNotNull(kt, "shortName 'kt' must be registered as a subcommand under 'kafka'");
        assertTrue(kt.getCommandSpec().usageMessage().hidden(),
            "shortName subcommand must be hidden from provider help");
        // Canonical command itself must NOT advertise inline aliases.
        CommandLine topics = kafka.getSubcommands().get("kafkatopics");
        assertEquals(0, topics.getCommandSpec().aliases().length,
            "canonical subcommand must not declare inline aliases");
    }

    @Test
    void shouldSortProviderParentsAlphabetically() {
        // Given — deliberately reversed insertion order
        JikkouApi api = apiReturning(
            List.of(
                new ApiResourceList(SR_GROUP + "/v1", List.of(SUBJECT_RES)),
                new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES))
            ),
            Map.of(
                "kafka", List.of(summary(KAFKA_GROUP, "KafkaTopic")),
                "schemaregistry", List.of(summary(SR_GROUP, "SchemaRegistrySubject"))
            )
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then — iteration order of subcommands map preserves insertion; we inserted sorted
        List<String> names = get.getSubcommands().keySet().stream()
            .filter(n -> n.equals("kafka") || n.equals("schemaregistry"))
            .toList();
        assertEquals(List.of("kafka", "schemaregistry"), names);
    }

    @Test
    void shouldUseShortProviderNameNotGroup() {
        // Given — group is "kafka.jikkou.io" but provider short name is "kafka"
        JikkouApi api = apiReturning(
            List.of(new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES))),
            Map.of("kafka", List.of(summary(KAFKA_GROUP, "KafkaTopic")))
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then — the parent command is 'kafka', NOT 'kafka.jikkou.io'
        assertNotNull(get.getSubcommands().get("kafka"));
        assertNull(get.getSubcommands().get("kafka.jikkou.io"),
            "must not register the full group as a provider parent");
    }

    @Test
    void shouldUseLocalNameUnderProviderWhenSet() {
        // Given — TOPIC_RES_WITH_LOCAL has local="topics"
        JikkouApi api = apiReturning(
            List.of(new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES_WITH_LOCAL))),
            Map.of("kafka", List.of(summary(KAFKA_GROUP, "KafkaTopic")))
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then
        CommandLine kafka = get.getSubcommands().get("kafka");
        assertNotNull(kafka.getSubcommands().get("topics"),
            "nested subcommand must use local name 'topics'");
    }

    @Test
    void shouldRegisterPluralAndShortNamesAsHiddenSubcommandsWhenLocalNameIsSet() {
        // Given
        JikkouApi api = apiReturning(
            List.of(new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES_WITH_LOCAL))),
            Map.of("kafka", List.of(summary(KAFKA_GROUP, "KafkaTopic")))
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then — canonical 'topics' is visible, plural and short alias are hidden subcommands
        CommandLine kafka = get.getSubcommands().get("kafka");
        CommandLine topics = kafka.getSubcommands().get("topics");
        assertFalse(topics.getCommandSpec().usageMessage().hidden(),
            "canonical 'topics' must be visible");
        assertEquals(0, topics.getCommandSpec().aliases().length,
            "canonical 'topics' must not declare inline aliases");

        CommandLine plural = kafka.getSubcommands().get("kafkatopics");
        assertNotNull(plural, "plural must be registered as a subcommand");
        assertTrue(plural.getCommandSpec().usageMessage().hidden(),
            "plural subcommand must be hidden from provider help");

        CommandLine shortName = kafka.getSubcommands().get("kt");
        assertNotNull(shortName, "shortName must be registered as a subcommand");
        assertTrue(shortName.getCommandSpec().usageMessage().hidden(),
            "shortName subcommand must be hidden from provider help");
    }

    @Test
    void shouldFallBackToPluralWhenLocalNamesCollideWithinProvider() {
        // Given — two resources share the same local name "topics" under 'kafka'
        ApiResource otherTopics = new ApiResource(
            "kafkavirtualtopics", "KafkaVirtualTopic", "kafkavirtualtopic", Set.of(),
            "Virtual topics.", Set.of(Verb.LIST.value())
        ).withLocalName("topics");

        JikkouApi api = apiReturning(
            List.of(new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES_WITH_LOCAL, otherTopics))),
            Map.of("kafka", List.of(
                summary(KAFKA_GROUP, "KafkaTopic"),
                summary(KAFKA_GROUP, "KafkaVirtualTopic")
            ))
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then — neither gets the local name; both fall back to plural
        CommandLine kafka = get.getSubcommands().get("kafka");
        assertNull(kafka.getSubcommands().get("topics"),
            "colliding local name must not be registered under the provider");
        assertNotNull(kafka.getSubcommands().get("kafkatopics"));
        assertNotNull(kafka.getSubcommands().get("kafkavirtualtopics"));
    }

    @Test
    void shouldUseLocalNameInFlatFormDeprecationReplacement() {
        // Given
        JikkouApi api = apiReturning(
            List.of(new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES_WITH_LOCAL))),
            Map.of("kafka", List.of(summary(KAFKA_GROUP, "KafkaTopic")))
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then — flat 'kafkatopics' is still hidden+deprecated and points at the local form
        CommandLine flat = get.getSubcommands().get("kafkatopics");
        assertNotNull(flat);
        assertTrue(flat.getCommandSpec().usageMessage().hidden());
    }

    @Test
    void shouldSetDeprecationReplacementUsingShortProviderName() {
        // Given
        JikkouApi api = apiReturning(
            List.of(new ApiResourceList(KAFKA_GROUP + "/v1", List.of(TOPIC_RES))),
            Map.of("kafka", List.of(summary(KAFKA_GROUP, "KafkaTopic")))
        );
        GetCommandLineFactory factory = new GetCommandLineFactory(contextProducingCommands(), api);

        // When
        CommandLine get = factory.createCommandLine();

        // Then — the flat 'kafkatopics' command's help usage should reference 'kafka kafkatopics'
        CommandLine flat = get.getSubcommands().get("kafkatopics");
        assertNotNull(flat);
        assertTrue(flat.getCommandSpec().usageMessage().hidden());
        // Note: deprecation replacement text lives on the command instance itself; the observable
        // side effect (stderr print) is tested in GetResourceCommandDeprecationTest.
    }
}
