package com.talenttrade.service;

import com.talenttrade.dto.UpdateProfileRequest;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.User;
import com.talenttrade.exception.DuplicateResourceException;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "userProfiles", key = "#email")
    public UserResponse getProfile(String email) {
        log.debug("Fetching profile for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    @Transactional
    @org.springframework.cache.annotation.Caching(
        put = {@org.springframework.cache.annotation.CachePut(value = "userProfiles", key = "#email")},
        evict = {
            @org.springframework.cache.annotation.CacheEvict(value = "dashboardStats", key = "#email"),
            @org.springframework.cache.annotation.CacheEvict(value = "platformAnalytics", key = "'analytics'"),
            @org.springframework.cache.annotation.CacheEvict(value = "dashboardSummary", key = "'summary'")
        }
    )
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        log.info("Attempting to update profile for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!user.getUsernameValue().equals(request.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                log.warn("Profile update failed - username already exists: {}", request.getUsername());
                throw new DuplicateResourceException("Username already exists");
            }
            user.setUsernameValue(request.getUsername());
        }

        user.setFullName(request.getFullName());
        user.setBio(request.getBio());
        user.setLocation(request.getLocation());

        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", email);

        return mapToUserResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users paginated");
        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsernameValue())
                .email(user.getEmail())
                .bio(user.getBio())
                .location(user.getLocation())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .provider(user.getProvider() != null ? user.getProvider().name() : null)
                .emailVerified(user.isEmailVerified())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
