package com.talenttrade.exception;

import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOAuthProviderException extends AuthenticationException {
    public InvalidOAuthProviderException(String msg) {
        super(msg);
    }
}
