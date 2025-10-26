package vn.iotstar.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.PasswordService;

import java.util.Optional;

@Service
public class UserProfileService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordService passwordService;

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public User updateProfile(User user) {
        return userRepository.save(user);
    }
    
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordService.verifyPassword(oldPassword, user.getHashedPassword())) {
                passwordService.createUserWithHashedPassword(user, newPassword);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
    
    public boolean updatePhone(String userId, String newPhone) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPhone(newPhone);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    public boolean updateFullName(String userId, String newFullName) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFullName(newFullName);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
