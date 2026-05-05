---
title: "Migrating to Jikkou 1.0"
linkTitle: "Migrate to 1.0"
description: "Step-by-step upgrade guide from Jikkou 0.37.x to 1.0.0."
weight: -100
---

Jikkou 1.0.0 is a major release. The good news: the project's first real breaking change — the move from `io.streamthoughts.jikkou.*` to `io.jikkou.*` — ships with a runtime deprecation shim, so most existing setups will run unchanged with warnings while you migrate at your own pace.

This guide walks through the upgrade end to end.

## Before You Upgrade

A short checklist before you start:

1. **Pin your current version.** Make sure your CI and any automation reference `0.37.3` (or whichever 0.37.x you run today) explicitly, not `latest`, so you can roll back fast.
2. **Back up your configurations.** Commit any uncommitted Jikkou configuration and resource files. The migration is mechanical, but a clean baseline makes review easier.
3. **Plan a window.** The shim means you don't *need* downtime, but if you operate Jikkou as a service (`jikkou-api-server`), the upgrade is a normal rolling deploy.
4. **Read the [v1.0.0 release notes]({{< relref "/docs/Releases/release-v1.0.0.md" >}}).** This guide is focused on *change recipes*; the release notes explain the *why*.

## 1. Update Maven / Gradle Coordinates

The Maven group changed from `io.streamthoughts` to `io.jikkou`. Artifact names are unchanged.

**Maven**

```xml
<!-- Before -->
<dependency>
  <groupId>io.streamthoughts</groupId>
  <artifactId>jikkou-core</artifactId>
  <version>0.37.3</version>
</dependency>

<!-- After -->
<dependency>
  <groupId>io.jikkou</groupId>
  <artifactId>jikkou-core</artifactId>
  <version>1.0.0</version>
</dependency>
```

**Gradle**

```kotlin
// Before
implementation("io.streamthoughts:jikkou-core:0.37.3")

// After
implementation("io.jikkou:jikkou-core:1.0.0")
```

If you depend on multiple Jikkou modules (`jikkou-extension-rest-client`, individual providers, etc.), update them all in one pass — they all moved together.

## 2. Rename Java Imports

If you maintain custom extensions, providers, or any Java code that imports Jikkou classes, replace the package prefix:

```
io.streamthoughts.jikkou.   →   io.jikkou.
```

**With your IDE.** Use the project-wide find-and-replace, scoped to `*.java` files. IntelliJ's "Refactor → Rename" on the package root handles imports and `pom.xml` references in one step.

**With `sed` (POSIX-friendly):**

```bash
# From the repo root
git ls-files '*.java' '*.kt' '*.scala' \
  | xargs sed -i 's|io\.streamthoughts\.jikkou|io.jikkou|g'
```

**Don't rename your own packages.** Only the prefix `io.streamthoughts.jikkou.*` belonging to Jikkou itself moved.

**You don't have to do this immediately.** The runtime keeps resolving old class names through a deprecation shim and logs a warning. This means a staged rollout is safe: upgrade the dependency first, observe warnings in your logs, then schedule the import rename. The shim is scheduled for removal in **1.1.0**.

## 3. Migrate Off Deprecated `JikkouApi` Overloads

Four `JikkouApi` overloads that take a raw `Configuration` have been deprecated since 0.37.0 (`@Deprecated(forRemoval = true)`) and will be removed in a future release. If you have custom Java code calling them, switch to the `GetContext` / `ListContext` builders:

| Deprecated | Replacement |
| --- | --- |
| `getResource(Class<?>, String, Configuration)` | `getResource(ResourceType, String, GetContext)` |
| `getResource(ResourceType, String, Configuration)` | `getResource(ResourceType, String, GetContext)` |
| `listResources(Class<?>, Selector, Configuration)` | `listResources(ResourceType, ListContext)` |
| `listResources(ResourceType, Selector, Configuration)` | `listResources(ResourceType, ListContext)` |

Recipe:

```java
// Before
KafkaTopic topic = api.getResource(KafkaTopic.class, "events", configuration);

ResourceList<KafkaTopic> topics = api.listResources(
    KafkaTopic.class, selector, configuration);

// After
KafkaTopic topic = api.getResource(
    ResourceType.of(KafkaTopic.class),
    "events",
    GetContext.builder().configuration(configuration).build()
);

ResourceList<KafkaTopic> topics = api.listResources(
    ResourceType.of(KafkaTopic.class),
    ListContext.builder().selector(selector).configuration(configuration).build()
);
```

The new context objects also accept a `provider` (string) — that's how you target a specific provider instance when you have several configured.

## 4. CLI Changes

### `get` is now grouped by provider

`jikkou get` subcommands are organised under their provider. The canonical form is now:

```bash
jikkou get kafka topics
jikkou get kafka acls
jikkou get schema-registry subjects
```

Existing scripts that call the old flat form (`jikkou get topics`, `jikkou get kafkatopics`, `jikkou get kt`, …) **still work** — the old names are registered as hidden, deprecated subcommands that print a deprecation notice on each invocation. Plan to switch your scripts to the provider-grouped form before 1.1.0.

### New flags on reconciliation commands

`apply`, `create`, `update`, `patch`, `replace`, `delete`, and `diff` accept three new options for multi-provider setups:

| Flag | Purpose |
| --- | --- |
| `--provider-all` | Apply to every registered provider of the matching type. |
| `--provider-group <name>` | Apply to a named provider group (configured under `jikkou.provider-groups`). |
| `--continue-on-error` | Continue the batch when one provider fails (default: fail-fast). |

`--provider`, `--provider-all`, and `--provider-group` are mutually exclusive. None of them are required: if you have a single provider of each type, your existing commands keep working unchanged.

### Output format options

`-o/--output` now accepts `JSON`, `YAML`, or `TABLE` on list and get commands.

## 5. REST API Additions

`ResourceReconcileRequest` accepts two new optional fields:

```json
{
  "resources": [ ... ],
  "providers": ["kafka-prod-eu", "kafka-prod-us"],
  "continueOnError": true
}
```

Both fields are additive. Existing clients that omit them are unaffected.

## 6. Verify the Upgrade

After updating coordinates and (optionally) imports:

```bash
# Confirm the version
jikkou --version

# Check that resources still parse and providers still load
jikkou api-providers list
jikkou api-resources

# Validate your manifests against the running providers
jikkou validate --files <your-manifests>
```

If you're running custom extensions, watch the logs on first start for `Class name 'io.streamthoughts.jikkou.…' is deprecated` warnings — those are your remaining import-rename targets.

## 7. Rolling Back

If something goes wrong, revert the coordinate change to fall back to 0.37.3. Pin to the previous version via the original group:

```xml
<groupId>io.streamthoughts</groupId>
<artifactId>jikkou-core</artifactId>
<version>0.37.3</version>
```

Or download the binary from the [v0.37.3 GitHub release](https://github.com/streamthoughts/jikkou/releases/tag/v0.37.3).

## Need Help?

- 🐛 Open an issue: <https://github.com/streamthoughts/jikkou/issues>
- 💬 Ask on Slack: <https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA>
