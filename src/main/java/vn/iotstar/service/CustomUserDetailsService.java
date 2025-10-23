package vn.iotstar.service;

import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByEmail(username);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByPhone(username);
        }
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        User user = userOpt.get();
        String role = user.getRole() != null ? user.getRole().name() : "USER";

        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getHashedPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_" + role))
                .accountLocked(user.getStatus() == User.UserStatus.BANNED)
                .disabled(user.getStatus() != User.UserStatus.ACTIVE)
                .build();
    }
}