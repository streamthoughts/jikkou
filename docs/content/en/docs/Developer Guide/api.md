---
title: "JikkouApi"
linkTitle: "JikkouApi"
weight: 2
description: >
  Learn how to use the Jikkou programmatically
---

{{% pageinfo color="info" %}}
Jikkou is not only a CLI but also a Java library that you can use internally in your project.
{{% /pageinfo %}}

## Examples

### Create Kafka Topics

The example below shows how to use the `JikkouApi` to create Kafka Topics from resource description file.

You will need to add the following dependency in the `pom.xml` file of your project.

```xml
<dependency>
    <groupId>io.streamthoughts</groupId>
    <artifactId>jikkou-extension-kafka</artifactId>
    <version>${jikkou.version}</version>
</dependency>
```

```java
public class CreateKafkaTopicsExample {

    public static void main(String[] args) {

        // (1) Register Kinds
        ResourceDeserializer.registerKind(V1KafkaTopic.class);
        ResourceDeserializer.registerKind(V1KafkaTopicList.class);

        // (2) Load Resources
        HasItems resources = ResourceLoader.create().load(List.of("./kafka-topics.yaml"));

        // (3) Create and configure Jikkou API
        AdminClientContext clientContext = new AdminClientContext(
                () -> AdminClient.create(Map.of("bootstrap.servers", "localhost:9092"))
        );
        try (JikkouApi api = DefaultApi.builder()
                .withCollector(new AdminClientKafkaTopicCollector(clientContext))
                .withController(new AdminClientKafkaTopicController(clientContext))
                .build()) {

            // (4) Execute Reconciliation
            List<ChangeResult<Change>> changes = api.apply(
                    resources,
                    ReconciliationMode.CREATE,
                    ReconciliationContext.with(false) // dry-run
            );

            // (5) Do something with changes
            System.out.println(changes);
        }
    }
}
```



