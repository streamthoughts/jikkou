---
title: "Detect Configuration Drift"
linkTitle: "Detect Drift"
weight: 4
description: >
  Run jikkou diff on a schedule to detect when your clusters no longer match what is in Git.
---

Configuration drift happens to every Kafka platform: someone bumps `retention.ms` during an
incident with a vendor UI, a topic gets created by hand, an ACL is "temporarily" widened.
Because Jikkou is stateless and always compares your Git-versioned definitions against the
**actual cluster state**, detecting drift is a single command:

```bash
jikkou diff --files ./resources --fail-on-changes
```

## Exit codes

With `--fail-on-changes`, `jikkou diff` uses distinct exit codes so CI can react precisely:

| Code | Meaning |
|------|---------|
| `0` | No changes: cluster matches Git |
| `1` | Validation or execution error |
| `2` | Usage error (invalid flags or arguments) |
| `3` | **Drift detected**: at least one pending change |

A one-line summary is printed to stderr (for example, `3 changes detected: 1 CREATE, 2 UPDATE`),
while stdout carries the full machine-readable diff (YAML by default, `-o JSON` available).

{{% alert title="Scoping the check" color="info" %}}
The drift check is evaluated on the filtered result. For example,
`--fail-on-changes --filter-change-op UPDATE` only counts updates as drift and ignores
creations and deletions. Without filters, every operation other than `NONE` counts.
{{% /alert %}}

## Scheduled drift check with GitHub Actions

The workflow below runs every hour using the
[`streamthoughts/setup-jikkou`](https://github.com/streamthoughts/setup-jikkou) action, uploads
the diff as an artifact, and opens an issue when drift is detected:

```yaml
name: Kafka Drift Detection

on:
  schedule:
    - cron: '0 * * * *'   # every hour
  workflow_dispatch: {}

jobs:
  drift:
    runs-on: ubuntu-latest
    permissions:
      issues: write
    steps:
      - uses: actions/checkout@v4

      - uses: streamthoughts/setup-jikkou@v1
        with:
          jikkou_config: ${{ github.workspace }}/.jikkou/config

      - name: Check for drift
        id: diff
        run: |
          set +e
          jikkou diff --files ./resources --fail-on-changes -o YAML > drift.yaml
          echo "exit_code=$?" >> "$GITHUB_OUTPUT"

      - name: Upload diff
        if: steps.diff.outputs.exit_code == '3'
        uses: actions/upload-artifact@v4
        with:
          name: drift-report
          path: drift.yaml

      - name: Open an issue on drift
        if: steps.diff.outputs.exit_code == '3'
        run: |
          gh issue create \
            --title "Kafka configuration drift detected ($(date -u +%F))" \
            --body "jikkou diff found pending changes. Download the drift-report artifact from run ${{ github.run_id }}."
        env:
          GH_TOKEN: ${{ github.token }}

      - name: Fail on error
        if: steps.diff.outputs.exit_code == '1' || steps.diff.outputs.exit_code == '2'
        run: exit 1
```

Instead of opening an issue, the same gate (`exit_code == '3'`) can post to Slack, page an
on-call rotation, or even trigger a `jikkou apply` job to revert the drift automatically.

## Running without cluster credentials

CI runners do not need direct access to your Kafka clusters. Run the
[Jikkou API server](/docs/how-to-guides/install/api-server/) inside your platform, and point
the CLI at it with [proxy mode](/docs/reference/api-server/configuration/cli_proxy_mode/):

```hocon
jikkou {
  proxy {
    enabled = true
    url = "https://jikkou.my-platform.internal"
    security {
      access-token = ${?JIKKOU_API_TOKEN}
    }
  }
}
```

With this configuration, the workflow above only needs an API token: cluster credentials stay
on the server side.

## Related

* [jikkou diff command reference](/docs/reference/cli/commands/jikkou-diff/)
* [Automate Jikkou with GitHub Actions](/docs/how-to-guides/automating/githubactions/)
* [Jikkou vs Terraform](/docs/comparisons/jikkou-vs-terraform/): why stateless reconciliation
  catches drift that state files miss
