package com.talenttrade.controller;

import com.talenttrade.dto.ApiResponse;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.Skill;
import com.talenttrade.entity.User;
import com.talenttrade.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Platform Management", description = "Admin dashboard statistics, users registry controls, and system health status checks.")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/summary")
    @Operation(summary = "Get admin dashboard statistics summary", description = "Aggregates overall system statistics across users, skills, sessions, and ratings.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardSummary(), "Summary fetched successfully"));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get admin platform analytics details", description = "Retrieves registrations per month, top requested skills, active users, and ratings distributions.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPlatformAnalytics(), "Analytics fetched successfully"));
    }

    @GetMapping("/users")
    @Operation(summary = "Search and filter platform users", description = "Lists platform users with full filtering capabilities (status, role, provider) and query-based search.")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Boolean enabled
    ) {
        List<UserResponse> users = adminService.searchAndFilterUsers(query, role, provider, enabled).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users, "Users fetched successfully"));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Activate or deactivate a user account", description = "Enables or disables a user account, changing their active login status.")
    public ResponseEntity<ApiResponse<UserResponse>> setUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled
    ) {
        User user = adminService.setUserStatus(id, enabled);
        return ResponseEntity.ok(ApiResponse.success(mapToUserResponse(user), "User status updated successfully"));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user authorization role", description = "Modifies a user's role on the platform, upgrading them to ADMIN or downgrading to USER.")
    public ResponseEntity<ApiResponse<UserResponse>> setUserRole(
            @PathVariable Long id,
            @RequestParam String role
    ) {
        User user = adminService.setUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success(mapToUserResponse(user), "User role updated successfully"));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user account", description = "Removes a user's profile completely from the database.")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    @GetMapping("/skills")
    @Operation(summary = "Retrieve all skills in registry", description = "Retrieves all standard skills available in the central registry.")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkills() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllSkills(), "Skills fetched successfully"));
    }

    @PostMapping("/skills")
    @Operation(summary = "Register a new standard skill", description = "Registers a new skill configuration under a specified category.")
    public ResponseEntity<ApiResponse<Skill>> addSkill(@RequestBody Skill skill) {
        return ResponseEntity.ok(ApiResponse.success(adminService.addSkill(skill), "Skill added successfully"));
    }

    @DeleteMapping("/skills/{id}")
    @Operation(summary = "Remove a standard skill", description = "Removes a skill registry entry from the platform.")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(@PathVariable Long id) {
        adminService.deleteSkill(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Skill deleted successfully"));
    }

    @GetMapping("/skills/usage")
    @Operation(summary = "Retrieve skill usage metrics", description = "Fetches metrics detailing which skills are requested and taught most frequently.")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getSkillUsage() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getSkillUsage(), "Skill usage metrics fetched successfully"));
    }

    @GetMapping("/health")
    @Operation(summary = "Retrieve database and application health status", description = "Displays basic memory usage, platform version, and PostgreSQL connection health.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getSystemHealth(), "Platform health status retrieved"));
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
