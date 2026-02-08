# Jikkou End-to-End Tests

Shell-based test suite that exercises the **native CLI binary** against real services running in Docker.

## Prerequisites

- Docker with Compose v2
- Maven 3.8+
- GraalVM 22.1+ (for native build)

## Running

```bash
# Full run: build native image + start services + run tests + tear down
./e2e/run-tests.sh

# Skip the native build (use an existing binary from a prior build)
./e2e/run-tests.sh --skip-build

# Keep Docker containers running after tests (useful for debugging)
./e2e/run-tests.sh --skip-build --keep
```

Or via `make`:

```bash
make e2e-test          # full build + test
make e2e-test-quick    # skip build
```

## Directory Structure

```
e2e/
├── run-tests.sh                 # Test orchestrator
├── docker-compose-e2e.yml       # Kafka + Schema Registry + Kafka Connect
├── resources/
│   ├── jikkou-e2e.conf          # HOCON provider config (Kafka, SR, Connect)
│   ├── kafka-topics.yaml        # Kafka topic definitions
│   ├── kafka-topics-update.yaml # Updated topic config
│   ├── kafka-acls.yaml          # ACL definitions
│   ├── kafka-acls-delete.yaml   # ACL deletion (jikkou.io/delete annotation)
│   ├── kafka-quotas.yaml        # Client quota definitions
│   ├── kafka-quotas-update.yaml # Updated quota values
│   ├── schema-avro.yaml         # AVRO schema (v1)
│   ├── schema-avro-v2.yaml      # AVRO schema (v2, adds field)
│   ├── kafka-connector.yaml     # FileStreamSink connector
│   └── kafka-connector-update.yaml # Updated connector config
└── README.md
```

## How to Add a New Test

### 1. Create resource YAML files

Add YAML files to `e2e/resources/`. Follow existing patterns:

- Use an `e2e-` prefix for resource names to avoid collisions.
- For inline schemas, use a YAML block scalar (`schema: |`), not an object with a `value` key.
- For delete tests, use the `jikkou.io/delete: true` annotation.

Example:

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: KafkaTopic
metadata:
  name: 'e2e-my-new-topic'
spec:
  partitions: 1
  replicas: 1
```

### 2. Write a test function

Add a shell function in `run-tests.sh` under the `# ── Test scenarios` section. The function must return non-zero on failure.

```bash
test_my_feature_create() {
  # Apply the resource
  run_jikkou_capture apply --files "${E2E_RESOURCES}/my-resource.yaml"
  assert_exit_code 0 || return 1

  # Verify it exists
  run_jikkou_capture get <resource-type>
  assert_exit_code 0 || return 1
  assert_output_contains "e2e-my-resource" || return 1
}
```

### 3. Register the test in `main()`

Add a `run_test` call in the `main()` function:

```bash
run_test "My Feature: create"  test_my_feature_create
```

Tests run sequentially in the order listed, so place tests that depend on prior state (e.g., update after create) accordingly.

## Available Helper Functions

| Function                               | Description                                                                                   |
|----------------------------------------|-----------------------------------------------------------------------------------------------|
| `run_jikkou_capture <args...>`         | Run the CLI binary, capturing output into `$JIKKOU_OUTPUT` and exit code into `$JIKKOU_EXIT`. |
| `run_jikkou <args...>`                 | Run the CLI binary with output going to stdout/stderr (not captured).                         |
| `assert_exit_code <code>`              | Assert `$JIKKOU_EXIT` equals the expected code.                                               |
| `assert_output_contains <pattern>`     | Assert `$JIKKOU_OUTPUT` matches an extended regex (case-insensitive).                         |
| `assert_output_not_contains <pattern>` | Assert `$JIKKOU_OUTPUT` does **not** match the pattern.                                       |

## Services

The Docker Compose file starts three services with health checks:

| Service         | Image                                   | Port | Purpose              |
|-----------------|-----------------------------------------|------|----------------------|
| Kafka (KRaft)   | `confluentinc/cp-kafka:7.5.0`           | 9092 | Topics, ACLs, Quotas |
| Schema Registry | `confluentinc/cp-schema-registry:7.5.0` | 8081 | AVRO/JSON schemas    |
| Kafka Connect   | `confluentinc/cp-kafka-connect:7.5.0`   | 8083 | Connectors           |

The test script waits up to 180 seconds for all services to report healthy before running tests.

## Adding a New Provider

1. Add the service to `docker-compose-e2e.yml` (with a health check).
2. Add the provider config block to `resources/jikkou-e2e.conf` under `jikkou.provider.<name>`.
3. Update the `start_services()` health-check loop to wait for the new container.
4. Add resource files and test functions as described above.
