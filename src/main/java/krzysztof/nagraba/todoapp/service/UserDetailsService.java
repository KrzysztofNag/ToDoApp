package krzysztof.nagraba.todoapp.service;

import krzysztof.nagraba.todoapp.repository.UserRepository;
import krzysztof.nagraba.todoapp.config.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .map(user -> new CustomUserDetails(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole(), user.isEnabled()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
}
