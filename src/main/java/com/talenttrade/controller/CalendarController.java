package com.talenttrade.controller;

import com.talenttrade.entity.User;
import com.talenttrade.entity.UserGoogleCredential;
import com.talenttrade.repository.UserGoogleCredentialRepository;
import com.talenttrade.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Google Calendar API", description = "Endpoints for managing Google Calendar integration and OAuth connection flow.")
public class CalendarController {

    private final UserGoogleCredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @GetMapping("/auth-url")
    @Operation(summary = "Generate Google OAuth2 authorization URL")
    public ResponseEntity<Map<String, String>> getAuthUrl(@RequestParam String redirectUri) {
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/calendar.events" +
                "&access_type=offline" +
                "&prompt=consent";

        Map<String, String> response = new HashMap<>();
        response.put("url", authUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exchange")
    @Operation(summary = "Exchange authorization code for access and refresh tokens")
    public ResponseEntity<?> exchangeCode(@AuthenticationPrincipal UserDetails userDetails, @RequestBody ExchangeRequestDto requestDto) {
        String email = userDetails.getUsername();
        log.info("Exchanging OAuth code for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        try {
            String tokenUrl = "https://oauth2.googleapis.com/token";
            Map<String, String> request = new HashMap<>();
            request.put("client_id", clientId);
            request.put("client_secret", clientSecret);
            request.put("code", requestDto.getCode());
            request.put("redirect_uri", requestDto.getRedirectUri());
            request.put("grant_type", "authorization_code");

            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                Map<String, Object> body = responseEntity.getBody();
                String accessToken = (String) body.get("access_token");
                String refreshToken = (String) body.get("refresh_token");
                Integer expiresIn = (Integer) body.get("expires_in");

                UserGoogleCredential credential = credentialRepository.findByUserId(user.getId())
                        .orElse(new UserGoogleCredential());

                credential.setUser(user);
                credential.setAccessToken(accessToken);
                if (refreshToken != null) {
                    credential.setRefreshToken(refreshToken);
                }
                credential.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));

                credentialRepository.save(credential);
                log.info("Successfully saved Google credentials for user ID: {}", user.getId());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Google Calendar successfully connected");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("Google API returned error status during token exchange");
            }
        } catch (Exception e) {
            log.error("Error during Google OAuth code exchange", e);
            return ResponseEntity.internalServerError().body("Failed to complete Google OAuth exchange: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Check Google Calendar integration connection status")
    public ResponseEntity<Map<String, Object>> getStatus(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<UserGoogleCredential> credential = credentialRepository.findByUserEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("connected", credential.isPresent());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/disconnect")
    @Operation(summary = "Disconnect Google Calendar integration and revoke tokens")
    public ResponseEntity<Map<String, Object>> disconnect(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<UserGoogleCredential> credential = credentialRepository.findByUserEmail(email);

        if (credential.isPresent()) {
            credentialRepository.delete(credential.get());
            log.info("Disconnected Google Calendar integration for user: {}", email);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Google Calendar successfully disconnected");
        return ResponseEntity.ok(response);
    }

    @Data
    public static class ExchangeRequestDto {
        private String code;
        private String redirectUri;
    }
}
