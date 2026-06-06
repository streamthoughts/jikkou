---
categories: [ ]
tags: [ "feature", "actions" ]
title: "KafkaShareGroupsResetOffsets"
linkTitle: "KafkaShareGroupsResetOffsets"
weight: 20
description: >
  Learn how to reset the offsets of Kafka Share Groups.
---

{{% pageinfo color="info" %}}
This section describes the `KafkaShareGroupsResetOffsets` action, used to reset the
Share-Partition Start Offset (SPSO) of one or more Kafka share groups.
{{% /pageinfo %}}

{{% alert title="Broker requirement" color="warning" %}}
Requires a Kafka **4.x** cluster with share groups enabled (`group.share.enable=true`).
{{% /alert %}}

## Usage

You must choose one of the following reset specifications: `to-earliest`, `to-latest`, `to-offset`,
or `delete-offsets`.

| Option            | Description                                                                          |
|-------------------|--------------------------------------------------------------------------------------|
| `--group`         | The share group to act on.                                                           |
| `--groups`        | The share groups to act on.                                                          |
| `--all`           | Act on all share groups.                                                             |
| `--topic`         | Topics to include. Each entry is `topic` (all partitions) or `topic:partition`.      |
| `--to-earliest`   | Reset offsets to the earliest offset.                                                |
| `--to-latest`     | Reset offsets to the latest offset.                                                  |
| `--to-offset`     | Reset offsets to a specific offset.                                                  |
| `--delete-offsets`| Delete the share-partition offsets for the given topics.                             |
| `--includes`      | Patterns of share groups to include.                                                 |
| `--excludes`      | Patterns of share groups to exclude.                                                 |
| `--dry-run`       | Only show results without executing changes.                                         |

## Examples

Reset a single share group to the earliest offset for a topic:

```bash
$ jikkou action KafkaShareGroupsResetOffsets execute \
    --group my-share-group \
    --topic my-topic \
    --to-earliest
```

Delete the share-partition offsets for a topic:

```bash
$ jikkou action KafkaShareGroupsResetOffsets execute \
    --group my-share-group \
    --topic my-topic \
    --delete-offsets
```
