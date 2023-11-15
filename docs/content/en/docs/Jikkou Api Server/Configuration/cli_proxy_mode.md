---
title: "CLI Proxy Mode"
linkTitle: "CLI Proxy Mode"
weight: 4
description: >
  Learn how to configure Jikkou CLI in proxy mode.
---

## Configuration

### Step 1: Enable Proxy Mode

To enable proxy mode so that the CLI communicates directly with your API Server, add the following parameters to your
configuration:

```hocon
jikkou {
  # Proxy Configuration
  proxy {
    # Specify whether proxy mode is enabled (default: false).
    enabled = true
    # URL of the API Server
    url = "http://localhost:28082"
    # Specifcy whether HTTP request debugging should be enabled (default: false)
    debugging = false
    # The connect timeout in millisecond (if not configured used ` default-timeout` ).
    connect-timeout = 10000
    # The read timeout in millisecond (if not configured used ` default-timeout` ).
    read-timeout = 10000
    # The write timeout in millisecond (if not configured used ` default-timeout` ).
    write-timeout = 10000
    # The default timeout (i.e., for read/connect) in millisecond (default: 10000)
    default-timeout = 10000
    # Security settings to authenticate to the API Server.
    security = {
      # For Token based Authentication.
      # access-token = ""
      # For Username/Password Basic-Authentication.
      # basic-auth = {
      #   username = ""
      #   password = ""
      # }
    }
  }
}
```

### Step 2: Check connection

When enabling Proxy Mode, Jikkou CLI provides the additional command `server-info`. You can use it to verify the
connectivity with teh server.

```bash
$ jikkou server-info -o JSON | jq

{
  "version": "0.31.0",
  "build_time": "2023-11-15T10:35:22+0100",
  "commit_id": "f3384d38e606fb32599c175895d0cbef28258540"
}
```

