---
title: "Manage a Fleet of Kafka Clusters"
linkTitle: "Manage a Kafka Fleet"
weight: 65
description: >
  Apply the same resource definitions across multiple Kafka clusters and providers in one command, with per-cluster results.
---

Most Kafka estates are fleets: a production cluster per region, staging, and often a mix of
on-premises and managed services. Jikkou applies one declarative model across all of them:
define each cluster as a named provider instance, group them, and target the group in one
command.

## Before you begin

* A Jikkou context configured for your platform. See [Getting Started](/docs/tutorials/get_started/).

## 1. Declare one provider instance per cluster

Each cluster is a named entry under `provider`, all sharing the same provider type:

```hocon
jikkou {
  provider.kafka-prod-eu {
    type = io.jikkou.kafka.KafkaExtensionProvider
    config {
      client {
        bootstrap.servers = "kafka-eu.internal:9092"
      }
    }
  }
  provider.kafka-prod-us {
    type = io.jikkou.kafka.KafkaExtensionProvider
    config {
      client {
        bootstrap.servers = "kafka-us.internal:9092"
      }
    }
  }
  provider.kafka-staging {
    type = io.jikkou.kafka.KafkaExtensionProvider
    config {
      client {
        bootstrap.servers = "kafka-staging.internal:9092"
      }
    }
  }

  # Named groups for batch operations
  provider-groups {
    prod = ["kafka-prod-eu", "kafka-prod-us"]
  }
}
```

## 2. Target the fleet

The three selection flags are mutually exclusive:

```bash
# One cluster
jikkou apply -f ./resources --provider kafka-prod-eu

# A named group
jikkou apply -f ./resources --provider-group prod

# Every registered instance
jikkou apply -f ./resources --provider-all
```

Multi-provider runs are **fail-fast** by default: the first failing cluster aborts the run.
Add `--continue-on-error` to keep going and get results for the remaining clusters.

## 3. Read per-cluster results

Results are reported per provider. In TEXT output, tasks are grouped under a provider header:

```
PROVIDER [kafka-prod-eu] *******************************************************
TASK [CREATE] Create a new topic orders-events (partitions=6, replicas=3) - CHANGED
PROVIDER [kafka-prod-us] *******************************************************
TASK [CREATE] Create a new topic orders-events (partitions=6, replicas=3) - CHANGED
EXECUTION in 3s 421ms
ok : 0, created : 2, altered : 0, deleted : 0 failed : 0
```

In JSON or YAML output, every change carries the `jikkou.io/provider` annotation, so results
stay attributable when piped into other tools:

```json
{
  "change": {
    "metadata": {
      "annotations": {
        "jikkou.io/provider": "kafka-prod-eu"
      }
    }
  }
}
```

## Fleet-wide drift detection

`jikkou diff` accepts the same provider flags, so a scheduled CI job can check the whole
fleet at once and tell you **which** cluster drifted:

```bash
jikkou diff -f ./resources --provider-group prod --fail-on-changes
```

See [Detect configuration drift](/docs/how-to-guides/automating/detect-configuration-drift/)
for the full CI setup.

## Related

* [jikkou apply command reference](/docs/reference/cli/commands/jikkou-apply/)
* [Enforce governance policies](/docs/how-to-guides/enforce-policies/): the same policies can
  guard every cluster in the fleet
