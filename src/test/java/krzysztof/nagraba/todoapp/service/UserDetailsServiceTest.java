package krzysztof.nagraba.todoapp.service;

import krzysztof.nagraba.todoapp.config.CustomUserDetails;
import krzysztof.nagraba.todoapp.entity.Role;
import krzysztof.nagraba.todoapp.entity.User;
import krzysztof.nagraba.todoapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceTest {

    @Mock UserRepository userRepository;

    @InjectMocks UserDetailsService userDetailsService;

    private static User userWithId(Long id, String email, String passwordHash, Role role, boolean enabled) {
        User user = new User(email, passwordHash, role);
        user.setEnabled(enabled);

        ReflectionTestUtils.setField(user, "id", id);

        return user;
    }

    @Test
    void loadUserByUsername_shouldNormalizeEmail_andReturnCustomUserDetails_whenUserExists() {
        // given
        User user = userWithId(7L, "test@example.com", "HASH", Role.ADMIN, true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // when
        UserDetails details = userDetailsService.loadUserByUsername("  TeSt@Example.com  ");

        // then
        assertThat(details).isInstanceOf(CustomUserDetails.class);

        CustomUserDetails cud = (CustomUserDetails) details;
        assertThat(cud.getId()).isEqualTo(7L);
        assertThat(cud.getEmail()).isEqualTo("test@example.com");
        assertThat(cud.getPasswordHash()).isEqualTo("HASH");
        assertThat(cud.getRole()).isEqualTo(Role.ADMIN);
        assertThat(cud.isEnabled()).isTrue();

        // authorities
        assertThat(cud.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");

        verify(userRepository).findByEmail("test@example.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        // given
        when(userRepository.findByEmail("noone@ex.com")).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("  NoOne@Ex.com  "))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found.");

        verify(userRepository).findByEmail("noone@ex.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_shouldPreserveEnabledFlagFromUserEntity() {
        // given
        User user = userWithId(1L, "a@b.com", "HASH", Role.USER, false);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        // when
        CustomUserDetails details = (CustomUserDetails) userDetailsService.loadUserByUsername("a@b.com");

        // then
        assertThat(details.isEnabled()).isFalse();

        verify(userRepository).findByEmail("a@b.com");
        verifyNoMoreInteractions(userRepository);
    }
}
