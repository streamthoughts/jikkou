---
categories: []
tags: ["feature", "extensions"] 
title: "Extensions"
linkTitle: "Extensions"
weight: 80
description: >
  Learn how to extend Jikkou
---

## Extensions

Jikkou allows implementing and configuring extensions, i.e., _`Validation`_, and _`Transformer`_.

Jikkou's sources are available on[Maven Central]( https://mvnrepository.com/artifact/io.streamthoughts/jikkou)

**For Maven:**

```xml
<dependency>
    <groupId>io.streamthoughts</groupId>
    <artifactId>jikkou</artifactId>
    <version>${jikkou.version}</version>
</dependency>
```

**For Gradle:**
```text
implementation group: 'io.streamthoughts', name: 'jikkou', version: ${jikkou.version}
```

## Packaging

To make your extensions available to Jikkou, install them into one or many local directories.
Then, use the `jikkou.extension.paths` property to configure the list of locations from which the extensions will be loaded.

Each configured directories may contain:

* an uber JAR containing all the classes and third-party dependencies for the extensions.
* a directory containing all JARs for the extensions.
