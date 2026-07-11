package com.talenttrade.repository;

import com.talenttrade.entity.UserGoogleCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserGoogleCredentialRepository extends JpaRepository<UserGoogleCredential, Long> {
    Optional<UserGoogleCredential> findByUserEmail(String email);
    Optional<UserGoogleCredential> findByUserId(Long userId);
}
