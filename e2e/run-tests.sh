#!/usr/bin/env bash
#
# SPDX-License-Identifier: Apache-2.0
# Copyright (c) The original authors
#
# Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
#
# End-to-end test suite for Jikkou CLI.
# Runs the native binary against real Docker services (Kafka, Schema Registry, Kafka Connect).
#
# Usage:
#   ./e2e/run-tests.sh              # Build native + run all tests
#   ./e2e/run-tests.sh --skip-build # Use existing binary
#   ./e2e/run-tests.sh --keep       # Don't tear down containers after tests
#
set -euo pipefail

# ── Paths ────────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose-e2e.yml"
E2E_RESOURCES="${SCRIPT_DIR}/resources"
JIKKOU_HOCON_CONFIG="${E2E_RESOURCES}/jikkou-e2e.conf"
# The CLI uses a JSON context file (like kubeconfig), not the HOCON directly.
# We generate this at runtime so it contains the correct absolute path.
JIKKOU_CONFIG=""  # set in setup_config()

# ── Flags ────────────────────────────────────────────────────────────────────
SKIP_BUILD=false
KEEP_CONTAINERS=false

for arg in "$@"; do
  case "$arg" in
    --skip-build) SKIP_BUILD=true ;;
    --keep)       KEEP_CONTAINERS=true ;;
    *)            echo "Unknown flag: $arg"; exit 1 ;;
  esac
done

# ── Version / binary ────────────────────────────────────────────────────────
VERSION=$(mvn -f "${PROJECT_ROOT}/pom.xml" \
  org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate \
  -Dexpression=project.version -q -DforceStdout)
JIKKOU_BIN="${PROJECT_ROOT}/cli/target/jikkou-cli-${VERSION}-runner"

# ── Colours & counters ──────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

TESTS_PASSED=0
TESTS_FAILED=0
TESTS_TOTAL=0
FAILED_TESTS=()

# ── Helper functions ────────────────────────────────────────────────────────

log_info()  { echo -e "${CYAN}[INFO]${NC}  $*"; }
log_ok()    { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }
log_test()  { echo -e "${BOLD}[TEST]${NC}  $*"; }

# Generate the JSON context config that the CLI expects.
# The CLI reads a JSON "kubeconfig-style" file, which references the HOCON provider config.
setup_config() {
  JIKKOU_CONFIG=$(mktemp /tmp/jikkou-e2e-context-XXXXXX.json)
  cat > "${JIKKOU_CONFIG}" <<EOF
{
  "currentContext": "e2e",
  "e2e": {
    "configFile": "${JIKKOU_HOCON_CONFIG}",
    "configProps": {}
  }
}
EOF
  log_ok "Generated CLI context config: ${JIKKOU_CONFIG}"
}

# Run jikkou binary with the e2e config.
run_jikkou() {
  JIKKOUCONFIG="${JIKKOU_CONFIG}" "${JIKKOU_BIN}" "$@"
}

# Capture stdout+stderr and exit code from a jikkou invocation.
# Sets: JIKKOU_OUTPUT, JIKKOU_EXIT
run_jikkou_capture() {
  set +e
  JIKKOU_OUTPUT=$(JIKKOUCONFIG="${JIKKOU_CONFIG}" "${JIKKOU_BIN}" "$@" 2>&1)
  JIKKOU_EXIT=$?
  set -e
}

assert_exit_code() {
  local expected=$1
  if [[ "${JIKKOU_EXIT}" -ne "${expected}" ]]; then
    log_error "Expected exit code ${expected}, got ${JIKKOU_EXIT}"
    log_error "Output: ${JIKKOU_OUTPUT}"
    return 1
  fi
}

assert_output_contains() {
  local pattern=$1
  if ! echo "${JIKKOU_OUTPUT}" | grep -qiE "${pattern}"; then
    log_error "Output does not match pattern: ${pattern}"
    log_error "Output: ${JIKKOU_OUTPUT}"
    return 1
  fi
}

assert_output_not_contains() {
  local pattern=$1
  if echo "${JIKKOU_OUTPUT}" | grep -qiE "${pattern}"; then
    log_error "Output unexpectedly matches pattern: ${pattern}"
    log_error "Output: ${JIKKOU_OUTPUT}"
    return 1
  fi
}

