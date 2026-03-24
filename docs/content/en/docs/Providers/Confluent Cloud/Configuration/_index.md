---
title: "Configuration"
linkTitle: "Configuration"
weight: 2
description: >
  Learn how to configure the extensions for Confluent Cloud.
---

{{% pageinfo %}}
Here, you will find the configuration for the Confluent Cloud extension.
{{% /pageinfo %}}

## Configuration

You can configure the properties to connect to Confluent Cloud
through the Jikkou client configuration property `jikkou.provider.confluent-cloud`.

**Example:**

```hocon
jikkou {
  provider.confluent-cloud {
    enabled = true
    type = io.streamthoughts.jikkou.extension.confluent.ConfluentCloudExtensionProvider
    config = {
      # URL to the Confluent Cloud REST API (default: https://api.confluent.cloud)
      apiUrl = "https://api.confluent.cloud"
      # Confluent Cloud API Key (must be a Cloud API Key, not a Cluster API Key)
      apiKey = ${CONFLUENT_CLOUD_API_KEY}
      # Confluent Cloud API Secret
      apiSecret = ${CONFLUENT_CLOUD_API_SECRET}
      # CRN pattern used to scope role binding list operations
      crnPattern = ${CONFLUENT_CLOUD_CRN_PATTERN}
      # Enable debug logging (default: false)
      debugLoggingEnabled = false
    }
  }
}
```

### Configuration Properties

| Property             | Type    | Required | Default                        | Description                                                      |
|----------------------|---------|----------|--------------------------------|------------------------------------------------------------------|
| `apiUrl`             | String  | No       | `https://api.confluent.cloud`  | URL to the Confluent Cloud REST API.                             |
| `apiKey`             | String  | Yes      |                                | Cloud API Key. Must be a **Cloud API Key**, not a Cluster API Key. |
| `apiSecret`          | String  | Yes      |                                | Cloud API Secret.                                                |
| `crnPattern`         | String  | Yes      |                                | CRN pattern to scope role binding list operations.               |
| `debugLoggingEnabled`| Boolean | No       | `false`                        | Enable debug logging for REST API calls.                         |

### Creating a Cloud API Key

Cloud API Keys can be created using the Confluent Cloud CLI:

```bash
confluent api-key create --resource cloud --description "Jikkou role binding management"
```

> **Important:** You must use a **Cloud API Key** (organization-level), not a Cluster API Key. Cluster API Keys will result in a `401 Unauthorized` error.

### CRN Pattern

The `crnPattern` property is required and scopes all list operations to a specific part of your organization hierarchy. Examples:

| Scope             | CRN Pattern                                                                      |
|-------------------|----------------------------------------------------------------------------------|
| Organization      | `crn://confluent.cloud/organization=org-abc123`                                  |
| Environment       | `crn://confluent.cloud/organization=org-abc123/environment=env-def456`           |
| Kafka Cluster     | `crn://confluent.cloud/organization=org-abc123/environment=env-def456/cloud-cluster=lkc-789` |
