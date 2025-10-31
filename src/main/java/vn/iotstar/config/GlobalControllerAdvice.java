package vn.iotstar.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import vn.iotstar.entity.User;
import vn.iotstar.service.UserService;

@ControllerAdvice
public class GlobalControllerAdvice {
    
    @Autowired
    private UserService userService;
    
    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            Optional<User> userOpt = userService.findByEmail(authentication.getName());
            return userOpt.orElse(null);
        }
        return null;
    }
}

