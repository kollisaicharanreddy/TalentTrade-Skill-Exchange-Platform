package com.talenttrade.exception;

import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class OAuthAuthenticationException extends AuthenticationException {
    public OAuthAuthenticationException(String msg) {
        super(msg);
    }
    public OAuthAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
