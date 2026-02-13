package krzysztof.nagraba.todoapp.controller;

import krzysztof.nagraba.todoapp.entity.dto.LoginRequest;
import krzysztof.nagraba.todoapp.entity.dto.RegisterRequest;
import krzysztof.nagraba.todoapp.entity.dto.TokenResponse;
import krzysztof.nagraba.todoapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> response(@RequestBody @Valid RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        String token = authService.login(loginRequest);
        return ResponseEntity.ok(new TokenResponse(token));
    }
}
