---
title: "Releases v0.32.0"
linkTitle: "Releases v0.32.0"
weight: 32
---

## Jikkou 0.32.0: Moving Beyond Apache Kafka. Introducing new features: Extension Providers, Actions, etc.!

I’m thrilled to announce the release of Jikkou [0.32.0](https://github.com/streamthoughts/jikkou/releases/tag/v0.32.0)
which packs two major features: **External Extension Providers** and **Actions**. 🙂

### Highlights: What’s new in Jikkou 0.32.0?

* New **External Extension Provider** mechanism to extend Jikkou features.

* New extension type **Action** to execute specific operations against resources.

* New action for resetting consumer group offsets.

* New action for restarting connector and tasks for Kafka Connect.

* New option selector-match to exclude/include resources from being returned or reconciled by Jikkou.

* New API to get resources by their name.

## Extension Providers

Jikkou is a project that continues to reinvent and redefine itself with each new version. Initially developed
exclusively to manage the configuration of Kafka topics, it can now be used to manage Schema Registries, Kafka Connect
connectors, and more. But, the funny thing is that Jikkou isn’t coupled with Kafka. It was designed around a concept of
**pluggable extensions** that enable new capabilities and kind of resources to be seamlessly added to the project. For
this, Jikkou uses
the [Java Service Loader](https://docs.oracle.com/javase%2F9%2Fdocs%2Fapi%2F%2F/java/util/ServiceLoader.html) mechanism
to automatically discover new extensions at runtime.

Unfortunately, until now there has been no official way of using this mechanism with Jikkou CLI or Jikkou API Server.
For this reason, Jikkou 0.32.0 brings the capability to easily configuration external extensions.

So how does it work? Well, let’s imagine you want to be able to load Text Files from the local filesystem using Jikkou.

First, we need to create a new Java project and add the Jikkou Core library to your project’s dependencies (
`io.streamthoughts:jikkou-core:0.32.0 dependency`).

Then, you will need to create some POJO classes to represent your resource (e.g., V1File.class) and to implement the
Collector interface :

```java

@SupportedResource(type = V1File.class)
@ExtensionSpec(
        options = {
                @ExtensionOptionSpec(
                        name = "directory",
                        description = "The absolute path of the directory from which to collect files",
                        type = String.class,
                        required = true
                )
        }
)
@Description("FileCollector allows listing all files in a given directory.")
public final class FileCollector
        extends ContextualExtension
        implements Collector<V1File> {
    private static final Logger LOG = LoggerFactory.getLogger(FileCollector.class);

    @Override
    public ResourceListObject<V1File> listAll(@NotNull Configuration configuration,
                                              @NotNull Selector selector) {

        // Get the 'directory' option.
        String directory = extensionContext().<String>configProperty("directory").get(configuration);

        // Collect all files.
        List<V1File> objects = Stream.of(new File(directory).listFiles())
                .filter(file -> !file.isDirectory())
                .map(file -> {
                    try {
                        Path path = file.toPath();
                        String content = Files.readString(path);
                        V1File object = new V1File(ObjectMeta
                                .builder()
                                .withName(file.getName())
                                .withAnnotation("system.jikkou.io/fileSize", Files.size(path))
                                .withAnnotation("system.jikkou.io/fileLastModifier", Files.getLastModifiedTime(path))
                                .build(),
                                new V1FileSpec(content)
                        );
                        return Optional.of(object);
                    } catch (IOException e) {
                        LOG.error("Cannot read content from file: {}", file.getName(), e);
                        return Optional.<V1File>empty();
                    }
                })
                .flatMap(Optional::stream)
                .toList();
        ObjectMeta objectMeta = ObjectMeta
                .builder()
                .withAnnotation("system.jikkou.io/directory", directory)
                .build();
        return new DefaultResourceListObject<>("FileList", "system.jikkou.io/v1", objectMeta, objects);
    }
}
```

Next, you will need to implement the ExtensionProvider interface to register both your extension and your resource kind.

```java
public final class FileExtensionProvider implements ExtensionProvider {

    /**
     * Registers the extensions for this provider.
     *
     * @param registry The ExtensionRegistry.
     */
    public void registerExtensions(@NotNull ExtensionRegistry registry) {
        registry.register(FileCollector.class, FileCollector::new);
    }

    /**
     * Registers the resources for this provider.
     *
     * @param registry The ResourceRegistry.
     */
    public void registerResources(@NotNull ResourceRegistry registry) {
        registry.register(V1File.class);
    }
}
```

Then, the fully qualified name of the class must be added to the resource file
META-INF/service/io.streamthoughts.jikkou.spi.ExtensionProvider.

Finally, all you need to do is to package your project as a tarball or ZIP archive. The archive must contain a single
top-level directory containing the extension JAR files, as well as any resource files or third-party libraries required
by your extensions.

To install your *Extension Provider*, all you need to do is to unpacks the archive into a desired location (e.g.,
/usr/share/jikkou-extensions) and to configure the Jikkou’s API Server or Jikkou CLI (when running the Java Binary
Distribution, i.e., not the native version) with the jikkou.extension.paths property (e.g.,
jikkou.extension.paths=/usr/share/jikkou-extensions). For people who are familiar with how Kafka Connect works, it’s
more or less the same mechanism.

*(The full code source of this example is available
on [GitHub](https://github.com/streamthoughts/jikkou-extension-provider-file)).*

And that’s it! 🙂

**Extension Providers** open up the possibility of infinitely expanding Jikkou to manage your own resources, and use it
with systems other than Kafka.

## Actions

Jikkou uses a declarative approach to manage the asset state of your data infrastructure using resource descriptors
written in YAML. But sometimes, ops and development teams may need to perform specific operations on their resources
that cannot be included in their descriptor files. For example, you may need to reset offsets for one or multiple Kafka
Consumer Groups, restart failed connectors and tasks for Kafka Connect, etc. So instead of having to switch from one
tool to another, why not just use Jikkou for this?

Well, to solve that need, Jikkou 0.32.0 introduces a new type of extension called “**Actions**” that allows users to
perform specific operations on resources.

Combined with the **External Extension Provider** mechanism, you can now implement the Action interface to add custom
operations to Jikkou.

```java

@Category(ExtensionCategory.ACTION)
public interface Action<T extends HasMetadata> extends HasMetadataAcceptable, Extension {

    /**
     * Executes the action.
     *
     * @param configuration The configuration
     * @return The ExecutionResultSet
     */
    @NotNull
    ExecutionResultSet<T> execute(@NotNull Configuration configuration);
}
```

Actions are fully integrated with Jikkou API Server through the new Endpoint: /api/v1/actions/{name}/execute{?[options]

## Kafka Consumer Groups

### Altering Consumer Group Offsets

Jikkou 0.32.0 introduces the new KafkaConsumerGroupsResetOffsets action allows resetting offsets of consumer groups.

Here is an example showing how to reset the group my-group to the earliest offsets for topic test.

```bash
$ jikkou action kafkaconsumergroupresetoffsets execute \
--group my-group \
--topic test \
--to-earliest
```

**(output)**

```yaml
kind: "ApiActionResultSet"
apiVersion: "core.jikkou.io/v1"
metadata:
  labels: { }
  annotations:
    configs.jikkou.io/to-earliest: "true"
    configs.jikkou.io/group: "my-group"
    configs.jikkou.io/dry-run: "false"
    configs.jikkou.io/topic:
      - "test"
results:
  - status: "SUCCEEDED"
    errors: [ ]
    data:
      apiVersion: "kafka.jikkou.io/v1beta1"
      kind: "KafkaConsumerGroup"
      metadata:
        name: "my-group"
        labels:
          kafka.jikkou.io/is-simple-consumer: false
        annotations: { }
      status:
        state: "EMPTY"
        members: [ ]
        offsets:
          - topic: "test"
            partition: 1
            offset: 0
        coordinator:
          id: "101"
          host: "localhost"
          port: 9092
```

This action is pretty similar to the kafka-consumer-group script that ships with Apache Kafka. You can use it to reset a
consumer group to the earliest or latest offsets, to a specific datetime or specific offset.

In addition, it can be executed in a dry-run.

### Deleting Consumer Groups

You can now add the core annotation jikkou.io/delete to a KafkaConsumerGroup resource to mark it for deletion:

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta1"
kind: "KafkaConsumerGroup"
metadata:
  name: "my-group"
  labels:
    kafka.jikkou.io/is-simple-consumer: false
  annotations:
    jikkou.io/delete: true
```

```bash
$ jikkou delete --files my-consumer-group.yaml -o wide

TASK [DELETE] Delete consumer group 'my-group' - CHANGED ************************************************
{
  "status" : "CHANGED",
  "changed" : true,
  "failed" : false,
  "end" : 1701162781494,
  "data" : {
    "apiVersion" : "core.jikkou.io/v1beta2",
    "kind" : "GenericResourceChange",
    "metadata" : {
      "name" : "my-group",
      "labels" : {
        "kafka.jikkou.io/is-simple-consumer" : false
      },
      "annotations" : {
        "jikkou.io/delete" : true,
        "jikkou.io/managed-by-location" : "./my-consumer-group.yaml"
      }
    },
    "change" : {
      "before" : {
        "apiVersion" : "kafka.jikkou.io/v1beta1",
        "kind" : "KafkaConsumerGroup",
        "metadata" : {
          "name" : "my-group",
          "labels" : {
            "kafka.jikkou.io/is-simple-consumer" : false
          },
          "annotations" : { }
        }
      },
      "operation" : "DELETE"
    }
  },
  "description" : "Delete consumer group 'my-group'"
}
EXECUTION in 64ms 
ok : 0, created : 0, altered : 0, deleted : 1 failed : 0
```

## Kafka Connect

### Restarting Connector and Tasks

This new release also packs with the new action KafkaConnectRestartConnectors action allows a user to restart all or
just the failed Connector and Task instances for one or multiple named connectors.

Here are a few examples from the documentation:

* **Restarting all connectors for all Kafka Connect clusters.**

```bash
  $ jikkou action kafkaconnectrestartconnectors execute
```

```yaml    
---
kind: "ApiActionResultSet"
apiVersion: "core.jikkou.io/v1"
metadata:
labels: {}
annotations: {}
results:
- status: "SUCCEEDED"
  data:
    apiVersion: "kafka.jikkou.io/v1beta1"
    kind: "KafkaConnector"
    metadata:
      name: "local-file-sink"
      labels:
        kafka.jikkou.io/connect-cluster: "my-connect-cluster"
      annotations: {}
    spec:
      connectorClass: "FileStreamSink"
      tasksMax: 1
      config:
        file: "/tmp/test.sink.txt"
        topics: "connect-test"
      state: "RUNNING"
      status:
        connectorStatus:
        name: "local-file-sink"
        connector:
        state: "RUNNING"
        workerId: "connect:8083"
        tasks:
          - id: 0
           state: "RUNNING"
            workerId: "connect:8083"
```

* **Restarting a specific connector and tasks for on Kafka Connect cluster**

```bash
  $ jikkou action kafkaconnectrestartconnectors execute \
  --cluster-name my-connect-cluster
  --connector-name local-file-sink \
  --include-tasks
```

## New Selector Matching Strategy

Jikkou CLI allows you to provide one or multiple *selector expressions *in order to include or exclude resources from
being returned or reconciled by Jikkou. In previous versions, selectors were cumulative, so resources had to match all
selectors to be returned. Now, you can specify a *selector matching strategy* to determine how expressions must be
combined using the option --selector-match=[ANY|ALL|NONE].

* ALL: A resource is selected if it matches all selectors.

* ANY: A resource is selected if it matches one of the selectors.

* NONE: A resource is selected if it matches none of the selectors.

For example, the below command will only return topics matching the regex ^__.* or having a name equals to _schemas.

```bash
$ jikkou get kafkatopics \
--selector 'metadata.name MATCHES (^__connect-*)'
--selector 'metadata.name IN (_schemas)'
--selector-match ANY
```

## New Get Resource by Name

In some cases, it may be necessary to retrieve only a specific resource for a specific name. In previous versions, the
solution was to use selectors. Unfortunately, this approach isn’t very efficient, as it involves retrieving all the
resources and then filtering them. To start solving that issue, Jikkou v0.32.0 adds a new API to retrieve a resource by
its name.

**Example (using Jikkou CLI):**

```bash
$ jikkou get kafkatopics --name _schemas
```

**Example (using Jikkou API Server):**

```bash
$ curl -sX GET \
http://localhost:28082/apis/kafka.jikkou.io/v1/kafkatopics/_schemas \
-H 'Accept:application/json'
```

> Note : Currently not all resources have been updated to use that new API, so it’s possible that selectors are used as
> a default implementation by internal Jikkou API.