test_passed() {
  TESTS_PASSED=$((TESTS_PASSED + 1))
  TESTS_TOTAL=$((TESTS_TOTAL + 1))
  log_ok "${GREEN}PASS${NC} - $1"
}

test_failed() {
  TESTS_FAILED=$((TESTS_FAILED + 1))
  TESTS_TOTAL=$((TESTS_TOTAL + 1))
  FAILED_TESTS+=("$1")
  log_error "${RED}FAIL${NC} - $1"
}

# Run a test function, catch failures.
run_test() {
  local test_name=$1
  local test_func=$2
  log_test "Running: ${test_name}"
  if ${test_func}; then
    test_passed "${test_name}"
  else
    test_failed "${test_name}"
  fi
  echo ""
}

# ── Build phase ─────────────────────────────────────────────────────────────

build_native() {
  if [[ "${SKIP_BUILD}" == "true" ]]; then
    log_info "Skipping native build (--skip-build)"
  else
    log_info "Building native image (this may take several minutes)..."
    "${PROJECT_ROOT}/mvnw" -f "${PROJECT_ROOT}/pom.xml" \
      clean package -Pnative -DskipTests -ntp -B
  fi

  if [[ ! -x "${JIKKOU_BIN}" ]]; then
    log_error "Binary not found or not executable: ${JIKKOU_BIN}"
    exit 1
  fi
  log_ok "Binary ready: ${JIKKOU_BIN}"
}

# ── Docker phase ────────────────────────────────────────────────────────────

start_services() {
  log_info "Starting Docker services..."
  docker compose -f "${COMPOSE_FILE}" up -d

  log_info "Waiting for services to become healthy..."
  local max_wait=180
  local elapsed=0
  local interval=5

  while [[ ${elapsed} -lt ${max_wait} ]]; do
    local kafka_ok=false
    local sr_ok=false
    local connect_ok=false

    if docker inspect --format='{{.State.Health.Status}}' e2e-kafka 2>/dev/null | grep -q healthy; then
      kafka_ok=true
    fi
    if docker inspect --format='{{.State.Health.Status}}' e2e-schema-registry 2>/dev/null | grep -q healthy; then
      sr_ok=true
    fi
    if docker inspect --format='{{.State.Health.Status}}' e2e-connect 2>/dev/null | grep -q healthy; then
      connect_ok=true
    fi

    if ${kafka_ok} && ${sr_ok} && ${connect_ok}; then
      log_ok "All services healthy"
      return 0
    fi

    log_info "Waiting... (${elapsed}s/${max_wait}s) kafka=${kafka_ok} schema-registry=${sr_ok} connect=${connect_ok}"
    sleep ${interval}
    elapsed=$((elapsed + interval))
  done

  log_error "Services did not become healthy within ${max_wait}s"
  docker compose -f "${COMPOSE_FILE}" logs
  exit 1
}

stop_services() {
  if [[ "${KEEP_CONTAINERS}" == "true" ]]; then
    log_info "Keeping containers running (--keep)"
  else
    log_info "Tearing down Docker services..."
    docker compose -f "${COMPOSE_FILE}" down -v
  fi
}

# ── Test scenarios ──────────────────────────────────────────────────────────
# Organized as CRUD per resource type.

# ── Kafka Topics (full CRUD + diff) ─────────────────────────────────────────

test_kafka_topics_create() {
  run_jikkou_capture apply --files "${E2E_RESOURCES}/kafka-topics.yaml"
  assert_exit_code 0 || return 1

  # Verify topics exist
  run_jikkou_capture get kafkatopics --name 'e2e-topic-1'
  assert_exit_code 0 || return 1
  assert_output_contains "e2e-topic-1" || return 1

  run_jikkou_capture get kafkatopics --name 'e2e-topic-2'
  assert_exit_code 0 || return 1
  assert_output_contains "e2e-topic-2" || return 1
}

test_kafka_topics_read() {
  # Read all topics and confirm both e2e topics are listed
  run_jikkou_capture get kafkatopics
  assert_exit_code 0 || return 1
  assert_output_contains "e2e-topic-1" || return 1
  assert_output_contains "e2e-topic-2" || return 1

  # Read a single topic with detail — verify partition count and config
  run_jikkou_capture get kafkatopics --name 'e2e-topic-1'
  assert_exit_code 0 || return 1
  assert_output_contains "partitions.*3" || return 1
  assert_output_contains "cleanup.policy" || return 1
}

