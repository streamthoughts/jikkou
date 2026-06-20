---
title: "Building from Source"
linkTitle: "Building from Source"
weight: 10
description: >
    How to set up your environment to build and test Jikkou itself.
---

{{% pageinfo %}}
This guide is for contributors who want to build and test the Jikkou project itself. If you are looking to develop your own Jikkou extensions or use the Java API, see the [Developer Guide]({{% relref "/docs/Developer Guide" %}}) instead.
{{% /pageinfo %}}

## Prerequisites
* Java 25 (see https://sdkman.io/ for installing java locally)
* Git
* [Docker](https://docs.docker.com/get-docker/) and [Docker-Compose](https://docs.docker.com/compose/install/)
* Your favorite IDE


## Building Jikkou

We use [Maven Wrapper](https://maven.apache.org/wrapper/) to build our project. The simplest way to get started is:

For building distribution files.

```bash
$ ./mvnw clean package -Pdist -DskipTests
```

Alternatively, we also use Make to package and build the Docker image for Jikkou:

```bash
$ make
```

## Running tests

For running all tests and checks:

```bash
$ ./mvnw clean verify
```

### Code Format

This project uses the Maven plugin [Spotless](https://github.com/diffplug/spotless/tree/master/plugin-maven)
to format all Java classes and to apply some code quality checks.

### Bugs & Security

This project uses the Maven plugin [SpotBugs](https://spotbugs.github.io/) and [FindSecBugs](https://find-sec-bugs.github.io/)
to run some static analysis to look for bugs in Java code.

Reported bugs can be analysed using SpotBugs GUI:

```bash
$ ./mvnw spotbugs:gui
```
