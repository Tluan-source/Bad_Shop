package vn.iotstar.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.User;

import java.util.UUID;

@Service
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder;
    
    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    public String generateSalt() {
        return UUID.randomUUID().toString();
    }
    
    public String hashPassword(String rawPassword, String salt) {
        return passwordEncoder.encode(rawPassword + salt);
    }
    
    public boolean verifyPassword(String rawPassword, String hashedPassword, String salt) {
        return passwordEncoder.matches(rawPassword + salt, hashedPassword);
    }
    
    // Method để tạo user với password đã được hash
    public void createUserWithHashedPassword(User user, String rawPassword) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(rawPassword, salt);
        
        user.setSalt(salt);
        user.setHashedPassword(hashedPassword);
    }
}