test_kafka_topics_update() {
  run_jikkou_capture apply --files "${E2E_RESOURCES}/kafka-topics-update.yaml"
  assert_exit_code 0 || return 1

  # Verify updated config
  run_jikkou_capture get kafkatopics --name 'e2e-topic-1'
  assert_exit_code 0 || return 1
  assert_output_contains "compact" || return 1
  assert_output_contains "86400000" || return 1
}

test_kafka_topics_diff() {
  # The current state has cleanup.policy=compact from the update test.
  # Diff against the original file which has cleanup.policy=delete.
  run_jikkou_capture diff --files "${E2E_RESOURCES}/kafka-topics.yaml"
  assert_exit_code 0 || return 1
  assert_output_contains "UPDATE|CHANGED" || return 1
}

test_kafka_topics_delete() {
  local tmpfile
  tmpfile=$(mktemp /tmp/e2e-delete-XXXXXX.yaml)
  trap "rm -f ${tmpfile}" RETURN

  cat > "${tmpfile}" <<'EOF'
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: KafkaTopic
metadata:
  name: 'e2e-topic-to-delete'
spec:
  partitions: 1
  replicas: 1
EOF

  run_jikkou_capture apply --files "${tmpfile}"
  assert_exit_code 0 || return 1

  # Verify topic was created
  run_jikkou_capture get kafkatopics --name 'e2e-topic-to-delete'
  assert_exit_code 0 || return 1
  assert_output_contains "e2e-topic-to-delete" || return 1

  # Now apply with delete annotation
  cat > "${tmpfile}" <<'EOF'
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: KafkaTopic
metadata:
  name: 'e2e-topic-to-delete'
  annotations:
    jikkou.io/delete: true
spec:
  partitions: 1
  replicas: 1
EOF

  run_jikkou_capture apply --files "${tmpfile}"
  assert_exit_code 0 || return 1

  # Verify topic is gone
  run_jikkou_capture get kafkatopics --name 'e2e-topic-to-delete'
  assert_output_not_contains "e2e-topic-to-delete" || return 1
}

# ── Kafka ACLs (create, read, delete — no update for ACLs) ──────────────────

test_kafka_acls_create() {
  run_jikkou_capture apply --files "${E2E_RESOURCES}/kafka-acls.yaml"
  assert_exit_code 0 || return 1

  # Verify ACLs exist
  run_jikkou_capture get kafkaprincipalauthorizations
  assert_exit_code 0 || return 1
  assert_output_contains "e2e-alice" || return 1
}

test_kafka_acls_read() {
  run_jikkou_capture get kafkaprincipalauthorizations
  assert_exit_code 0 || return 1
  assert_output_contains "e2e-alice" || return 1
  assert_output_contains "PREFIXED" || return 1
  assert_output_contains "READ" || return 1
  assert_output_contains "WRITE" || return 1
}

test_kafka_acls_delete() {
  run_jikkou_capture apply --files "${E2E_RESOURCES}/kafka-acls-delete.yaml"
  assert_exit_code 0 || return 1

  # Verify the ACLs for e2e-alice are gone
  run_jikkou_capture get kafkaprincipalauthorizations
  assert_exit_code 0 || return 1
  assert_output_not_contains "e2e-alice" || return 1
}

# ── Kafka Quotas (full CRUD) ────────────────────────────────────────────────

test_kafka_quotas_create() {
  run_jikkou_capture apply --files "${E2E_RESOURCES}/kafka-quotas.yaml"
  assert_exit_code 0 || return 1

  # Verify quota exists
  run_jikkou_capture get kafkaclientquotas
  assert_exit_code 0 || return 1
  assert_output_contains "e2e-client" || return 1
}

test_kafka_quotas_read() {
  run_jikkou_capture get kafkaclientquotas
  assert_exit_code 0 || return 1
  assert_output_contains "e2e-client" || return 1
  assert_output_contains "1048576" || return 1
}

