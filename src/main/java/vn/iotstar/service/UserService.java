package vn.iotstar.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordService passwordService;
    
    public User registerUser(User user, String rawPassword) {
        // Generate ID
        user.setId("U" + System.currentTimeMillis());
        // ensure inactive until OTP verifies
        user.setStatus(User.UserStatus.INACTIVE);
        
        // Hash password với salt
        passwordService.createUserWithHashedPassword(user, rawPassword);
        
        return userRepository.save(user);
    }
    
    public boolean authenticate(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordService.verifyPassword(rawPassword, user.getHashedPassword());
        }
        return false;
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
    
    public List<User> getShippers() {
        return userRepository.findByRole(User.UserRole.SHIPPER);
    }
    
    public List<User> getActiveUsers() {
        return userRepository.findByStatus(User.UserStatus.ACTIVE);
    }
    
    public User promoteToShipper(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(User.UserRole.SHIPPER);
            return userRepository.save(user);
        }
        return null;
    }
}