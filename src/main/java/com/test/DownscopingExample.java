/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test;

import java.io.File;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.CredentialAccessBoundary;
import com.google.auth.oauth2.DownscopedCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.OAuth2CredentialsWithRefresh;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.FileInputStream;
import java.io.IOException;

/** Demonstrates how to use Downscoping with Credential Access Boundaries. */
public class DownscopingExample {

    /**
     * Tests the downscoping functionality.
     *
     * <p>
     * This will generate a downscoped token with readonly access to the specified
     * GCS bucket,
     * inject them into a storage instance and then test print the contents of the
     * specified object.
     */
    public static void main(String[] args) throws IOException {
        // TODO(developer): Replace these variables before running the sample.
        String serviceAccountKeyFile = "./gcs_sa_key.json";
        // The Cloud Storage bucket name.
        String bucketName = "gcs-token";
        // The Cloud Storage object prefix that resides in the specified bucket.
        String objectPrefix = "device1/1/2/3/";

        // tokenConsumer(bucketName, objectName);
        AccessToken accessToken = getTokenFromBroker(serviceAccountKeyFile, bucketName, objectPrefix);
        System.out.println(accessToken.getTokenValue());
    }

    /** Simulates token broker generating downscoped tokens for specified bucket. */
    // [START auth_downscoping_token_broker]
    public static AccessToken getTokenFromBroker(String keyFile, String bucketName, String objectPrefix)
            throws IOException {
        // Retrieve the source credentials from ADC.
        // GoogleCredentials sourceCredentials =
        // GoogleCredentials.getApplicationDefault()
        // .createScoped("https://www.googleapis.com/auth/cloud-platform");

        GoogleCredentials sourceCredentials;

        File credentialsPath = new File(keyFile);
        try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
            sourceCredentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
        }
        if (sourceCredentials.createScopedRequired())
            sourceCredentials = sourceCredentials
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");

        // [START auth_downscoping_rules]
        // Initialize the Credential Access Boundary rules.
        String availableResource = "//storage.googleapis.com/projects/_/buckets/" + bucketName;

        // Downscoped credentials will have readonly access to the resource.
        String availablePermission = "inRole:roles/storage.objectAdmin";

        // Only objects starting with the specified prefix string in the object name
        // will be allowed
        // read access.
        String expression = "resource.name.startsWith('projects/_/buckets/"
                + bucketName
                + "/objects/"
                + objectPrefix
                + "')";

        // Build the AvailabilityCondition.
        CredentialAccessBoundary.AccessBoundaryRule.AvailabilityCondition availabilityCondition = CredentialAccessBoundary.AccessBoundaryRule.AvailabilityCondition
                .newBuilder()
                .setExpression(expression)
                .build();

        // Define the single access boundary rule using the above properties.
        CredentialAccessBoundary.AccessBoundaryRule rule = CredentialAccessBoundary.AccessBoundaryRule.newBuilder()
                .setAvailableResource(availableResource)
                .addAvailablePermission(availablePermission)
                .setAvailabilityCondition(availabilityCondition)
                .build();

        // Define the Credential Access Boundary with all the relevant rules.
        CredentialAccessBoundary credentialAccessBoundary = CredentialAccessBoundary.newBuilder().addRule(rule).build();
        // [END auth_downscoping_rules]

        // [START auth_downscoping_initialize_downscoped_cred]
        // Create the downscoped credentials.
        DownscopedCredentials downscopedCredentials = DownscopedCredentials.newBuilder()
                .setSourceCredential(sourceCredentials)
                .setCredentialAccessBoundary(credentialAccessBoundary)
                .build();

        // Retrieve the token.
        // This will need to be passed to the Token Consumer.
        AccessToken accessToken = downscopedCredentials.refreshAccessToken();
        // [END auth_downscoping_initialize_downscoped_cred]
        return accessToken;
    }
    // [END auth_downscoping_token_broker]
}