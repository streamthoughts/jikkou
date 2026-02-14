<p align="center">
  <img src="./assets/jikkou-logo-title.png" alt="Jikkou Logo" width="400"/>
</p>

<h3 align="center">Resource as Code for Apache Kafka</h3>
<p align="center">
  Declare. Apply. Automate.<br/>
  Manage your Kafka resources the same way you manage your infrastructure.
</p>

<p align="center">
  <a href="https://github.com/streamthoughts/jikkou/actions/workflows/maven-build.yml"><img src="https://github.com/streamthoughts/jikkou/actions/workflows/maven-build.yml/badge.svg" alt="Build Status"/></a>
  <a href="https://github.com/streamthoughts/jikkou/blob/main/LICENSE"><img src="https://img.shields.io/github/license/streamthoughts/jikkou" alt="License"/></a>
  <a href="https://github.com/streamthoughts/jikkou/releases"><img src="https://img.shields.io/github/v/release/streamthoughts/jikkou?color=blue&label=latest" alt="Release"/></a>
  <a href="https://github.com/streamthoughts/jikkou/stargazers"><img src="https://img.shields.io/github/stars/streamthoughts/jikkou?style=flat" alt="Stars"/></a>
</p>

<p align="center">
  <a href="https://sonarcloud.io/dashboard?id=streamthoughts_jikkou"><img src="https://sonarcloud.io/api/project_badges/measure?project=streamthoughts_jikkou&metric=reliability_rating" alt="Reliability"/></a>
  <a href="https://sonarcloud.io/dashboard?id=streamthoughts_jikkou"><img src="https://sonarcloud.io/api/project_badges/measure?project=streamthoughts_jikkou&metric=sqale_rating" alt="Maintainability"/></a>
  <a href="https://sonarcloud.io/dashboard?id=streamthoughts_jikkou"><img src="https://sonarcloud.io/api/project_badges/measure?project=streamthoughts_jikkou&metric=coverage" alt="Coverage"/></a>
</p>

<p align="center">
  <a href="https://jikkou.io/">Documentation</a> &bull;
  <a href="https://jikkou.io/docs/install/">Install</a> &bull;
  <a href="https://jikkou.io/docs/tutorials/">Tutorials</a> &bull;
  <a href="https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA">Slack</a>
</p>

---

**Jikkou** (jikkou / 実行 — *execution* in Japanese) is an open-source tool that lets you manage Apache Kafka resources declaratively using YAML files, the same way `kubectl` manages Kubernetes resources.

Stop writing scripts. Stop clicking through UIs. Define your desired state, and Jikkou makes it happen.

<p align="center">
  <img src="./assets/demo.gif" alt="Jikkou in action" width="800"/>
</p>

## Why Jikkou?

| | |
|---|---|
| **Declarative** | Define Topics, ACLs, Schemas, Connectors, and Quotas as code in simple YAML files |
| **GitOps-Ready** | Version-control your Kafka configuration and automate changes through CI/CD |
| **Stateless** | No database needed — Jikkou uses your Kafka platform as the source of truth |
| **Safe** | Built-in dry-run mode, validations, and reconciliation engine prevent accidents |
| **Extensible** | Plugin-based architecture with providers, validators, transformations, and templates |
| **Multi-Platform** | Works with Apache Kafka, Confluent Cloud, Aiven, Amazon MSK, Redpanda, and more |

## Quick Start

### Install

```bash
# Via SDKMan (recommended)
sdk install jikkou

# Or via Docker
docker pull streamthoughts/jikkou
```

> See the full [installation guide](https://jikkou.io/docs/install/) for native binaries, Homebrew, and more.

### Define a Kafka topic

```yaml
# kafka-topics.yaml
apiVersion: 'kafka.jikkou.io/v1beta2'
kind: 'KafkaTopic'
metadata:
  name: 'my-topic'
spec:
  partitions: 12
  replicas: 3
  configs:
    min.insync.replicas: 2
```

### Apply it

```bash
jikkou apply --files ./kafka-topics.yaml
```

That's it. Jikkou computes the diff and applies only the necessary changes:

```
TASK [CREATE] Create a new topic my-topic (partitions=12, replicas=3) - CHANGED
EXECUTION in 2s 661ms
ok: 0, created: 1, altered: 0, deleted: 0, failed: 0
```

## Supported Resources

| Apache Kafka | Schema Registry | Kafka Connect | Cloud Providers |
|:---:|:---:|:---:|:---:|
| Topics & Configs | Avro Schemas | Connectors | Aiven (ACLs, Quotas) |
| ACLs | JSON Schemas | | AWS Glue Schemas |
| Quotas | Protobuf Schemas | | |
| Consumer Groups | | | |
| Brokers & Users | | | |
| KTable Records | | | |

## How It Works

<p align="center">
  <img src="./docs/content/en/docs/Overview/jikkou-architecture-overview.png" alt="Architecture" width="800"/>
</p>

Jikkou follows a simple reconciliation loop:

1. **Read** your resource definitions from YAML files (with Jinja templating support)
2. **Compute** the differences between desired state and actual cluster state
3. **Apply** only the minimal set of changes needed
4. **Report** what was created, updated, or deleted

## Deployment Modes

| Mode              | Description                                                                                   |
|-------------------|-----------------------------------------------------------------------------------------------|
| **CLI**           | Run as a command-line tool — perfect for local development and CI/CD pipelines                |
| **API Server**    | Run as a REST API server — ideal for platform teams and automation                            |
| **Docker**        | Available as container images on [Docker Hub](https://hub.docker.com/r/streamthoughts/jikkou) |
| **Native Binary** | GraalVM-compiled native executables for instant startup                                       |

## Documentation

Full documentation is available at **[jikkou.io](https://jikkou.io/)**.

- [Getting Started](https://jikkou.io/docs/install/)
- [Concepts & Architecture](https://jikkou.io/docs/concepts/)
- [Providers Reference](https://jikkou.io/docs/providers/)
- [Tutorials](https://jikkou.io/docs/tutorials/)

## Developers

For build instructions, development setup, and contribution guidelines, see:

- **[CONTRIBUTING.md](./CONTRIBUTING.md)** — How to contribute, coding guidelines, commit conventions
- **[AGENTS.md](./AGENTS.md)** — Detailed development guidelines, build commands, and architecture

### Quick Build

```bash
# Build and run all tests
./mvnw clean verify

# Build without tests
./mvnw clean verify -DskipTests

# Apply code formatting
./mvnw spotless:apply
```

**Requirements:** Java 25, Docker (for integration tests), GraalVM (for native builds)

## Contributors

Jikkou is built by its community. Thank you to everyone who has contributed!

<!-- CONTRIBUTORS-START -->
<a href="https://github.com/streamthoughts/jikkou/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=streamthoughts/jikkou&max=100&columns=12" alt="Contributors"/>
</a>
<!-- CONTRIBUTORS-END -->

Want to see your name here? Check out the [contribution guide](./CONTRIBUTING.md) and [open issues](https://github.com/streamthoughts/jikkou/issues).

## Support the Project

If you find Jikkou useful, please consider:

- Giving it a **[star on GitHub](https://github.com/streamthoughts/jikkou)** to help others discover it
- Joining the **[Slack community](https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA)** to ask questions and share feedback
- **[Contributing](./CONTRIBUTING.md)** code, documentation, or bug reports

## License

Licensed under the [Apache License, Version 2.0](./LICENSE).

---

<p align="center">
  Developed with &#10084; by <a href="https://github.com/fhussonnois">Florian Hussonnois</a> and the Jikkou community.
</p>
