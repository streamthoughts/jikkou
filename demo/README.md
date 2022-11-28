#  Jikkou : Demo

## Introduction

**https://github.com/streamthoughts/jikkou[Jikkou]** (jikkō / 実行) is an open-source tool to help you automate the
management of the configurations that live on your https://kafka.apache.org/documentation/[Apache Kafka] clusters.

## Prerequisites

* Java
* Docker & Docker Compose
* [yq](https://github.com/mikefarah/yq)
* [jq](https://stedolan.github.io/jq/)

## Getting Started

## Install Jikkou

**First, let's install latest Jikkou release**

```bash
sudo dpkg --remove jikkou && \
sudo rm -rf /usr/bin/jikkou && \
wget https://github.com/streamthoughts/jikkou/releases/download/v0.13.0/jikkou.deb && \
sudo dpkg -i jikkou.deb && \
source <(jikkou generate-completion) && \
jikkou --version
```

## Start Local Kafka 

**Then, Start a local Kafka environment (using Docker)**

```bash
wget https://github.com/streamthoughts/jikkou/raw/main/docker-compose.yml && \
wget https://github.com/streamthoughts/jikkou/raw/main/down && \
wget https://github.com/streamthoughts/jikkou/raw/main/up && \
chmod +x ./up && ./up
```

## Check Jikkou configuration

```bash
jikkou config get | jq
```

## Check if cluster is accessible

```bash
jikkou health | jq
```

## Describe Kafka Broker

```bash
jikkou brokers describe | yq
```

## Working with Kafka Topics 

### Create Some Topics

**Display Resource Definition**

```bash
cat ./resources/initial-topics.yaml | yq
```

**Create Kafka Topics**

```bash
jikkou topics \
  apply \
  --files ./resources/initial-topics.yaml \
  --include "jikkou-demo-.*" \
  --verbose \
  --yes
```

NOTE: Run the command above a second time to see the behavior of Jikkou

### Delete Topics

**Display Resource Definition**

```bash
cat ./resources/remove-topics.yaml | yq
```

**Delete Kafka Topics**

```bash
jikkou topics \
  apply \
  --files ./resources/remove-topics.yaml \
  --include "jikkou-demo-.*" \
  --delete-config-orphans \
  --delete-topic-orphans \
  --verbose \
  --yes
```

WARN: When working on a production environment, we highly recommend to always run the apply or delete command with the `--include` or `--exclude` options to make sure to not remove any topics by accident. Furthermore, always run your command in `--dry-run` mode to check for changes that will be executed by Jikkou before proceeding.

### Exploring the use of ConfigMap

**Display Resource Definition**

```bash
cat ./resources/topics-configmap.yaml | yq
```

**Validate Resource Definition**

```bash
jikkou validate \
--files ./resources/topics-configmap.yaml | yq
```

**Apply Resource Definition**
```bash
jikkou topics \
  apply \
  --files ./resources/topics-configmap.yaml \
  --include "jikkou-demo-.*" \
  --verbose \
  --yes
```

### Exploring the use of Templating

**Display Resource Definition**
```bash
cat ./resources/topics-template.tpl
```

**Display Values**
```bash
cat ./resources/topics-values.yaml | yq
```

**Validate Resource Definition**
```bash
jikkou validate \
  --files ./resources/topics-template.tpl \
  --values-files ./resources/topics-values.yaml | yq
```

**Apply Resource Definition**
```bash
TOPIC_PREFIX=demo jikkou topics \
  apply \
  --files ./resources/topics-template.tpl \
  --values-files ./resources/topics-values.yaml \
  --include ".*iot.*" \
  --verbose \
  --yes
```