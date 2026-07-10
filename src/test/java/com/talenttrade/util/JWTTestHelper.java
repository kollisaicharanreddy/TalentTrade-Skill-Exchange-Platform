package com.talenttrade.util;

import com.talenttrade.entity.User;
import com.talenttrade.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JWTTestHelper {

    private final JwtService jwtService;

    public String generateTestToken(User user) {
        return jwtService.generateToken(user);
    }
}
