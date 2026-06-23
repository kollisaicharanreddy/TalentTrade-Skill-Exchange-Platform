package com.talenttrade.service;

import com.talenttrade.dto.UpdateProfileRequest;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.User;
import com.talenttrade.exception.DuplicateResourceException;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        log.debug("Fetching profile for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    @Transactional
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

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsernameValue())
                .email(user.getEmail())
                .bio(user.getBio())
                .location(user.getLocation())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
