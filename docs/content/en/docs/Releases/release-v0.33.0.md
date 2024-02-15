---
title: "Release v0.33.0"
linkTitle: "Release v0.33.0"
weight: 33
---

## Introducing Jikkou 0.33.0

We're excited to unveil the latest release of
Jikkou [0.33.0](https://github.com/streamthoughts/jikkou/releases/tag/v0.33.0). 🎉

To install the new version, please visit the [installation guide](https://www.jikkou.io/docs/install/). For detailed
release notes, check out the [GitHub page](https://github.com/streamthoughts/jikkou/releases/tag/v0.33.0).

### What's New in Jikkou 0.33.0?

* Enhanced resource change format.
* Added support for the patch command.
* Introduced the new `--status` option for `KafkaTopic` resources.
* Exported offset-lag to the status of `KafkaConsumerGroup` resources.

Below is a summary of these new features with examples.

## Diff/Patch Commands

In previous versions, Jikkou provided the `diff` command to display changes required to reconcile input resources.
However, this command lacked certain capabilities to be truly useful. This new version introduces a standardized change
format for all resource types, along with two new options for filtering changes:

* `--filter-resource-op=`: Filters out all state changes except those corresponding to the given operations.
* `--filter-change-op=`: Filters out all resources except those corresponding to the given operations.

The new output format you can expect from the `diff` command is as follows:

```yaml
---
apiVersion: [ group/version of the change ]
kind: [ kind of the change ]
metadata: [ resource metadata ]
spec:
  # Array of state changes
  changes:
    - name: [ name of the changed state ]
      op: [ change operation ]
      before: [ value of the state before the operation ]
      after: [ value of the state after the operation ]
  data: [ static data attached to the resource ]
  op: [ resource change operation ]
```

The primary motivation behind this new format is the introduction of a new patch command. Prior to **Jikkou 0.33.0**,
when
using the `apply` command after a `dry-run` or a `diff` command, Jikkou couldn't guarantee that the applied changes
matched
those returned from the previous command. With **Jikkou 0.33.0**, you can now directly pass the result of the `diff`
command
to the new `patch` command to efficiently apply the desired state changes.

Here's a workflow to create your resources:

**Step 1) Create a resource descriptor file**

```bash
cat << EOF > my-topic.yaml
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: KafkaTopic
metadata:
  name: 'my-topic'
  labels:
    environment: example
spec:
  partitions: 3
  replicas: 1
  configs:
    min.insync.replicas: 1
    cleanup.policy: 'delete'
EOF
```

**Step 2) Run diff**

```bash
jikkou diff -f ./my-topic.yaml > my-topic-diff.yaml

(output)
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaTopicChange"
metadata:
  name: "my-topic"
  labels:
    environment: "example"
  annotations:
    jikkou.io/managed-by-location: "my-topic.yaml"
spec:
  changes:
    - name: "partitions"
      op: "CREATE"
      after: 3
    - name: "replicas"
      op: "CREATE"
      after: 1
    - name: "config.cleanup.policy"
      op: "CREATE"
      after: "delete"
    - name: "config.min.insync.replicas"
      op: "CREATE"
      after: 1
  op: "CREATE"
```

**Step 3) Run patch**

```batch
jikkou patch -f ./my-topic-diff.yaml --mode FULL --output compact
```

(output)

````json
TASK [
  CREATE
] Create topic 'my-topic' (partitions=3, replicas=1, configs=[cleanup.policy=delete, min.insync.replicas=1]) - CHANGED
EXECUTION in 3s 797ms
ok: 0, created: 1, altered: 0, deleted: 0 failed: 0
````

Attempting to apply the changes a second time may result in an error from the remote system:

```yaml
{
  "status": "FAILED",
  "description": "Create topic 'my-topic' (partitions=3, replicas=1,configs=[cleanup.policy=delete,min.insync.replicas=1])",
  "errors": [ {
    "message": "TopicExistsException: Topic 'my-topic' already exists."
  } ],
  ...
}
```

## Resource Provider for Apache Kafka

Jikkou 0.33.0 also packs with some minor improvements for the **Apache Kafka** provider.

### KafkaTopic Status

You can now describe the status of a topic-partitions by using the new `--status` option  
when getting a `KafkaTopic` resource.

```yaml
jikkou get kt --status --selector "metadata.name IN (my-topic)"

---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaTopic"
metadata:
  name: "my-topic"
  labels:
    kafka.jikkou.io/topic-id: "UbZI2N2YQTqfNcbKKHps5A"
  annotations:
    kafka.jikkou.io/cluster-id: "xtzWWN4bTjitpL3kfd9s5g"
spec:
  partitions: 1
  replicas: 1
  configs:
    cleanup.policy: "delete"
  configMapRefs: [ ]
status:
  partitions:
    - id: 0
      leader: 101
      isr:
        - 101
      replicas:
        - 101
```

## KafkaConsumerGroup OffsetLags

With Jikkou 0.33.0, you can export the offset-lag of a `KafkaConsumerGroup` resource using the `--offsets` option.

```yaml
jikkou get kafkaconsumergroups --offsets

---
apiVersion: "kafka.jikkou.io/v1beta1"
kind: "KafkaConsumerGroup"
metadata:
  name: "my-group"
  labels:
    kafka.jikkou.io/is-simple-consumer: false
status:
  state: "EMPTY"
  members: [ ]
  offsets:
    - topic: "my-topic"
      partition: 0
      offset: 16
      offset-lag: 0
  coordinator:
    id: "101"
    host: "localhost"
    port: 9092
```

Finally, all those new features are also completely available through the Jikkou REST Server.

## Wrapping Up

We hope you enjoy these new features. If you encounter any issues with Jikkou v0.33.0, please feel free to open a GitHub
issue on our [project page](https://github.com/streamthoughts/jikkou/issues). Don't forget to give us a ⭐️
on [Github](https://github.com/streamthoughts/jikkou) to support
the team, and join us on [Slack](https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA).