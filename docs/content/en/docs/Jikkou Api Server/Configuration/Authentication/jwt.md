---
title: "JWT"
linkTitle: "JWT"
weight: 3
description: >
  Learn how to secure Jikkou API Server using JWT (JSON Web Token) Authentication.
---

Jikkou API Server can be secured using **JWT (JSON Web Token) Authentication**.

## Configure JWT

### Step1: Set JWT signature secret

Add the following configuration to your server configuration.

```yaml
# ./etc/application.yaml
micronaut:
  security:
    enabled: true
    authentication: bearer <1>
    token:
      enabled: true
      jwt:
        signatures:
          secret:
            generator:
              secret: ${JWT_GENERATOR_SIGNATURE_SECRET:pleaseChangeThisSecretForANewOne} <2>
```

* **<1>** Set authentication to bearer to receive a JSON response from the login endpoint.
* **<2>** Change this to your own secret and keep it safe (do not store this in your VCS).

### Step2: Generate a Token

Generate a valid JSON Web Token on `https://jwt.io/` using your secret.

Example with `pleaseChangeThisSecretForANewOne` as signature secret.

```bash
TOKEN=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.6cD3MnZmX2xyEAWyh-GgGD11TX8SmvmHVLknuAIJ8yE
```

### Step3: Validate authentication

```bash
$ curl -I -X GET http://localhost:28082/apis/kafka.jikkou.io/v1beta2/kafkabrokers \
-H "Accept: application/json" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.6cD3MnZmX2xyEAWyh-GgGD11TX8SmvmHVLknuAIJ8yE"

HTTP/1.1 200 OK
Content-Type: application/hal+json
content-length: 576
```