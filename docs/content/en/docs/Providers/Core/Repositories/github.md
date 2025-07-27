---
title: "Github Resource Repository"
linkTitle: "Github Resource Repository"
description: "Load resources from a public or private GitHub repository."
weight: 1
---

## Overview

The `GithubResourceRepository` can be used to load resources from a public or private GitHub repository.

## Configuration

```yaml
jikkou {
  repositories = [
    {
      # Name of your local repositories  
      name = "<string>"
      # The fully qualified class name (FQCN) of the repository
      type = io.streamthoughts.jikkou.core.repository.GithubResourceRepository
      config {
        # Specify the GitHub repository in the format 'owner/repo'
        repository = "<string>"
  
        # Specify the branch or ref to load resources from
        branch = main
  
        # Specify the paths/directories in the repository containing the resource definitions
        paths = []    # Default: **/*.{yaml,yml}
  
        # Specify the pattern used to match YAML file paths.
        # Pattern should be passed in the form of 'syntax:pattern'. The "glob" and "regex" syntaxes are supported (e.g.: **/*.{yaml,yml}).
        # If no syntax is specified the 'glob' syntax is used.
        file-pattern = "**/*.{yaml,yml}"
        
        # Specify the locations of the values-files containing the variables to pass into the template engine built-in object 'Values'.
        values-files = []
        
        # Specify the pattern used to match YAML file paths when one or multiple directories are given through the `values-files` property.
        # Pattern should be passed in the form of 'syntax:pattern'. The "glob" and "regex" syntaxes are supported (e.g.: **/*.{yaml,yml}).
        # If no syntax is specified the 'glob' syntax is used.
        values-file-name = "<string>" # Default: **/*.{yaml,yml}
        
        # The labels to be added to all resources loaded from the repository
        labels {
          <label_key> = <label_value>
        }
      }
    }
  ]
}
```