/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws;

public interface AwsGlueAnnotations {

    String SCHEMAREGISTRY_AWSGLUE_PREFIX = "glue.aws.amazon.com/";

    String SCHEMA_CREATED_TIME = SCHEMAREGISTRY_AWSGLUE_PREFIX + "created-time";
    String SCHEMA_UPDATED_TIME = SCHEMAREGISTRY_AWSGLUE_PREFIX + "updated-time";
    String SCHEMA_REGISTRY_NAME = SCHEMAREGISTRY_AWSGLUE_PREFIX + "registry-name";
    String SCHEMA_REGISTRY_ARN = SCHEMAREGISTRY_AWSGLUE_PREFIX + "registry-arn";
    String SCHEMA_SCHEMA_ARN = SCHEMAREGISTRY_AWSGLUE_PREFIX + "schema-arn";
    String SCHEMA_SCHEMA_VERSION_ID = SCHEMAREGISTRY_AWSGLUE_PREFIX + "schema-version-id";
    String SCHEMA_REGISTRY_USE_CANONICAL_FINGERPRINT = SCHEMAREGISTRY_AWSGLUE_PREFIX + "use-canonical-fingerprint";
}
