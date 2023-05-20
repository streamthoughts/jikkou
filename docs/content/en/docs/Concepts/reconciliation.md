---
tags: [ "concept" ]
title: "Reconciliation"
linkTitle: "Reconciliation"
weight: 3
---

{{% pageinfo color="info" %}}
In the context of Jikkou, reconciliation refers to the process of comparing the desired state of an object with the
actual state of the system and making any necessary corrections or adjustments to align them.
{{% /pageinfo %}}

### Changes

A _Change_ represents a difference, detected during reconciliation, between two objects that can reconciled or
corrected by adding, updating, or deleting an object or property attached to the actual state of the system.

A _Change_ represents a detected difference between two objects during the reconciliation process. These differences can
be reconciled or corrected by adding, updating, or deleting an object or property associated with the actual state of
the system

* Jikkou identifies four types of changes:

* **ADD**: Indicates the addition of a new object or property to an existing object.
* **UPDATE**: Indicates modifications made to an existing object or property of an existing object.
* **DELETE**: Indicates the removal of an existing object or property of an existing object.
* **NONE**: Indicates that no changes were made to an existing object or property.

### Reconciliation Modes

Depending on the chosen reconciliation mode, only specific types of changes will be applied.

Jikkou provides four distinct reconciliation modes that determine the types of changes to be applied:

* **`CREATE`**: This mode only applies changes that create new resource objects in your system.
* **`DELETE`**: This mode only applies changes that delete existing resource objects in your system.
* **`UPDATE`**: This mode only applies changes that create or update existing resource objects in your system.
* **`APPLY_ALL`**: This mode applies all changes to ensure that the actual state of a resource in the cluster matches
  the desired state defined in your resource definition file, regardless of the specific type of change.

Each mode corresponds to a command offered by the Jikkou CLI (i.e., `create`, `update`, `delete`, and `apply`). Choose
the appropriate mode based on your requirements.