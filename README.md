<h1 style="text-align: center;">Jikkou: The Open Source Resource as Code framework for Apache Kafka&reg;.</h1>

<p style="text-align: center;">
  <img src="./assets/jikkou-logo-title.png" alt="Jikkou Logo"/>
  <h2 style="text-align: center;">
    Developed by Kafka ❤️, for all Kafka users!
  </h2>
</p>

<p style="text-align: center;">

![](https://img.shields.io/github/license/streamthoughts/jikkou)
![](https://img.shields.io/github/issues/streamthoughts/jikkou)
![](https://img.shields.io/github/forks/streamthoughts/jikkou)
![](https://img.shields.io/github/stars/streamthoughts/jikkou)
![](https://github.com/streamthoughts/jikkou/actions/workflows/maven-build.yml/badge.svg)

![Reliability_rating](https://sonarcloud.io/api/project_badges/measure?project=streamthoughts_jikkou&metric=reliability_rating)
![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=streamthoughts_jikkou&metric=sqale_rating)
![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=streamthoughts_jikkou&metric=vulnerabilities)
![Coverage](https://sonarcloud.io/api/project_badges/measure?project=streamthoughts_jikkou&metric=coverage)

[![Slack Community](https://img.shields.io/badge/Slack-Join%20Community-c92760?logo=slack)](https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA)
[![Website](https://img.shields.io/badge/Wesite-Jikkou-c92760)](https://streamthoughts.github.io/jikkou/)

</p>

See: [official documentation](https://streamthoughts.github.io/jikkou/)

<img src="./assets/demo.gif" alt="Jikkou Demonstration"/>

## Do You Like This Project? ⭐

Please consider giving us a star ⭐ on GitHub. Your stars motivate us to persistently improve and help other developers discover our project. ![](https://img.shields.io/github/stars/streamthoughts/jikkou)

## Introduction

**[Jikkou](https://github.com/streamthoughts/jikkou)** (jikkō / 実行) is an open-source tool built to provide an efficient
and easy way to manage, automate, and provision resources on your event-stream platform.

Developed by Kafka ❤️, Jikkou aims to streamline daily operations on [Apache Kafka](https://kafka.apache.org/documentation/), ensuring that platform governance is no longer a boring and tedious task for both **Developers** and **Administrators**.

Jikkou enables a declarative management approach of **Topics**, **ACLs**, **Quotas**, **Schemas**, **Connectors** and even more with the use of YAML files called **_Resource Definitions_**.

Taking inspiration from `kubectl` and Kubernetes resource definition files, Jikkou offers an intuitive and user-friendly approach to configuration management.

<p style="text-align: center;">
  <img src="./docs/content/en/docs/Overview/jikkou-architecture-overview.png" alt="Jikkou Logo"/>
</p>

Jikkou can be used with [Apache Kafka](https://kafka.apache.org/), [Aiven](https://aiven.io/kafka), [MSK](https://aws.amazon.com/fr/msk/), [Confluent Cloud](https://www.confluent.io/confluent-cloud/), [Redpanda](https://redpanda.com/), etc.


## 🛠️ Installation

The latest stable release of jikkou (x86) for Linux, and macOS can be retrieved via https://sdkman.io/[SDKMan]:

```bash
sdk install jikkou
```

Alternatively, the latest stable release of jikkou (x86) for Linux, and macOS can be downloaded from  https://github.com/streamthoughts/jikkou/releases/latest[GitHub Releases]

Below are the convenience links for the base downloads of Jikkou.

| Platform | Link                                                                                                                                              |
|----------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| Linux    | ⬇️ [download (jikkou-0.35.0-linux-x86_64.zip)](https://github.com/streamthoughts/jikkou/releases/download/v0.35.0/jikkou-0.35.0-linux-x86_64.zip) |
| macOS    | ⬇️ [download (jikkou-0.35.0-osx-x86_64.zip)](https://github.com/streamthoughts/jikkou/releases/download/v0.35.0/jikkou-0.35.0-osx-x86_64.zip)     |

Download the jikkou binary from the [releases page](https://github.com/streamthoughts/jikkou/releases), uncompress and copy to the desired location.

```bash
# Download the latest stable release
wget https://github.com/streamthoughts/jikkou/releases/download/v0.35.0/jikkou-0.35.0-linux-x86_64.zip

# Uncompress
unzip jikkou-0.35.0-linux-x86_64.zip

# Copy to the desired location
cp jikkou-0.35.0-linux-x86_64/bin/jikkou $HOME/.local/bin
```

Finally, Jikkou is can also be retrieved :

* As a **fatJar** from [Maven Central](https://repo.maven.apache.org/maven2/io/streamthoughts/jikkou/0.35.0/)
* As a **Docker Image** from [Docker Hub](https://hub.docker.com/r/streamthoughts/jikkou).

Note, it is recommended to install the bash/zsh completion script `jikkou_completion`:

```bash
wget https://raw.githubusercontent.com/streamthoughts/jikkou/main/jikkou_completion . jikkou_completion
```

or alternatively, run the following command for generation the completion script.

```bash
source <(jikkou generate-completion)
```

  **WARNING:** If you are using macOS you may need to remove the quarantine attribute from the bits before you can use them To do this, run the following: `sudo xattr -r -d com.apple.quarantine path/to/jikkou/folder/`

## Overview

Here is an example of how to create and manage a _Kafka topic_ using Jikkou:

* Create a resource file _kafka-topics.yaml_:
kafka-topics.yaml:::

```yaml
# file:./kafka-topics.yaml
apiVersion: 'kafka.jikkou.io/v1beta2'
kind: 'KafkaTopic'
metadata:
  name: 'my-first-topic-with-jikkou'
  labels: {}
  annotations: {}
spec:
  partitions: 12
  replicas: 3
  configs:
    min.insync.replicas: 2
```

* Then run the following command:

```bash
$ jikkou apply --files ./kafka-topics.yaml
```

Jikkou will then take care of computing and applying the necessary changes directly to your cluster.

_(output)_:

```
TASK [CREATE] Create a new topic my-first-topic-with-jikkou (partitions=12, replicas=3) - CHANGED **********************
```

```json
{
  "changed" : true,
  "end" : 1634071489773,
  "resource" : {
    "name" : "my-first-topic-with-jikkou",
    "operation" : "ADD",
    "partitions" : {
      "after" : 12,
      "operation" : "ADD"
    },
    "replicas" : {
      "after" : 3,
      "operation" : "ADD"
    },
    "configs" : {
      "min.insync.replicas" : {
        "after" : "2",
        "operation" : "ADD"
      }
    }
  },
  "failed" : false,
  "status" : "CHANGED"
}
```

```
EXECUTION in 2s 661ms (DRY_RUN)
ok : 0, created : 1, altered : 0, deleted : 0 failed : 0
```

## Documentation

Check the official [documentation](https://jikkou.io) for further [installation](https://www.jikkou.io/docs/install/) and [use cases](https://www.jikkou.io/docs/tutorials/).

## 🏭 Developers

You need to have [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Docker](https://www.docker.com/) installed.

### Dependencies

Jikkou CLI is built with [Micronaut](https://micronaut.io/) and [Picocli](https://picocli.info/)

To build the project you will need:

* Java 21
* [GraalVM](https://www.graalvm.org/) 22.1.0 or newer to create native executable
* [TestContainer](https://testcontainers.com/) to run integration tests

### Build project

This project includes [Maven Wrapper](https://maven.apache.org/wrapper/).

Below are the commands commonly used to build the project:

```bash
# Build and run all tests
./mvnw clean verify

# Build and skip integration tests
./mvnw clean verify -DskipTests
```

### Build Docker Images (locally)

```bash
$ make
```

### Create Native Executable

```bash
# Build and run all tests
./mvnw clean verify -Pnative
```

You can then execute the native executable with: `./jikkou-cli/target/jikkou-$PROJECT_VERSION-runner`

### Create Debian Package (on Linux)

```bash
# Build and run all tests
./mvnw clean package -Pnative
./mvnw package -Pdeb
```

You can then install the package with: `sudo dpkg -i ./dist/jikkou-$PROJECT_VERSION-linux-x86_64.deb`

NOTE: Jikkou will install itself in the directory :  `/opt/jikkou`

### Formats

This project uses the Maven plugin [Spotless](https://github.com/diffplug/spotless/tree/master/plugin-maven)
to format all Java classes and to apply some code quality checks.

### Bugs

This project uses the Maven plugin [SpotBugs](https://spotbugs.github.io/) and [FindSecBugs](https://find-sec-bugs.github.io/)
to run some static analysis to look for bugs in Java code.

Reported bugs can be analysed using SpotBugs GUI:
```bash
$ ./mvnw spotbugs:gui
```

## 💡 Contributions

Any feedback, bug reports and PRs are greatly appreciated!

- **Source Code**: https://github.com/streamthoughts/jikkou
- **Issue Tracker**: https://github.com/streamthoughts/jikkou/issues

## Licence

This code base is available under the Apache License, version 2.
