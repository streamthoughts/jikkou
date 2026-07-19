# Confluent Cloud provider

## Kinds

| Kind | Get command | apiVersion |
|---|---|---|
| RoleBinding | `jikkou get confluent-cloud role-bindings` | iam.confluent.cloud/v1 |

The `role-bindings` get command accepts `-o JSON|YAML` and selectors `-s '<expr>'`. It does not accept `--name` (role bindings are listed and filtered by selector, not fetched by name).

## Authoring examples

Role binding (one file, `---`-separated for multiple):
```yaml
apiVersion: 'iam.confluent.cloud/v1'
kind: 'RoleBinding'
metadata:
  labels: {}
  annotations: {}
spec:
  principal: 'User:sa-abc123'
  roleName: 'CloudClusterAdmin'
  crnPattern: 'crn://confluent.cloud/organization=org-123/environment=env-456/cloud-cluster=lkc-789'
```

`spec.principal` is `User:<id>` or `Group:<id>`. `spec.roleName` is any Confluent Cloud RBAC role name (e.g. `CloudClusterAdmin`, `EnvironmentAdmin`, `Operator`).

## Notes

- Requires `jikkou.provider.confluent-cloud.config`: `apiKey` and `apiSecret` (must be a **Cloud API Key**, not a Cluster API Key — a Cluster API Key fails with `401 Unauthorized`), and `crnPattern` (required — scopes role-binding list operations to an org/environment/cluster). `apiUrl` defaults to `https://api.confluent.cloud`. Optional proxy settings: `proxyUrl`, `proxyUsername`, `proxyPassword`, `nonProxyHosts`.
- Create a Cloud API Key with: `confluent api-key create --resource cloud --description "Jikkou role binding management"`.
- Deleting: add `jikkou.io/delete: true` under `metadata.annotations`, then `jikkou apply`.
- `jikkou api-resources schema --api-version=iam.confluent.cloud/v1 --kind=RoleBinding` prints the JSON Schema for the `spec` field.
