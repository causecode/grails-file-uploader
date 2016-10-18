/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.cdn.google

import com.causecode.fileuploader.StorageConfigurationException
import com.google.cloud.storage.Storage
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.Holders
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class GoogleCredentialsSpec extends Specification {

    ConfigObject storageProviderGoogle

    void setup() {
        storageProviderGoogle = grailsApplication.config.fileuploader.storageProvider.google
    }

    void cleanup() {
        // Restore the config object.
        grailsApplication.config.fileuploader.storageProvider.google = storageProviderGoogle
    }

    void 'test credentials initialization from config object'() {
        when: 'Credentials are read from the config object'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()

        then: 'Fields should be successfully initialized'
        googleCredentials.project_id == storageProviderGoogle.project_id
        googleCredentials.type == storageProviderGoogle.type
    }

    void 'test credentials initialization when config object is empty'() {
        given: 'Config object is set to empty map'
        grailsApplication.config.fileuploader.storageProvider.google = [:]

        when: 'Credentials are read from the config object'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()

        then: 'StorageConfigurationException should be thrown'
        StorageConfigurationException exception = thrown(StorageConfigurationException)
        exception.message == 'No configuration found for storage provider Google.'
    }

    void 'test credentials initialization when config object does not contain the project_id'() {
        given: 'project_id is set to blank string'
        grailsApplication.config.fileuploader.storageProvider.google.project_id = ''

        when: 'Credentials are read from the config object'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()

        then: 'StorageConfigurationException should be thrown'
        StorageConfigurationException exception = thrown(StorageConfigurationException)
        exception.message == 'Project Id is required for storage provider Google.'
    }

    void 'test authenticaton by reading path of json file from config object'() {
        given: 'auth variable is set to point to testkey.json file'
        File file = new File('')
        String testFilePath = file.absolutePath +
                '/test/unit/com/causecode/fileuploader/cdn/google/testkey.json'
        file.delete()
        // Only auth variable should be present. To confirm credentials are read from the file and not the config.
        grailsApplication.config.fileuploader.storageProvider.google = [:]
        grailsApplication.config.fileuploader.storageProvider.google.auth = testFilePath
        grailsApplication.config.fileuploader.storageProvider.google.project_id = 'test_id'

        assert Holders.config.fileuploader.storageProvider.google.private_key == null

        when: 'Credentials are read from the test json key file'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()
        Storage storage = googleCredentials.authenticateUsingKeyFileFromConfig()

        then: 'Authentication should be successful and no exception should be thrown'
        notThrown(Exception)
        googleCredentials.project_id == 'test_id'
        storage != null // Only writing storage will do but that is not readable.
    }

    @Unroll
    void 'test authenticaton by reading path of json file from config object when path is #filePath'() {
        given: 'auth is set to blank/incorrect path'
        grailsApplication.config.fileuploader.storageProvider.google = [:]
        grailsApplication.config.fileuploader.storageProvider.google.auth = filePath
        grailsApplication.config.fileuploader.storageProvider.google.project_id = 'test_id'

        assert Holders.config.fileuploader.storageProvider.google.private_key == null

        when: 'File path is read from the config object'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()
        Storage storage = googleCredentials.authenticateUsingKeyFileFromConfig()

        then: 'StorageConfigurationException/IOException should be thrown'
        Exception exception = thrown(exceptionClass)
        exception.message == message
        storage == null // !storage will do but written this way for readability.

        where:
        filePath | exceptionClass | message
        '' | IllegalArgumentException | 'JSON Key file path for storage provider Google not found.'
        '/incorrect/path/to/testkey.json' | IOException | '/incorrect/path/to/testkey.json (No such file or directory)'
    }

    void 'test authentication for failure when credentials are read directly from the configuration object'() {
        given: 'Google storage provider configuration'
        grailsApplication.config.fileuploader.storageProvider.google = [:]
        grailsApplication.config.fileuploader.storageProvider.google.project_id = 'test_id'

        when: 'Credentials are read directly from the config object'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()
        Storage storage = googleCredentials.authenticateUsingValuesFromConfig()

        then: 'IOException should be thrown'
        IOException exception = thrown(IOException)
        exception.message == 'Invalid PKCS#8 data.' // Due to absence of private_key.
        storage == null
    }

    void 'test authentication for success when credentials are read directly from the configuration object'() {
        when: 'Credentials are read directly from the config object'
        grailsApplication.config.fileuploader.storageProvider.google.project_id = 'test_id'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()
        Storage storage = googleCredentials.authenticateUsingValuesFromConfig()

        then: 'Authentication should be successful'
        storage != null
    }
}
