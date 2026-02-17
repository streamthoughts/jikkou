---
title: "Install Jikkou"
linkTitle: "Install"
weight: 2
description: >
  This guide shows how to install the Jikkou CLI.
menu:
  main:
    weight: 20
    pre: <i class='fa-solid fa-cog'></i>
---

{{% pageinfo %}}
Jikkou can be installed either from source, or from releases.
{{% /pageinfo %}}

## From SDKMan! (recommended)

The latest stable release of jikkou (x86) for Linux, and macOS can be retrieved via [SDKMan!](https://sdkman.io/):

```bash
sdk install jikkou
```

## From The Jikkou Project

### Releases

Every [`release`](https://github.com/streamthoughts/jikkou/releases) released versions of Jikkou is available:

* As a zip/tar.gz package from [GitHub Releases](https://github.com/streamthoughts/jikkou/releases) (for Linux, MacOS)
* As a fatJar available from [Maven Central](https://repo.maven.apache.org/maven2/io/streamthoughts/jikkou-cli/)
* As a docker image available from [Docker Hub](https://hub.docker.com/r/streamthoughts/jikkou).

These are the official ways to get Jikkou releases that you manually downloaded and installed.

#### Install From Release distribution

1. Download your desired [version](https://github.com/streamthoughts/jikkou/releases)
2. Unpack it (`unzip jikkou-<version>-linux-x86_64.zip`)
3. Move the unpacked directory to the desired destination (`mv jikkou-<version>-linux-x86_64 /opt/jikkou`)
4. Add the executable to your PATH (`export PATH=$PATH:/opt/jikkou/bin`)

From there, you should be able to run the client: `jikkou help`.

It is recommended to install the bash/zsh completion script `jikkou_completion`:

```bash
wget https://raw.githubusercontent.com/streamthoughts/jikkou/main/jikkou_completion -O jikkou_completion
```

or alternatively, run the following command for generation the completion script.

```
$ source <(jikkou generate-completion)
```

#### Using Docker Image

```bash
# Create a Jikkou configfile (i.e., jikkouconfig)
cat << EOF >jikkouconfig
{
  "currentContext" : "localhost",
  "localhost" : {
    "configFile" : null,
    "configProps" : {
     "provider.kafka.config.client.bootstrap.servers" : "localhost:9092"
    }
  }
}
EOF

# Run Docker
docker run -it \
--net host \
--mount type=bind,source="$(pwd)"/jikkouconfig,target=/etc/jikkou/config \
streamthoughts/jikkou:latest -V
```

## Development Builds

In addition to releases you can download or install development snapshots of Jikkou.

### From Docker Hub

Docker images are built and push to [Docker Hub](https://hub.docker.com/r/streamthoughts/jikkou) from the latest `main`
branch.
They are not official releases, and may not be stable.
However, they offer the opportunity to test the cutting edge features.

```bash
$ docker run -it streamthoughts/jikkou:main
```

### From Source (Linux, macOS)

Building Jikkou from source is slightly more work, but is the best way to go if you want to test the latest (
pre-release) Jikkou version.

#### Prerequisites

To build the project you will need:

* Java 25 (i.e. `$JAVA_HOME` environment variable is configured).
* [GraalVM](https://www.graalvm.org/) 25.0.2 or newer to create native executable
* [TestContainer](https://testcontainers.com) to run integration tests

#### Create Native Executable

```bash
# Build and run all tests
./mvnw clean verify -Pnative
```

You can then execute the native executable with: `./jikkou-cli/target/jikkou-$PROJECT_VERSION-runner`

#### Build Debian Package (.deb)

```bash
# Build and run all tests
./mvnw clean package -Pnative
./mvnw package -Pdeb
```

You can then install the package with: `sudo dpkg -i ./dist/jikkou-$PROJECT_VERSION-linux-x86_64.deb`

NOTE: Jikkou will install itself in the directory :  `/opt/jikkou`

#### Build RPM Package

```bash
# Build and run all tests
./mvnw clean package -Pnative
./mvnw package -Prpm
```

The RPM package will available in the `./target/rpm/jikkou/RPMS/noarch/` directory.