package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens")
@Data
public class OtpToken {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String code; // plain code (short), you may hash it for security

    @Enumerated(EnumType.STRING)
    private OtpType type;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private Boolean used = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum OtpType {
        REGISTER, RESET
    }
}
