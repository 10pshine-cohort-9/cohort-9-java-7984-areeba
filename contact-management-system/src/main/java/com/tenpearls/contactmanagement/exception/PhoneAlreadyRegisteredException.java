package com.tenpearls.contactmanagement.exception;

public class PhoneAlreadyRegisteredException extends RuntimeException {

    public PhoneAlreadyRegisteredException(String message) {
        super(message);
    }
}
