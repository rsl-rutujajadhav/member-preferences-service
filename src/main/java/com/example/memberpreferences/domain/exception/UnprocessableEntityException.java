package com.example.memberpreferences.domain.exception;

public class UnprocessableEntityException extends RuntimeException {

    public UnprocessableEntityException(String detail) {
        super(detail);
    }
}
