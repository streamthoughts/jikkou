---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Role Bindings for Confluent Cloud"
linkTitle: "Role Bindings for Confluent Cloud"
weight: 10
description: >
  Learn how to manage RBAC Role Bindings in Confluent Cloud.
---

{{% pageinfo color="info" %}}
The `RoleBinding` resources are used to manage RBAC role bindings in Confluent Cloud. A
`RoleBinding` resource defines which role is granted to a principal for a specific scope (identified by a CRN pattern).
{{% /pageinfo %}}

## `RoleBinding`

### Specification

Here is the _resource definition file_ for defining a `RoleBinding`.

```yaml
---
apiVersion: "iam.confluent.cloud/v1"    # The api version (required)
kind: "RoleBinding"                     # The resource kind (required)
metadata:
  labels: { }
  annotations: { }
spec:
  principal: <>              # The principal (e.g., User:sa-abc123 or User:u-xyz789)
  roleName: <>               # The role name (e.g., CloudClusterAdmin, DeveloperRead)
  crnPattern: <>             # The Confluent Resource Name pattern (e.g., crn://confluent.cloud/...)
```

### Fields

| Field         | Type   | Required | Description                                                                 |
|---------------|--------|----------|-----------------------------------------------------------------------------|
| `principal`   | String | Yes      | The principal. Pattern: `User:<user-id>` or `Group:<group-name>`.           |
| `roleName`    | String | Yes      | The role to bind. See [Confluent Cloud RBAC roles](https://docs.confluent.io/cloud/current/security/access-control/rbac/predefined-rbac-roles.html). |
| `crnPattern`  | String | Yes      | The Confluent Resource Name (CRN) pattern defining the scope of the binding. |

### Common Role Names

| Role                     | Description                                    |
|--------------------------|------------------------------------------------|
| `OrganizationAdmin`      | Full access to the organization.               |
| `EnvironmentAdmin`       | Full access to an environment.                 |
| `CloudClusterAdmin`      | Full access to a Kafka cluster.                |
| `DeveloperManage`        | Manage topics and schemas.                     |
| `DeveloperRead`          | Read from topics and view schemas.             |
| `DeveloperWrite`         | Write to topics and manage schemas.            |
| `ResourceOwner`          | Full access to a specific resource.            |

### Example

Here is a simple example that shows how to define a single role binding using
the `RoleBinding` resource type.

_`file: role-binding.yaml`_

```yaml
---
apiVersion: "iam.confluent.cloud/v1"
kind: "RoleBinding"
metadata:
  labels: { }
  annotations: { }
spec:
  principal: "User:sa-abc123"
  roleName: "CloudClusterAdmin"
  crnPattern: "crn://confluent.cloud/organization=org-123/environment=env-456/cloud-cluster=lkc-789"
```

### Usage

```bash
# List all role bindings
jikkou get ccloud-rbs

# Apply role bindings from a file
jikkou apply --files ./role-binding.yaml

# Delete orphan role bindings not defined in the file
jikkou apply --files ./role-binding.yaml -o delete-orphans=true

# Dry-run to preview changes without applying
jikkou diff --files ./role-binding.yaml
```

### Metadata Labels

When listing role bindings, Jikkou automatically enriches each resource with metadata labels
to help identify principals:

| Label                              | Description                                          |
|------------------------------------|------------------------------------------------------|
| `confluent.cloud/principal-name`   | The display name of the user or service account.     |
| `confluent.cloud/principal-email`  | The email of the user (not set for service accounts). |

**Example output:**

```yaml
apiVersion: "iam.confluent.cloud/v1"
kind: "RoleBinding"
metadata:
  labels:
    confluent.cloud/principal-name: "Florian Hussonnois"
    confluent.cloud/principal-email: "florian@example.com"
  annotations:
    confluent.cloud/role-binding-id: "rb-NBl9kE"
spec:
  principal: "User:u-rrnm2g9"
  roleName: "OrganizationAdmin"
  crnPattern: "crn://confluent.cloud/organization=d497af93-23f5-434a-a008-0547797be410"
```

## `RoleBindingList`

If you need to define multiple role bindings (e.g., using a template), it may be easier to use a `RoleBindingList` resource.

### Specification

Here is the _resource definition file_ for defining a `RoleBindingList`.

```yaml
---
apiVersion: "iam.confluent.cloud/v1"    # The api version (required)
kind: "RoleBindingList"                 # The resource kind (required)
metadata: # (optional)
  labels: { }
  annotations: { }
items: [ ]                             # An array of RoleBinding
```

### Example

```yaml
---
apiVersion: "iam.confluent.cloud/v1"
kind: "RoleBindingList"
items:
  - spec:
      principal: "User:sa-abc123"
      roleName: "CloudClusterAdmin"
      crnPattern: "crn://confluent.cloud/organization=org-123/environment=env-456/cloud-cluster=lkc-789"
  - spec:
      principal: "User:sa-abc123"
      roleName: "DeveloperRead"
      crnPattern: "crn://confluent.cloud/organization=org-123/environment=env-456/cloud-cluster=lkc-789/kafka=lkc-789/topic=my-topic"
  - spec:
      principal: "User:u-xyz789"
      roleName: "OrganizationAdmin"
      crnPattern: "crn://confluent.cloud/organization=org-123"
```

> **Note:** Role bindings are immutable in the Confluent Cloud API. If you need to change a role binding, delete the old one and create a new one. Jikkou only supports `CREATE` and `DELETE` operations (no `UPDATE`).