test_kafka_quotas_update() {
  run_jikkou_capture apply --files "${E2E_RESOURCES}/kafka-quotas-update.yaml"
  assert_exit_code 0 || return 1

  # Verify updated quota values
  run_jikkou_capture get kafkaclientquotas
  assert_exit_code 0 || return 1
  assert_output_contains "2097152" || return 1
}

test_kafka_quotas_delete() {
  local tmpfile
  tmpfile=$(mktemp /tmp/e2e-quota-delete-XXXXXX.yaml)
  trap "rm -f ${tmpfile}" RETURN

  cat > "${tmpfile}" <<'EOF'
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaClientQuota"
metadata:
  labels: {}
  annotations:
    jikkou.io/delete: true
spec:
  type: 'CLIENT'
  entity:
    clientId: 'e2e-client'
  configs:
    requestPercentage: 25
    producerByteRate: 2097152
    consumerByteRate: 2097152
EOF

  run_jikkou_capture apply --files "${tmpfile}"
  assert_exit_code 0 || return 1

  # Verify quota is gone
  run_jikkou_capture get kafkaclientquotas
  assert_output_not_contains "e2e-client" || return 1
}

# ── Schema Registry (full CRUD) ─────────────────────────────────────────────

test_schema_registry_create() {
  run_jikkou_capture apply --files "${E2E_RESOURCES}/schema-avro.yaml"
  assert_exit_code 0 || return 1

  # Verify subject exists
  run_jikkou_capture get schemaregistrysubjects
  assert_exit_code 0 || return 1
  assert_output_contains "E2ePersonAvro" || return 1
}

test_schema_registry_read() {
  run_jikkou_capture get schemaregistrysubjects --name 'E2ePersonAvro'
  assert_exit_code 0 || return 1
  assert_output_contains "E2ePersonAvro" || return 1
  assert_output_contains "AVRO" || return 1
  assert_output_contains "firstname" || return 1
  assert_output_contains "lastname" || return 1
}

test_schema_registry_update() {
  run_jikkou_capture apply --files "${E2E_RESOURCES}/schema-avro-v2.yaml"
  assert_exit_code 0 || return 1

  # Verify updated schema contains the new "email" field
  run_jikkou_capture get schemaregistrysubjects --name 'E2ePersonAvro'
  assert_exit_code 0 || return 1
  assert_output_contains "email" || return 1
}

test_schema_registry_delete() {
  local tmpfile
  tmpfile=$(mktemp /tmp/e2e-schema-delete-XXXXXX.yaml)
  trap "rm -f ${tmpfile}" RETURN

  cat > "${tmpfile}" <<'EOF'
---
apiVersion: "schemaregistry.jikkou.io/v1beta2"
kind: "SchemaRegistrySubject"
metadata:
  name: "E2ePersonAvro"
  annotations:
    jikkou.io/delete: true
spec:
  schemaType: "AVRO"
  schema: "{}"
EOF

  run_jikkou_capture apply --files "${tmpfile}"
  assert_exit_code 0 || return 1

  # Verify subject is gone
  run_jikkou_capture get schemaregistrysubjects
  assert_output_not_contains "E2ePersonAvro" || return 1
}

# ── Kafka Connect (full CRUD) ───────────────────────────────────────────────

test_kafka_connect_create() {
  run_jikkou_capture apply --files "${E2E_RESOURCES}/kafka-connector.yaml"
  assert_exit_code 0 || return 1

  # Give the connector a moment to start
  sleep 5

  # Verify connector exists
  run_jikkou_capture get kafkaconnectors
  assert_exit_code 0 || return 1
  assert_output_contains "e2e-file-sink" || return 1
}

test_kafka_connect_read() {
  run_jikkou_capture get kafkaconnectors
  assert_exit_code 0 || return 1
  assert_output_contains "e2e-file-sink" || return 1
  assert_output_contains "FileStreamSink" || return 1
  assert_output_contains "RUNNING" || return 1
}

test_kafka_connect_update() {
  run_jikkou_capture apply --files "${E2E_RESOURCES}/kafka-connector-update.yaml"
  assert_exit_code 0 || return 1

  sleep 5

  # Verify the connector config was updated
  run_jikkou_capture get kafkaconnectors
  assert_exit_code 0 || return 1
  assert_output_contains "e2e-test-updated.sink.txt" || return 1
}

