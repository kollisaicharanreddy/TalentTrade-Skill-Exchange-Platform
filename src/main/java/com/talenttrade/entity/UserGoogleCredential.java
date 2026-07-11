package com.talenttrade.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_google_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGoogleCredential {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(length = 2048, nullable = false)
    private String accessToken;

    @Column(length = 2048)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime tokenExpiresAt;
}
