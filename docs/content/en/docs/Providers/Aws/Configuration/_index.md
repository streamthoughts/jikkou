---
title: "Configuration"
linkTitle: "Configuration"
weight: 2
description: >
  Learn how to configure the extensions for AWS.
---

{{% pageinfo %}}
Here, you will find the list of resources supported by the extension for AWS.
{{% /pageinfo %}}

## Configuration

You can configure the properties to be used to connect the AWS services
through the Jikkou client configuration property `jikkou.providers`.

**Example:**

```hocon
jikkou {
  # AWS
  provider.aws {
    enabled = true
    config = {
      # The AWS S3 Region, e.g. us-east-1
      aws.client.region = ""
      # The AWS Access Key ID.
      aws.client.accessKeyId = ""
      # The AWS Secret Access Key.
      aws.client.secretAccessKey = ""
      # The AWS session token.
      aws.client.sessionToken = ""
      # The endpoint with which the SDK should communicate allowing you to use a different S3 compatible service
      aws.client.endpointOverride = ""
      # The name of the registries. Used only for lookup.
      aws.glue.registryNames = ""
    }
  }
}
```

