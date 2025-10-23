package vn.iotstar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OtpTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OtpService {

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private MailService mailService;

    // generate numeric OTP (6 digits)
    public String generateOtpCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(code);
    }

    public OtpToken createOtpForUser(User user, OtpToken.OtpType type, int minutesValid) {
        OtpToken token = new OtpToken();
        token.setId(UUID.randomUUID().toString());
        token.setUser(user);
        String code = generateOtpCode();
        token.setCode(code);
        token.setType(type);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(minutesValid));
        token.setUsed(false);
        otpTokenRepository.save(token);

        // send email
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            String subject = type == OtpToken.OtpType.REGISTER ? "OTP kích hoạt tài khoản" : "OTP khôi phục mật khẩu";
            String text = String.format("Mã OTP của bạn là: %s\nHết hạn trong %d phút.", code, minutesValid);
            mailService.sendSimpleMessage(user.getEmail(), subject, text);
        }

        return token;
    }

    public boolean verifyOtp(User user, String code, OtpToken.OtpType type) {
        Optional<OtpToken> opt = otpTokenRepository.findByUserIdAndCodeAndType(user.getId(), code, type);
        if (opt.isPresent()) {
            OtpToken token = opt.get();
            if (token.getUsed() != null && token.getUsed()) return false;
            if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now())) return false;
            token.setUsed(true);
            otpTokenRepository.save(token);
            return true;
        }
        return false;
    }
}
