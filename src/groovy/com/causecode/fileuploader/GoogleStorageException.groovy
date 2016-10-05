package com.causecode.fileuploader

class GoogleStorageException extends StorageException {

    GoogleStorageException(String message) {
        super(message)
    }

    GoogleStorageException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
