package com.talenttrade.repository;

import com.talenttrade.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    long countByEnabled(boolean enabled);
    long countByEmailVerified(boolean emailVerified);
    long countByProvider(com.talenttrade.entity.AuthProvider provider);
    long countByRole(com.talenttrade.entity.Role role);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:provider IS NULL OR u.provider = :provider) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled)")
    List<User> searchAndFilterUsers(
            @org.springframework.data.repository.query.Param("query") String query,
            @org.springframework.data.repository.query.Param("role") com.talenttrade.entity.Role role,
            @org.springframework.data.repository.query.Param("provider") com.talenttrade.entity.AuthProvider provider,
            @org.springframework.data.repository.query.Param("enabled") Boolean enabled
    );
}
