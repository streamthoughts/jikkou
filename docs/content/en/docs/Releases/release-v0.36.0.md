---
title: "Release v0.36.0"
linkTitle: "Release v0.36.0"
weight: 36
---

## 🚀 Introducing Jikkou 0.36.0

We’re excited to announce the release of
[Jikkou 0.36.0](https://github.com/streamthoughts/jikkou/releases/tag/v0.36.0)! 🎉

This release brings **major new features** to make Jikkou more powerful, flexible, and GitOps-friendly than ever before:

- 🆕 New resource for managing **AWS Glue Schemas**
- 🛡️ New resource for defining **ValidatingResourcePolicy**
- 🔎 Support for **Google CEL selectors**
- 📦 New concept of **Resource Repositories**
- ⚙️ Enhanced Kafka **actions**
- 🔄 Evolved provider configuration system

To install the new version, check out the [installation guide](https://www.jikkou.io/docs/install/).  
For detailed release notes, see the [GitHub page](https://github.com/streamthoughts/jikkou/releases/tag/v0.36.0).

---

## 🆕 AWS Glue Schema Provider

We know that many developers and DevOps teams rely on Jikkou to manage their **AWS MSK clusters**.  
With this release, we’re going one step further: Jikkou 0.36.0 adds a new provider for **AWS Glue Schemas**.

You can now **fully manage schemas registered in AWS Glue Registries** — just like you already do with Confluent Schema
Registry.

Example:

```yaml
# file: ./aws-glue-schema-user.yaml
---
apiVersion: "aws.jikkou.io/v1"
kind: "AwsGlueSchema"
metadata:
  name: "Person"
  labels:
    glue.aws.amazon.com/registry-name: Test
  annotations:
    glue.aws.amazon.com/normalize-schema: true
spec:
  compatibility: "BACKWARD"
  dataFormat: "AVRO"
  schemaDefinition: |
    {
      "namespace": "example",
      "type": "record",
      "name": "Person",
      "fields": [
        {
          "name": "id",
          "type":  "int",
          "doc": "The person's unique ID (required)"
        },
        {
          "name": "firstname",
          "type": "string",
          "doc": "The person's legal firstname (required)"
        },
        {
          "name": "lastname",
          "type": "string",
          "doc": "The person's legal lastname (required)"
        }
    }
```

👉 With the Jikkou CLI, you can now run commands like:

```bash
jikkou get aws-glueschemas
```

For more information: [AWS Provider Documentation](https://www.jikkou.io/docs/providers/aws/)

---

## 🛡️ ValidatingResourcePolicy for Smarter Governance

Validating resources has always been a challenge in Jikkou.  
Earlier releases provided a [validation chain](https://www.jikkou.io/docs/providers/kafka/validations/) with built-in
checks (e.g., `TopicMinReplicationFactor`, `TopicMaxNumPartitions`, `TopicNamePrefix`).  
However, these were **limited, provider-specific, and mostly resource-scoped**.

With Jikkou 0.36, we’re introducing **`ValidatingResourcePolicy`** — a declarative, reusable way to enforce governance
and compliance across *any* resource.

### ⚡ How It Works

A `ValidatingResourcePolicy` defines:

- **Selectors**:
    - Match by resource kinds or operations
    - Match by labels
    - Use **Google CEL** expressions for advanced logic

- **Rules**:  
  Write expressions using [Google CEL](https://cel.dev/?hl=fr) to enforce validation logic.

- **Failure Policies**:  
  Decide what happens when validation fails:
    - `FAIL` → abort the operation
    - `CONTINUE` → log but proceed
    - `FILTER` → automatically remove invalid resources

### 📑 Examples

Enforcing min/max partitions for `KafkaTopic`:

```yaml
---
apiVersion: core.jikkou.io/v1
kind: ValidatingResourcePolicy
metadata:
  name: KafkaTopicPolicy
spec:
  failurePolicy: FAIL
  selector:
    matchResources:
      - kind: KafkaTopic
  rules:
    - name: MaxTopicPartitions
      expression: "resource.spec.partitions <= 50"
      messageExpression: "'Topic partitions MUST be <= 50, but was: ' + string(resource.spec.partitions)"

    - name: MinTopicPartitions
      expression: "resource.spec.partitions >= 3"
      message: "Topic must have at least 3 partitions"
```

Filtering out `DELETE` operations on Kafka Topics:

```yaml
---
apiVersion: core.jikkou.io/v1
kind: ValidatingResourcePolicy
metadata:
  name: KafkaTopicPolicy
spec:
  failurePolicy: FILTER
  selector:
    matchResources:
      - kinds: KafkaTopicChange
  rules:
    - name: FilterDeleteOperation
      expression: "resource.spec.op == 'DELETE'"
      messageExpression: "'Operation ' + resource.spec.op + ' on topics is not authorized'"
```

👉 Learn more: [ValidatingResourcePolicy Documentation](https://www.jikkou.io/docs/concepts/validations/)

---

## 📦 Resource Repositories — GitOps-Friendly Resource Management

Managing and sharing resource definitions just got easier.  
Jikkou 0.36 introduces **Resource Repositories**, allowing you to load resources directly from **GitHub repositories**
or **local directories**.

Repositories are perfect for:

- Reusable resources across multiple environments
- Shared definitions across teams
- Keeping **transient or computed resources** (e.g., `ConfigMap`, `ValidatingResourcePolicy`) separate from persistent
  ones
- Injecting dynamic configuration without polluting your main repo

💡 **Use Case Spotlight**:  
Instead of keeping temporary validation policies or config maps in your CLI input, you can store them in a repository
and inject them dynamically. This makes transient resources **clean, shareable, and environment-specific**.

### Example Configuration

```hocon
jikkou {
  repositories = [
    {
      name = "github-repository"
      type = io.streamthoughts.jikkou.core.repository.GitHubResourceRepository
      config {
        repository = "streamthoughts/jikkou"
        branch = "main"
        paths = [
          "examples/",
        ]
        # Optionally set an access token for private repositories
        # token = ${?GITHUB_TOKEN}
      }
    }
  ]
}
```

👉 Learn more: [Repositories Documentation](https://www.jikkou.io/docs/concepts/repositories/)

---

## 🔎 Expression-based CLI Selectors

Selectors just became more powerful with **Google CEL**.  
You can now filter resources dynamically based on *any* attribute.

Example: List all topics with more than 12 partitions:

```bash
jikkou get kafkatopics --selector "expr: resource.spec.partitions >= 12"
```

---

## ⚙️ Actions Improvements

- Added **`TruncateKafkaTopicRecords`** action → truncate topic-partitions to a specific datetime.
- Extended **`KafkaConsumerGroupsResetOffsets`**:
    - Reset offsets for multiple consumer groups.
    - New options:
        - `--all`: apply to all consumer groups
        - `--groups`: specify consumer groups
        - `--includes`: regex patterns for inclusion
        - `--excludes`: regex patterns for exclusion

---

## 🔄 Migration: Provider Configurations

Starting in 0.36.0, provider configuration has evolved to support future extensibility.

**Before 0.36.0:**

```hocon
jikkou {
  extension.providers {
    kafka.enabled = true
  }
  kafka {
    client {
      bootstrap.servers = "localhost:9092"
    }
  }
}
```

**After 0.36.0:**

```hocon
provider.kafka {
  enabled = true
  type = io.streamthoughts.jikkou.kafka.KafkaExtensionProvider
  config = {
    client {
      bootstrap.servers = "localhost:9092"
    }
  }
}
```

⚠️ Old configs still work for now, but we **recommend migrating**.

---

## ✅ Wrapping Up

We can’t wait to see what you build with this new release.

If you encounter any issues, please open a GitHub issue on
our [project page](https://github.com/streamthoughts/jikkou/issues).  
Don’t forget to give us a ⭐️ on [GitHub](https://github.com/streamthoughts/jikkou) and join the community
on [Slack](https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA).  