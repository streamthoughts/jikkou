/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.get;

import io.jikkou.client.command.AbstractCommandLineFactory;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.models.ApiOptionSpec;
import io.jikkou.core.models.ApiProvider;
import io.jikkou.core.models.ApiProviderSummary;
import io.jikkou.core.models.ApiResource;
import io.jikkou.core.models.ApiResourceList;
import io.jikkou.core.models.ApiResourceSummary;
import io.jikkou.core.models.ApiResourceVerbOptionList;
import io.jikkou.core.models.ResourceType;
import io.jikkou.core.models.Verb;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

@Singleton
public final class GetCommandLineFactory extends AbstractCommandLineFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GetCommandLineFactory.class);

    @Inject
    public GetCommandLineFactory(@NotNull ApplicationContext applicationContext,
                                 @NotNull JikkouApi api) {
        super(applicationContext, api);
    }

    @Override
    public CommandLine createCommandLine() {
        CommandLine root = new CommandLine(new GetCommand());

        List<ApiResourceList> apiResourceLists = api.listApiResources();
        Map<ResourceKey, String> providerNameByResource = buildProviderIndex();

        // Pass 1 — nested, visible: jikkou get <provider> <kind>
        Map<String, List<ResourceEntry>> byProvider = new TreeMap<>();
        for (ApiResourceList apiResourceList : apiResourceLists) {
            for (ApiResource resource : apiResourceList.resources()) {
                if (!resource.isVerbSupported(Verb.LIST)) continue;
                ResourceType type = ResourceType.of(resource.kind(), apiResourceList.groupVersion());
                String providerKey = resolveProvider(providerNameByResource, type);
                byProvider.computeIfAbsent(providerKey, k -> new ArrayList<>())
                    .add(new ResourceEntry(type, resource));
            }
        }
        for (Map.Entry<String, List<ResourceEntry>> entry : byProvider.entrySet()) {
            String providerKey = entry.getKey();
            CommandLine providerCmd = new CommandLine(new GetCommand());
            CommandSpec providerSpec = providerCmd.getCommandSpec();
            providerSpec.name(providerKey)
                .usageMessage()
                .header(String.format("Resources managed by provider '%s'.", providerKey))
                .description(String.format(
                    "Use jikkou get %s <kind> to describe resources provided by '%s'.",
                    providerKey, providerKey
                ));

            List<ResourceEntry> entries = entry.getValue();
            Set<String> canonicalNames = resolveCanonicalNames(providerKey, entries);
            entries.sort(Comparator.comparing(e -> canonicalNames.contains(e.resource.localName())
                ? e.resource.localName()
                : e.resource.name()));
            for (ResourceEntry re : entries) {
                boolean useLocal = canonicalNames.contains(re.resource.localName());
                String canonical = useLocal ? re.resource.localName() : re.resource.name();
                Set<String> aliases = new LinkedHashSet<>();
                if (useLocal) {
                    aliases.add(re.resource.name());
                }
                aliases.addAll(re.resource.shortNames());
                aliases.remove(canonical);
                // Visible canonical subcommand.
                providerCmd.addSubcommand(buildKindSubcommand(re.type, re.resource, canonical, null, null));
                // Aliases are registered as separate hidden subcommands so they keep working
                // (e.g. `jikkou get kafka kafkatopics`, `jikkou get kafka kt`) without cluttering
                // the provider help.
                for (String alias : aliases) {
                    CommandLine aliasCmd = buildKindSubcommand(re.type, re.resource, alias, null, null);
                    aliasCmd.getCommandSpec().usageMessage().hidden(true);
                    providerCmd.addSubcommand(aliasCmd);
                }
            }
            root.addSubcommand(providerCmd);
        }

        // Pass 2 — flat, hidden, deprecated: jikkou get <kind>
        for (ApiResourceList apiResourceList : apiResourceLists) {
            for (ApiResource resource : apiResourceList.resources()) {
                if (!resource.isVerbSupported(Verb.LIST)) continue;
                ResourceType type = ResourceType.of(resource.kind(), apiResourceList.groupVersion());
                String providerKey = resolveProvider(providerNameByResource, type);
                String flatName = resource.name();
                String canonicalUnderProvider = Optional.ofNullable(resource.localName()).orElse(flatName);
                String replacement = providerKey + " " + canonicalUnderProvider;
                addHiddenDeprecated(root, type, resource, flatName, replacement);
                for (String shortName : resource.shortNames()) {
                    addHiddenDeprecated(root, type, resource, shortName, replacement);
                }
            }
        }

        return root;
    }

    /**
     * Resolves which resources in a provider may use their {@code localName} as the
     * canonical subcommand. A local name is accepted only if it is unique within the
     * provider; on collision, all colliding resources fall back to their plural name
     * and the collision is logged.
     */
    private static Set<String> resolveCanonicalNames(String providerKey, List<ResourceEntry> entries) {
        Map<String, List<ResourceEntry>> byLocal = new HashMap<>();
        for (ResourceEntry re : entries) {
            String local = re.resource.localName();
            if (local == null || local.isBlank()) continue;
            byLocal.computeIfAbsent(local, k -> new ArrayList<>()).add(re);
        }
        Set<String> winners = new HashSet<>();
        for (Map.Entry<String, List<ResourceEntry>> e : byLocal.entrySet()) {
            if (e.getValue().size() == 1) {
                winners.add(e.getKey());
            } else {
                LOG.warn(
                    "Local name '{}' under provider '{}' is declared by {} resources ({}); "
                        + "falling back to plural name for each.",
                    e.getKey(), providerKey, e.getValue().size(),
                    e.getValue().stream().map(r -> r.resource.kind()).toList()
                );
            }
        }
        return winners;
    }

    private Map<ResourceKey, String> buildProviderIndex() {
        Map<ResourceKey, String> index = new HashMap<>();
        for (ApiProviderSummary summary : api.getApiProviders().providers()) {
            ApiProvider provider;
            try {
                provider = api.getApiProvider(summary.name());
            } catch (Exception e) {
                // A provider may fail to introspect (e.g. missing mandatory config at CLI startup).
                // In that case we fall back to ResourceType.group() for its resources.
                continue;
            }
            for (ApiResourceSummary resource : provider.spec().resources()) {
                index.put(new ResourceKey(resource.group(), resource.kind()), summary.name());
            }
        }
        return index;
    }

    private static String resolveProvider(Map<ResourceKey, String> index, ResourceType type) {
        String fromIndex = index.get(new ResourceKey(type.group(), type.kind()));
        if (fromIndex != null) return fromIndex;
        return Optional.ofNullable(type.group()).orElse("core");
    }

    private void addHiddenDeprecated(CommandLine root, ResourceType type, ApiResource resource,
                                     String name, String replacement) {
        CommandLine cmd = buildKindSubcommand(type, resource, name, name, replacement);
        cmd.getCommandSpec().usageMessage().hidden(true);
        root.addSubcommand(cmd);
    }

    private CommandLine buildKindSubcommand(ResourceType type,
                                            ApiResource resource,
                                            String name,
                                            String deprecatedForm,
                                            String deprecatedReplacement) {
        final GetResourceCommand command = applicationContext.getBean(GetResourceCommand.class);
        command.setType(type);
        if (deprecatedForm != null) {
            command.setDeprecated(deprecatedForm, deprecatedReplacement);
        }

        final CommandLine subcommand = new CommandLine(command);
        CommandSpec spec = subcommand.getCommandSpec();
        spec.name(name)
            .usageMessage()
            .header(String.format("Get all '%s' resources.", resource.kind()))
            .description(String.format(
                "Use jikkou get %s when you want to describe the state of all resources of type '%s'.",
                name, resource.kind()
            ));

        Optional<ApiResourceVerbOptionList> optional = resource.getVerbOptionList(Verb.LIST);
        if (optional.isPresent()) {
            for (ApiOptionSpec option : optional.get().options()) {
                spec.addOption(createOptionSpec(option, command));
            }
        }
        if (resource.isVerbSupported(Verb.GET)) {
            spec.addOption(CommandLine.Model.OptionSpec
                .builder("--name")
                .hasInitialValue(false)
                .paramLabel("<name>")
                .type(String.class)
                .description("The name of the resource.")
                .required(false)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        String str = Optional.ofNullable(value).map(Objects::toString).orElse(null);
                        command.setName(str);
                        return null;
                    }
                })
                .build()
            );
        }
        return subcommand;
    }

    private record ResourceEntry(ResourceType type, ApiResource resource) {}

    private record ResourceKey(String group, String kind) {}
}
