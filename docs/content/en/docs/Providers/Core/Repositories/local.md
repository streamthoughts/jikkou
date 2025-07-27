---
title: "Local Resource Repository"
linkTitle: "Local Resource Repository"
description: "Load resources from local files or directories."
weight: 1
---

## Overview

The `LocalResourceRepository` can be used to load resources from local files or directories.

## Configuration

```yaml
jikkou {
  repositories = [
    {
      # Name of your local repositories  
      name = "<string>"
      # The fully qualified class name (FQCN) of the repository
      type = io.streamthoughts.jikkou.core.repository.LocalResourceRepository
      config {
        # Specify the locations containing the definitions for resources in a YAML file, a directory.
        files = []
        
        # Specify the pattern used to match YAML file paths when one or multiple directories are given through the `files` property.
        # Pattern should be passed in the form of 'syntax:pattern'. The "glob" and "regex" syntaxes are supported (e.g.: **/*.{yaml,yml}).
        # If no syntax is specified the 'glob' syntax is used.
        file-name = "<string>"    # Default: **/*.{yaml,yml}
        
        # Specify the locations of the values-files containing the variables to pass into the template engine built-in object 'Values'.
        values-files = []
        
        # Specify the pattern used to match YAML file paths when one or multiple directories are given through the `values-files` property.
        # Pattern should be passed in the form of 'syntax:pattern'. The "glob" and "regex" syntaxes are supported (e.g.: **/*.{yaml,yml}).
        # If no syntax is specified the 'glob' syntax is used.
        values-file-name = "<string>" # Default: **/*.{yaml,yml}
        
        # The labels to add to all resources loaded from the repository
        labels {
          <label_key> = <label_value>
        }
      }
    }
  ]
}
```