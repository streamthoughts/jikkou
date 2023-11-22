---
title: "Package Extensions"
linkTitle: "Package Extensions"
weight: 1
description: >
  Learn how to package and install custom extensions for Jikkou.
---

## Packaging Extensions

You can extend Jikkou's capabilities by developing custom extensions and resources.

An extension must be developed in Java and packaged as a tarball or ZIP archive. The archive must contain a single
top-level directory containing the extension JAR files, as well as any resource files or third party libraries required
by your extensions. An alternative approach is to create an **uber-JAR** that contains all the extension's JAR files and
other resource files needed.

An *extension package* is more commonly described as an **Extension Provider**.

### Dependencies

Jikkou's sources are available on [Maven Central]( https://mvnrepository.com/artifact/io.streamthoughts/jikkou)

To start developing custom extension for Jikkou, simply add the Core library to your project's dependencies.

**For Maven:**

```xml

<dependency>
    <groupId>io.streamthoughts</groupId>
    <artifactId>jikkou-core</artifactId>
    <version>${jikkou.version}</version>
</dependency>
```

**For Gradle:**

```text
implementation group: 'io.streamthoughts', name: 'jikkou-core', version: ${jikkou.version}
```

### Extension Discovery

Jikkou uses the standard Java [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
mechanism to discover and registers custom extensions and resources. For this, you will need to the implement
the Service Provider Interface: `io.streamthoughts.jikkou.spi.ExtensionProvider`

```java
/**
 * <pre>
 * Service interface for registering extensions and resources to Jikkou at runtime.
 * The implementations are discovered using the standard Java {@link java.util.ServiceLoader} mechanism.
 *
 * Hence, the fully qualified name of the extension classes that implement the {@link ExtensionProvider}
 * interface must be added to a {@code META-INF/services/io.streamthoughts.jikkou.spi.ExtensionProvider} file.
 * </pre>
 */
public interface ExtensionProvider extends HasName, Configurable {

    /**
     * Registers the extensions for this provider.
     *
     * @param registry The ExtensionRegistry.
     */
    void registerExtensions(@NotNull ExtensionRegistry registry);

    /**
     * Registers the resources for this provider.
     *
     * @param registry The ResourceRegistry.
     */
    void registerResources(@NotNull ResourceRegistry registry);
}
```

### Recommendations

If you are using [Maven](https://maven.apache.org/) as project management tool, we recommended to use
the [Apache Maven Assembly Plugin](https://maven.apache.org/plugins/maven-assembly-plugin/) to package your extensions
as a tarball or ZIP archive.

Simply create an *assembly descriptor*  in your project as follows:

**src/main/assembly/package.xml**

```xml

<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.2.0 http://maven.apache.org/xsd/assembly-2.2.0.xsd">
    <id>package</id>
    <formats>
        <format>zip</format>
    </formats>
    <includeBaseDirectory>false</includeBaseDirectory>
    <fileSets>
        <fileSet>
            <directory>${project.basedir}</directory>
            <outputDirectory>${organization.name}-${project.artifactId}/doc</outputDirectory>
            <includes>
                <include>README*</include>
                <include>LICENSE*</include>
                <include>NOTICE*</include>
            </includes>
        </fileSet>
    </fileSets>
    <dependencySets>
        <dependencySet>
            <outputDirectory>${organization.name}-${project.artifactId}/lib</outputDirectory>
            <useProjectArtifact>true</useProjectArtifact>
            <useTransitiveFiltering>true</useTransitiveFiltering>
            <unpack>false</unpack>
            <excludes>
                <exclude>io.streamthoughts:jikkou-core</exclude>
            </excludes>
        </dependencySet>
    </dependencySets>
</assembly>
```

Then, configure the `maven-assembly-plugin` in the `pom.xml` file of your project:

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <configuration>
        <finalName>${organization.name}-${project.artifactId}-${project.version}</finalName>
        <appendAssemblyId>false</appendAssemblyId>
        <descriptors>
            <descriptor>src/assembly/package.xml</descriptor>
        </descriptors>
    </configuration>
    <executions>
        <execution>
            <id>make-assembly</id>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
        <execution>
            <id>test-make-assembly</id>
            <phase>pre-integration-test</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Finally, use the `mvn clean package` to build your project and create the archive.

## Installing Extension Providers

To install an *Extension Provider*, all you need to do is to unpacks the archive into a desired location (
e.g., `/usr/share/jikkou-extensions`).
Also, you should ensure that the archive's top-level directory name is unique, to prevent overwriting existing files or
extensions.

## Configuring Extension Providers

Custom extensions can be supplied to the Jikkou's API Server and Jikkou CLI (when running the Java Binary Distribution,
i.e., not the native version). For this, you simply need to configure the `jikkou.extension.paths` property. The
property accepts a list of paths from which to load extension providers.

Example for the Jikkou API Server:

```yaml
# application.yaml
jikkou:
  extension.paths:
    - /usr/share/jikkou-extensions
```

Once your extensions are configured you should be able to list your extensions using either :

* The Jikkou CLI: `jikkou api-extensions list` command, or
* The Jikkou API Server: `GET /apis/core.jikkou.io/v1/extensions -H "Accept: application/json"`
