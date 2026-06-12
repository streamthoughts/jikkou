---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Kafka Share Groups"
linkTitle: "Share Groups"
weight: 11
description: >
  Learn how to manage Kafka Share Groups (Queues).
aliases:
  - /docs/providers/kafka/resources/share_groups/
---

{{% pageinfo color="info" %}}
This section describes the resource definition format for `KafkaShareGroup` entities, which can be used to
manage the share groups (Queues, [KIP-932](https://cwiki.apache.org/confluence/display/KAFKA/KIP-932%3A+Queues+for+Kafka))
of a specific Kafka cluster.
{{% /pageinfo %}}

{{% alert title="Broker requirement" color="warning" %}}
Share groups require a Kafka **4.x** cluster with share groups enabled on the brokers
(`group.share.enable=true` and the `share.version` feature). On older clusters the share group
operations are not available.
{{% /alert %}}

## Listing `KafkaShareGroup`

You can retrieve the state of Kafka Share Groups using the `jikkou get kafkasharegroups` (or `jikkou get ksg`) command.

### Examples

(command)

```bash
$ jikkou get kafkasharegroups --in-states STABLE --offsets
```

(output)

```yaml
---
apiVersion: "kafka.jikkou.io/v1"
kind: "KafkaShareGroup"
metadata:
  name: "my-share-group"
spec:
  configs:
    share.auto.offset.reset: "earliest"
status:
  state: "STABLE"
  members:
    - memberId: "share-consumer-b103994e-bcd5-4236-9d03-97065057e594"
      clientId: "share-consumer"
      host: "/127.0.0.1"
      rackId: "rack-1"
      assignments:
        - "my-topic-0"
  offsets:
    - topic: "my-topic"
      partition: 0
      offset: 0
  coordinator:
    id: "101"
    host: "localhost"
    port: 9092
```

## Managing share group configuration

The `spec.configs` map lets you declaratively manage the dynamic group configuration of a share group
(e.g. `share.auto.offset.reset`, `share.record.lock.duration.ms`, `share.isolation.level`). These are applied
against the Kafka `GROUP` configuration resource and are reconciled like topic configs (drift detection +
incremental alter).

```yaml
apiVersion: "kafka.jikkou.io/v1"
kind: "KafkaShareGroup"
metadata:
  name: "orders-queue"
spec:
  configs:
    share.record.lock.duration.ms: 30000
    share.auto.offset.reset: "earliest"
```

Apply it with:

```bash
$ jikkou apply --files orders-queue.yaml
```

By default, group configs present on the cluster but absent from the resource are removed
(`config-delete-orphans=true`). Set it to `false` to leave externally-managed configs untouched.

## Deleting `KafkaShareGroup`

A share group can be deleted by reconciling a resource flagged for deletion (`jikkou delete`),
which removes the group via the AdminClient `deleteShareGroups` API.

## Resetting share group offsets

See the [`KafkaShareGroupsResetOffsets`]({{< relref "../Actions/KafkaShareGroupsResetOffsets.md" >}}) action
to reset the Share-Partition Start Offset (SPSO) of one or more share groups.
