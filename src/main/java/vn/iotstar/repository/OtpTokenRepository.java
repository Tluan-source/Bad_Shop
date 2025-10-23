package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.OtpToken;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, String> {
    Optional<OtpToken> findByUserIdAndCodeAndType(String userId, String code, OtpToken.OtpType type);
    Optional<OtpToken> findByUserIdAndTypeAndUsedFalse(String userId, OtpToken.OtpType type);
}
