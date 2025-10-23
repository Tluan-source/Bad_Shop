package vn.iotstar.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.iotstar.entity.User;

@Service
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder;
    
    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


    //Bỏ salt không dùng nữa
    // public String generateSalt() {
    //     return UUID.randomUUID().toString();
    // }
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
    
    // Method để tạo user với password đã được hash
    public void createUserWithHashedPassword(User user, String rawPassword) {
        // String salt = generateSalt();
        String hashedPassword = hashPassword(rawPassword);
        
        // user.setSalt(salt);
        user.setHashedPassword(hashedPassword);
    }
}