# Diagnostics with Jikkou

Jikkou reads are snapshots of live cluster state — ideal for config audits, not a monitoring system. For lag *trends*, direct the user to their monitoring stack; for point-in-time answers, use the recipes below.

## Connectivity / health checks

Before diagnosing anything, confirm the target is reachable:
```bash
jikkou health get kafka           # or: schemaregistry | avnservice | kafkaconnect | iceberg
```
`jikkou health get-indicators` only lists the available indicator names — it does not report status; use `jikkou health get <name>` for the actual health check. There is no `confluent-cloud` or `aws` indicator.

## Misconfiguration audit

Fetch everything, then check each topic:
```bash
jikkou get kafka topics -o JSON --default-configs
jikkou get kafka brokers -o JSON
```
Red flags to report:
- `min.insync.replicas` >= `replicas` (producers with acks=all will fail)
- `replicas: 1` on anything that looks production-critical
- `retention.ms` extreme values (< 1h or > 30d) — confirm intent with the user
- `cleanup.policy: compact` on topics whose producers don't use keys (can't verify from config alone — flag it as a question)
- partitions = 1 on high-throughput topics

## Who can access a topic

```bash
jikkou get kafka acls -o JSON
```
Filter client-side for the topic name in `spec.acls[].resource.pattern`, honoring `patternType` (literal vs prefixed). Report principal, operations, host.

An empty (or non-matching) ACL result doesn't mean "nobody can access it" — check the broker authorizer configuration too:
```bash
jikkou get kafka brokers --static-broker-configs -o JSON
```
Look at `authorizer.class.name` (no authorizer configured means no ACL enforcement at all), `allow.everyone.if.no.acl.found` (true grants access to anyone when no ACL matches), and `super.users` (listed principals bypass ACL checks entirely). All three change the answer to "who can read topic X" independently of the ACL list itself.

Caution: the Kafka Admin API does **not** return `allow.everyone.if.no.acl.found` or `super.users` — they are absent from `--static-broker-configs` output even when explicitly set on the broker. Absent means *unknown*, not "default false". So when the ACL list is empty but an authorizer is configured, do not conclude "nobody can access it": report that no ACLs are defined and that effective access depends on `allow.everyone.if.no.acl.found` and `super.users`, which must be read from the broker's server.properties or deployment config, not from Jikkou.

## Stuck consumer snapshot

Take two snapshots ~30s apart:
```bash
jikkou get kafka consumer-groups -o JSON --offsets
sleep 30
jikkou get kafka consumer-groups -o JSON --offsets
```
A group is likely stuck when: state is STABLE, committed offsets unchanged between snapshots, and end offsets advanced (the output has only `offset` and `offset-lag` per partition; end offset = offset + offset-lag). EMPTY state with lag means no active members — different problem, report it as such.

To narrow the snapshot to specific states, use `--in-states`, but note it only accepts a **single** value (e.g. `--in-states=STABLE`) — a comma-separated list or a repeated flag both fail with an `IllegalArgumentException`/duplicate-option error. Run one command per state if you need more than one.
