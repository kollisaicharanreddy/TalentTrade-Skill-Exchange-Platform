package com.talenttrade.exception;

import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateOAuthAccountException extends AuthenticationException {
    public DuplicateOAuthAccountException(String msg) {
        super(msg);
    }
}
