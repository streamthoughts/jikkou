---
title: "Installing Jikkou"
linkTitle: "Installing Jikkou"
weight: 2
description: >
    This guide shows how to install the Jikkou CLI.
---

{{% pageinfo %}}
Jikkou can be installed either from source, or from releases.
{{% /pageinfo %}}

## From The Jikkou Project

### Releases

Every [`release`](https://github.com/streamthoughts/jikkou/releases) released versions of Jikkou is available: 

* As a zip/tar.gz package from [GitHub Releases](https://github.com/streamthoughts/jikkou/releases/tag/v0.17.0)
* As a fatJar available from [Maven Central](https://repo.maven.apache.org/maven2/io/streamthoughts/jikkou/0.17.0/)
* As a docker image available from [Docker Hub](https://hub.docker.com/r/streamthoughts/jikkou).
* As a Debian package from [GitHub Releases](https://github.com/streamthoughts/jikkou/releases/tag/v0.17.0)

These are the official ways to get Jikkou releases that you manually downloaded and installed.

#### Install From Tarball distribution

1. Download your desired [version](https://github.com/streamthoughts/jikkou/releases)
2. Unpack it (`tar -zxvf jikkou-0.18.0-runner.tar.gz`)
3. Move the unpacked directory to its desired destination (`mv jikkou-0.18.0-runner /opt`)
4. Add the executable to your PATH (`export PATH=$PATH:/opt/jikkou/bin`)

#### Install From Debian distribution

1. Download your desired [version](https://github.com/streamthoughts/jikkou/releases)
```bash
$ wget https://github.com/streamthoughts/jikkou/releases/download/0.18.0/jikkou.deb
$ sudo dpkg -i jikkou.deb
```

or just run the command:

```bash
curl -s https://raw.githubusercontent.com/streamthoughts/jikkou/main/get.sh | sh
```

From there, you should be able to run the client: `jikkou help`.

{{% alert title="Note" color="info" %}}
Jikkou will install itself in the directory :  `/opt/jikkou`
{{% /alert %}}

It is recommended to install the bash/zsh completion script `jikkou_completion`:

```bash
wget https://raw.githubusercontent.com/streamthoughts/jikkou/master/jikkou_completion . jikkou_completion
```

or alternatively, run the following command for generation the completion script.

```
$ source <(jikkou generate-completion)
```

## Development Builds

In addition to releases you can download or install development snapshots of Jikkou.

### From Docker Hub

Docker images are built and push to [Docker Hub](https://hub.docker.com/r/streamthoughts/jikkou) from the latest `main` branch. 
They are not official releases, and may not be stable. 
However, they offer the opportunity to test the cutting edge features.

```bash
$ docker run -it streamthoughts/jikkou:master
```

### From Source (Linux, macOS)

Building Jikkou from source is slightly more work, but is the best way to go if you want to test the latest (pre-release) Jikkou version.

#### Prerequisites

You must have a working Java environment with a JDK 17 (i.e. `$JAVA_HOME` environment variable is configured).

#### Build Tarball / Debian packages

```bash
$ git clone https://github.com/streamthoughts/jikkou.git
$ cd jikkou
$ ./mvnw clean package -DskipTests -Pdist
```

Then, distributions will be available in the `./dist` directory:
```
.
├── jikkou-<VERSION>-runner
│        └── jikkou-<VERSION>-runner
│            ├── bin
│            │       ├── jikkou
│            │       └── jikkou.bat
│            ├── etc
│            │       └── logback.xml
│            ├── jikkou_completion
│            ├── lib
│            │       └── jikkou-runner.jar
│            ├── LICENSE
│            └── README.adoc
├── jikkou-<VERSION>-runner.tar.gz
├── jikkou-<VERSION>-runner.zip
└── jikkou.deb
```

#### Build RPM

In addition to that, you may need to install Jikkou from an _RPM_ package. For doing that, you can run the following commands:

```bash
$ ./mvnw clean package -DskipTests -Prpm
```

The RPM package will available in the `./target/rpm/jikkou/RPMS/noarch/` directory.