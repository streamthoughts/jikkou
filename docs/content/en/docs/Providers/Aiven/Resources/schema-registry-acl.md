---
categories: [ ]
tags: [ "feature", "resources" ]
title: "ACL for Aiven Schema Registry"
linkTitle: "ACL for Aiven Schema Registry"
weight: 30
description: >
  Learn how to manage Access Control Lists (ACLs) in Aiven for Schema Registry
---

{{% pageinfo color="info" %}}
The `SchemaRegistryAclEntry` resources are used to manage the Access Control Lists in Aiven for Schema Registry. A
`SchemaRegistryAclEntry` resource defines the permission to be granted to a user for one or more Schema Registry
Subjects.
{{% /pageinfo %}}

## `SchemaRegistryAclEntry`

### Specification

Here is the _resource definition file_ for defining a `SchemaRegistryAclEntry`.

```yaml
---
apiVersion: "kafka.aiven.io/v1beta1"   # The api version (required)
kind: "SchemaRegistryAclEntry"         # The resource kind (required)
metadata:
  labels: { }
  annotations: { }
spec:
  permission: <>               # The permission. Accepted values are: READ, WRITE
  username: <>                 # The username
  resource: <>                 # The Schema Registry ACL entry resource name pattern
```

NOTE: The resource name pattern should be `Config:` or `Subject:<subject_name>` where `subject_name` must consist of
alpha-numeric characters, underscores, dashes, dots and glob characters `*` and `?`.

### Example

Here is an example that shows how to define a simple ACL entry using
the `SchemaRegistryAclEntry` resource type.

_`file: schema-registry-acl-entry.yaml`_

```yaml
---
apiVersion: "kafka.aiven.io/v1beta1"
kind: "SchemaRegistryAclEntry"
spec:
  permission: "READ"
  username: "Alice"
  resource: "Subject:*"
```

## `SchemaRegistryAclEntryList`

If you need to define multiple ACL entries (e.g. using a template), it may be easier to use
a `SchemaRegistryAclEntryList` resource.

### Specification

Here the _resource definition file_ for defining a `SchemaRegistryAclEntryList`.

```yaml
---
apiVersion: "kafka.aiven.io/v1beta1"    # The api version (required)
kind: "SchemaRegistryAclEntryList"      # The resource kind (required)
metadata: # (optional)
  labels: { }
  annotations: { }
items: [ ]                              # An array of SchemaRegistryAclEntry
```

### Example

Here is a simple example that shows how to define a single YAML file containing two ACL entry definitions using
the `SchemaRegistryAclEntryList` resource type.

```yaml
---
apiVersion: "kafka.aiven.io/v1beta1"
kind: "SchemaRegistryAclEntryList"
items:
  - spec:
      permission: "READ"
      username: "alice"
      resource: "Config:"
  - spec:
      permission: "WRITE"
      username: "alice"
      resource: "Subject:*"
```