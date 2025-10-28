package vn.iotstar;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Chạy class này để tạo BCrypt hash cho password
 * Password: 123456
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String hashedPassword = encoder.encode(rawPassword);
        
        System.out.println("Raw Password: " + rawPassword);
        System.out.println("BCrypt Hash: " + hashedPassword);
        System.out.println("\nCopy hash này vào SQL file:");
        System.out.println("hashed_password = '" + hashedPassword + "'");
    }
}
