---
title: "Authentication"
linkTitle: "Authentication"
weight: 3
description: >
  Learn how to secure access to Jikkou API server.
---

## Enable Security

To enable secure access to the API Server:

### Configuration File

Update the configuration file (i.e., `application.yaml`) of the server with:

```yaml
micronaut:
  security:
    enabled: true
```

### Environment Variable

As an alternative, you can set the following environment variable `MICRONAUT_SECUTIRY_ENABLED=true`.


{{% alert title="Note" color="info" %}}
For more information about how Micronaut binds environment variables and configuration property: https://docs.micronaut.io/latest/guide/index.html#_property_value_binding).
{{% /alert %}}

## Unauthorized Access

When accessing a secured path, the server will return the following response if access is not authorized:

```json
{
  "message": "Unauthorized",
  "errors": [
    {
      "status": 401,
      "error_code": "authentication_user_unauthorized",
      "message": "Unauthorized"
    }
  ]
}

```


