package krzysztof.nagraba.todoapp.service;

import krzysztof.nagraba.todoapp.config.JwtService;
import krzysztof.nagraba.todoapp.entity.Role;
import krzysztof.nagraba.todoapp.entity.User;
import krzysztof.nagraba.todoapp.entity.dto.LoginRequest;
import krzysztof.nagraba.todoapp.entity.dto.RegisterRequest;
import krzysztof.nagraba.todoapp.exception.EmailAlreadyExistsException;
import krzysztof.nagraba.todoapp.exception.UserNotFoundException;
import krzysztof.nagraba.todoapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @InjectMocks AuthService authService;

    private static RegisterRequest registerReq(String email, String password) {
        RegisterRequest req = new RegisterRequest();
        ReflectionTestUtils.setField(req, "email", email);
        ReflectionTestUtils.setField(req, "password", password);
        return req;
    }

    private static LoginRequest loginReq(String email, String password) {
        LoginRequest req = new LoginRequest();
        ReflectionTestUtils.setField(req, "email", email);
        ReflectionTestUtils.setField(req, "password", password);
        return req;
    }

    @Test
    void register_shouldNormalizeEmail_encodePassword_andSaveUser() {
        // given
        RegisterRequest req = registerReq("  TeSt@Example.com  ", "secret");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("HASH");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // when
        authService.register(req);

        // then
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("secret");
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("HASH");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.isEnabled()).isTrue();

        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        // given
        RegisterRequest req = registerReq("  A@B.com ", "pw");
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);

        // when + then
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists: a@b.com");

        verify(userRepository).existsByEmail("a@b.com");
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void login_shouldGenerateToken_whenEmailAndPasswordCorrect() {
        // given
        LoginRequest req = loginReq("  TeSt@Example.com  ", "secret");

        User user = new User("test@example.com", "HASH", Role.USER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "HASH")).thenReturn(true);

        when(jwtService.generateToken(anyString(), anyList())).thenReturn("TOKEN");

        // when
        String token = authService.login(req);

        // then
        assertThat(token).isEqualTo("TOKEN");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("secret", "HASH");

        verify(jwtService).generateToken("  TeSt@Example.com  ", List.of("USER"));

        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void login_shouldThrow_whenEmailNotFound() {
        // given
        LoginRequest req = loginReq("  noone@ex.com  ", "secret");
        when(userRepository.findByEmail("noone@ex.com")).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email!");

        verify(userRepository).findByEmail("noone@ex.com");
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void login_shouldThrow_whenPasswordInvalid() {
        // given
        LoginRequest req = loginReq("a@b.com", "wrong");

        User user = new User("a@b.com", "HASH", Role.ADMIN);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        // when + then
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid password!");

        verify(userRepository).findByEmail("a@b.com");
        verify(passwordEncoder).matches("wrong", "HASH");
        verifyNoInteractions(jwtService);
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    @Test
    void disableUser_shouldSetEnabledFalse_andSave() {
        // given
        User user = new User("a@b.com", "HASH", Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        // when
        authService.disableUser(1L);

        // then
        verify(userRepository).findById(1L);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.isEnabled()).isFalse();

        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void enableUser_shouldSetEnabledTrue_andSave() {
        // given
        User user = new User("a@b.com", "HASH", Role.USER);
        user.setEnabled(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        // when
        authService.enableUser(2L);

        // then
        verify(userRepository).findById(2L);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.isEnabled()).isTrue();

        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void disableUser_shouldThrow_whenUserNotFound() {
        // given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> authService.disableUser(99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(99L);
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void changePassword_shouldEncodeNewPassword_andSave_whenCurrentMatches() {
        // given
        User user = new User("a@b.com", "OLD_HASH", Role.USER);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("current", "OLD_HASH")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("NEW_HASH");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        // when
        authService.changePassword(5L, "current", "newPass");

        // then
        verify(userRepository).findById(5L);
        verify(passwordEncoder).matches("current", "OLD_HASH");
        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getPasswordHash()).isEqualTo("NEW_HASH");

        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void changePassword_shouldThrow_whenCurrentPasswordInvalid() {
        // given
        User user = new User("a@b.com", "OLD_HASH", Role.USER);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "OLD_HASH")).thenReturn(false);

        // when + then
        assertThatThrownBy(() -> authService.changePassword(5L, "wrong", "newPass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid password!");

        verify(userRepository).findById(5L);
        verify(passwordEncoder).matches("wrong", "OLD_HASH");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void updateUserRole_shouldSetRole_andSave() {
        // given
        User user = new User("a@b.com", "HASH", Role.USER);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        // when
        authService.updateUserRole(7L, Role.ADMIN);

        // then
        verify(userRepository).findById(7L);
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);

        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService);
    }
}