test_kafka_connect_delete() {
  local tmpfile
  tmpfile=$(mktemp /tmp/e2e-connector-delete-XXXXXX.yaml)
  trap "rm -f ${tmpfile}" RETURN

  cat > "${tmpfile}" <<'EOF'
---
apiVersion: "kafka.jikkou.io/v1beta1"
kind: "KafkaConnector"
metadata:
  name: "e2e-file-sink"
  labels:
    kafka.jikkou.io/connect-cluster: "e2e-connect"
  annotations:
    jikkou.io/delete: true
spec:
  connectorClass: "FileStreamSink"
  tasksMax: 1
  config:
    file: "/tmp/e2e-test-updated.sink.txt"
    topics: "e2e-topic-1"
  state: "RUNNING"
EOF

  run_jikkou_capture apply --files "${tmpfile}"
  assert_exit_code 0 || return 1

  sleep 3

  # Verify the connector is gone
  run_jikkou_capture get kafkaconnectors
  assert_output_not_contains "e2e-file-sink" || return 1
}

# ── Health ───────────────────────────────────────────────────────────────────

test_health_indicators() {
  run_jikkou_capture health get kafka
  assert_exit_code 0 || return 1
  assert_output_contains "UP|HEALTHY" || return 1
}

# ── Main ────────────────────────────────────────────────────────────────────

main() {
  echo ""
  echo -e "${BOLD}======================================${NC}"
  echo -e "${BOLD}  Jikkou End-to-End Test Suite${NC}"
  echo -e "${BOLD}======================================${NC}"
  echo ""

  build_native
  echo ""

  setup_config
  echo ""

  start_services
  echo ""

  log_info "Running test scenarios..."
  echo ""

  # ── Kafka Topics CRUD ──
  run_test "Kafka Topics: create"           test_kafka_topics_create
  run_test "Kafka Topics: read"             test_kafka_topics_read
  run_test "Kafka Topics: update"           test_kafka_topics_update
  run_test "Kafka Topics: diff"             test_kafka_topics_diff
  run_test "Kafka Topics: delete"           test_kafka_topics_delete

  # ── Kafka ACLs CRD (no update — ACLs are atomic) ──
  run_test "Kafka ACLs: create"             test_kafka_acls_create
  run_test "Kafka ACLs: read"               test_kafka_acls_read
  run_test "Kafka ACLs: delete"             test_kafka_acls_delete

  # ── Kafka Quotas CRUD ──
  run_test "Kafka Quotas: create"           test_kafka_quotas_create
  run_test "Kafka Quotas: read"             test_kafka_quotas_read
  run_test "Kafka Quotas: update"           test_kafka_quotas_update
  run_test "Kafka Quotas: delete"           test_kafka_quotas_delete

  # ── Schema Registry CRUD ──
  run_test "Schema Registry: create"        test_schema_registry_create
  run_test "Schema Registry: read"          test_schema_registry_read
  run_test "Schema Registry: update"        test_schema_registry_update
  run_test "Schema Registry: delete"        test_schema_registry_delete

  # ── Kafka Connect CRUD ──
  run_test "Kafka Connect: create"          test_kafka_connect_create
  run_test "Kafka Connect: read"            test_kafka_connect_read
  run_test "Kafka Connect: update"          test_kafka_connect_update
  run_test "Kafka Connect: delete"          test_kafka_connect_delete

  # ── Health ──
  run_test "Health indicators"              test_health_indicators

  echo ""
  echo -e "${BOLD}======================================${NC}"
  echo -e "${BOLD}  Results${NC}"
  echo -e "${BOLD}======================================${NC}"
  echo -e "  Total:  ${TESTS_TOTAL}"
  echo -e "  ${GREEN}Passed: ${TESTS_PASSED}${NC}"
  echo -e "  ${RED}Failed: ${TESTS_FAILED}${NC}"

  if [[ ${#FAILED_TESTS[@]} -gt 0 ]]; then
    echo ""
    echo -e "${RED}Failed tests:${NC}"
    for t in "${FAILED_TESTS[@]}"; do
      echo -e "  - ${t}"
    done
  fi

  echo ""

  stop_services

  # Clean up temp config file
  rm -f "${JIKKOU_CONFIG}"

  if [[ ${TESTS_FAILED} -gt 0 ]]; then
    exit 1
  fi
}

main
