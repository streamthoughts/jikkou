---
title: "Reconciliation"
linkTitle: "Reconciliation"
weight: 3
---

{{% pageinfo color="info" %}}
In the context of Jikkou, reconciliation refers to the process by which the current state of a desired object
is compared to the actual state of the system and any differences (called _changes_) between the two are reconciled or
corrected.
{{% /pageinfo %}}

### Changes

A _Change_ represents a difference, detected during reconciliation, between two objects that can reconciled or
corrected by adding, updating, or deleting an object or property attached to the actual state of the system.

Jikkou computes four types of changes:

* **ADD**: Indicates that a new object or a property of an existing object was added.
* **UPDATE**: Indicates that an existing object or a property of an existing object was changed.
* **DELETE**: Indicates that an existing object must be deleted or a property of an existing object was removed.
* **NONE**: Indicates that an existing object or property was not changed.

### Reconciliation Modes

Depending on the chosen reconciliation mode, only specific types of changes may be applied.

Jikkou defines the following four distinct reconciliation modes:

Jikkou offers four distinct reconciliation modes that determine which types of changes will be applied:

* **CREATE**: Only changes that create new resource objects on your system will be applied.
* **DELETE**: Only changes that delete existing resource objects on your system will be applied.
* **UPDATE**: Only changes that create or update existing resource objects on your system will be applied.
* **APPLY_ALL**: Apply all changes to ensure that the actual state of a resource in the cluster matches the desired
  state as defined in your resource definition file, regardless of the specific type of change.

{{% alert title="Note" color="info" %}}
The reconciliation modes listed above correspond to the _commands_ available through the Jikkou CLI.
{{% /alert %}}