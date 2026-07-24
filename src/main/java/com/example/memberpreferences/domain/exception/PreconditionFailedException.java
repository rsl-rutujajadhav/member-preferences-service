package com.example.memberpreferences.domain.exception;

public class PreconditionFailedException extends RuntimeException {

    public PreconditionFailedException(String detail) {
        super(detail);
    }
}
