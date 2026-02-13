package krzysztof.nagraba.todoapp.controller;

import krzysztof.nagraba.todoapp.config.CustomUserDetails;
import krzysztof.nagraba.todoapp.entity.dto.ChangePasswordRequest;
import krzysztof.nagraba.todoapp.entity.dto.UserResponse;
import krzysztof.nagraba.todoapp.service.AuthService;
import krzysztof.nagraba.todoapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(userService.getUserById(customUserDetails.getId()));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal CustomUserDetails customUserDetails, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        authService.changePassword(customUserDetails.getId(), changePasswordRequest.getCurrentPassword(), changePasswordRequest.getNewPassword());

        return ResponseEntity.noContent().build();
    }
}

