---
title: "Basic Auth"
linkTitle: "Basic Auth"
weight: 3
description: >
  Learn how to secure Jikkou API Server using Basic HTTP Authentication Scheme.
---

Jikkou API Server can be secured using a **Basic HTTP Authentication Scheme**.

[RFC7617](https://datatracker.ietf.org/doc/html/rfc7617) defines the "Basic" Hypertext Transfer Protocol (HTTP)
authentication scheme, which transmits credentials as user-id/password pairs, encoded using Base64.

Basic Authentication should be used over a secured connection using HTTPS.

## Configure Basic HTTP Authentication

### Step1: Enable security

Add the following configuration to your server configuration.

```yaml
# ./etc/application.yaml
micronaut:
  security:
    enabled: true
```

### Step2: Configure the list of users

The list of `username/password` authorized to connect to the API server can be configured as follows:

```yaml
# ./etc/application.yaml
jikkou:
  security:
    basic-auth:
      - username: "admin"
        password: "{noop}password"
```

For production environment, *password* must not be configured in plaintext. *Password* can be passed encoded
in `bcrypt`, `scrypt`, `argon2`, and `sha256`.

#### Example

```bash
echo -n password | sha256sum
5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8
```

```yaml
# ./etc/application.yaml
jikkou:
  security:
    basic-auth:
      - username: "admin"
        password: "{sha256}5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
```

### Step3: Validate authentication

**Encode credentials**
```bash
echo -n "admin:password" | base64 
YWRtaW46cGFzc3dvcmQ=
```
**Send 
request**
```bash
curl -IX GET http://localhost:28082/apis/kafka.jikkou.io/v1beta2/kafkabrokers \
-H "Accept: application/json" \
-H "Authorization: Basic YWRtaW46cGFzc3dvcmQ"

HTTP/1.1 200 OK
Content-Type: application/hal+json
content-length: 576
```