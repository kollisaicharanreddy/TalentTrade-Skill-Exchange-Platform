package com.talenttrade.security;

import com.talenttrade.entity.User;
import com.talenttrade.entity.Role;
import com.talenttrade.entity.AuthProvider;
import com.talenttrade.exception.OAuthAuthenticationException;
import com.talenttrade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        try {
            return processOAuth2User(oAuth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuthAuthenticationException(ex.getMessage(), ex);
        }
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        if (email == null || email.trim().isEmpty()) {
            throw new OAuthAuthenticationException("Email not found from OAuth2 provider");
        }

        String name = oAuth2User.getAttribute("name");
        if (name == null || name.trim().isEmpty()) {
            name = oAuth2User.getAttribute("given_name");
            if (name == null) {
                name = email.split("@")[0];
            }
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Automatically create a new user if they do not exist
            String username = email.split("@")[0];
            int suffix = 1;
            while (userRepository.existsByUsername(username)) {
                username = email.split("@")[0] + suffix;
                suffix++;
            }

            user = User.builder()
                    .fullName(name)
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // satisfying existing NOT NULL constraint
                    .emailVerified(true) // Auto verified from google
                    .enabled(true)
                    .role(Role.USER)
                    .provider(AuthProvider.GOOGLE)
                    .build();
            user = userRepository.save(user);
            log.info("Registered new Google OAuth2 user: {}", email);
        } else {
            // If the user already exists, update and verify/enable them
            boolean updated = false;
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                updated = true;
            }
            if (!user.isEnabled()) {
                user.setEnabled(true);
                updated = true;
            }
            if (user.getProvider() == null || user.getProvider() == AuthProvider.LOCAL) {
                user.setProvider(AuthProvider.GOOGLE); // Upgrade / Link to Google provider
                updated = true;
            }
            if (user.getRole() == null) {
                user.setRole(Role.USER);
                updated = true;
            }
            if (updated) {
                user = userRepository.save(user);
            }
            log.info("Logged in existing user via Google OAuth2: {}", email);
        }

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}
