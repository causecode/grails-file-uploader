package com.causecode.fileuploader

class ProviderNotFoundException extends Exception {

    ProviderNotFoundException(String message) {
        super(message)
    }

    ProviderNotFoundException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
