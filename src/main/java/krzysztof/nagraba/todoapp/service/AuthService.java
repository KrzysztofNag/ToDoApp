package krzysztof.nagraba.todoapp.service;

import krzysztof.nagraba.todoapp.config.JwtService;
import krzysztof.nagraba.todoapp.entity.Role;
import krzysztof.nagraba.todoapp.entity.User;
import krzysztof.nagraba.todoapp.entity.dto.LoginRequest;
import krzysztof.nagraba.todoapp.entity.dto.RegisterRequest;
import krzysztof.nagraba.todoapp.exception.EmailAlreadyExistsException;
import krzysztof.nagraba.todoapp.exception.UserNotFoundException;
import krzysztof.nagraba.todoapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest registerRequest) {
        String normalizedEmail = registerRequest.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        String passwordHash = passwordEncoder.encode(registerRequest.getPassword());

        userRepository.save(new User(normalizedEmail, passwordHash, Role.USER));
    }

    public String login(LoginRequest loginRequest) {
        String normalizedEmail = loginRequest.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(normalizedEmail).orElseThrow(() -> new IllegalArgumentException("Invalid email!"));

        boolean ok = passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash());

        if(!ok) {
            throw new IllegalArgumentException("Invalid password!");
        }

        String role = user.getRole().name();

        return jwtService.generateToken(loginRequest.getEmail(), List.of(role));

    }

    public void disableUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setEnabled(false);
        userRepository.save(user);
    }

    public void enableUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if(!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password!");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void updateUserRole(Long id, Role role) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        user.setRole(role);
        userRepository.save(user);
    }

}
