---
name: managing-kafka-resources
description: Use when managing Apache Kafka topics, ACLs, quotas, users, consumer groups, Schema Registry subjects, Kafka Connect connectors, or Aiven, Confluent Cloud, AWS Glue, and Apache Iceberg resources declaratively with the Jikkou CLI: creating, inspecting, or changing resources, previewing changes before apply, or diagnosing Kafka misconfigurations.
---

# Jikkou: Resource as Code for Apache Kafka

Declarative management of Kafka-ecosystem resources: YAML in, reconciliation against the real cluster out. Stateless: `diff` compares against live cluster state, never a state file.

## Setup check (always run first)

1. `jikkou --version` confirms the CLI is on PATH. Missing? See "If jikkou is not installed".
2. `jikkou health get kafka` verifies connectivity (`health get-indicators` only *lists* indicators; it doesn't check connectivity). For other providers use `jikkou health get <indicator>`; indicator names differ from provider names, see `references/diagnostics.md` for the exact list.
3. No context configured? Run `jikkou config current-context` and `jikkou config view`; config lives in `~/.jikkou/config`. See the context safety rule below.

## Core workflow

Discover → author → validate → diff → apply. Never skip validate/diff.

- Inspect current state: `jikkou get <provider> <kind> -o JSON` (providers: kafka, schemaregistry, kafkaconnect, aiven, confluent-cloud, aws, iceberg). Filter: `--name <name>` or `-s 'metadata.name IN (a,b)'`.
- Discover kinds/schemas: `jikkou api-resources list`, then `jikkou api-resources schema --api-version=<v> --kind=<k>`. `list → schema → write YAML → validate → diff` is the canonical authoring chain.
- Validate: `jikkou validate --files <file>` returns normalized resources or errors.
- Preview: `jikkou diff --files <file>` shows the exact changes vs. the live cluster.
- Apply: `jikkou apply --files <file> --dry-run` first; drop `--dry-run` only after the user confirms the diff.

## Safety rules

- Never run `apply` without first showing the user `diff` or `--dry-run` output.
- Deleting a resource requires the annotation `jikkou.io/delete: true` on it — never delete another way.
- Treat contexts named prod/production with extra confirmation before any non-dry-run apply.
- Never run `jikkou config use-context` casually: jikkou eagerly loads the current context on every call, so a broken context bricks *every* command, not just the one you meant to try. Check `current-context`/`config view` first; if a switch is truly needed, verify the target, then switch back.

## Provider references

Before authoring resources for a provider, read the matching file:
`references/kafka.md`, `references/schema-registry.md`, `references/kafka-connect.md`, `references/aiven.md`, `references/confluent-cloud.md`, `references/aws-glue.md`, `references/iceberg.md`.
For misconfiguration checks, consumer lag, and ACL audits, use `references/diagnostics.md`.

## If jikkou is not installed

Confirm with the user first. In order of preference:
- SDKMan: `sdk install jikkou`
- Homebrew: `brew install streamthoughts/tap/jikkou`
- No install rights: prefix every command with `docker run --rm -v $PWD:/work -w /work streamthoughts/jikkou`
After install: `jikkou --version`, then `jikkou health get kafka` before proceeding.

## Common mistakes

- Guessing YAML fields instead of reading the provider reference or running `api-resources schema`; `validate` catches this, so run it.
- Using `kafka-topics.sh`-style imperative commands — everything goes through resource files.
- Editing a live resource without fetching it first: `jikkou get ... -o YAML` is the starting point.
- Applying directly (e.g. `jikkou create -f file.yaml`) skipping `validate`/`diff`/`--dry-run`, even when the change looks trivial; it mutates the cluster with no preview.
- Rediscovering commands via repeated `--help` calls instead of reading `references/<provider>.md` first (far more tool calls for the same answer).
- Switching contexts to "just try" a different cluster — see the context safety rule above.
