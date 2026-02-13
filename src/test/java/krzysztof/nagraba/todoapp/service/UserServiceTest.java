package krzysztof.nagraba.todoapp.service;

import krzysztof.nagraba.todoapp.entity.Role;
import krzysztof.nagraba.todoapp.entity.User;
import krzysztof.nagraba.todoapp.entity.dto.UserResponse;
import krzysztof.nagraba.todoapp.exception.UserNotFoundException;
import krzysztof.nagraba.todoapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;

    @InjectMocks UserService userService;

    private static Pageable pageable() {
        return PageRequest.of(0, 10, Sort.by("id").descending());
    }

    private static User userWithId(Long id, String email, Role role, boolean enabled) {
        User user = new User(email, "HASH", role);
        user.setEnabled(enabled);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void getAllUsers_shouldReturnMappedPageOfUserResponse() {
        // given
        User u1 = userWithId(1L, "a@b.com", Role.USER, true);
        User u2 = userWithId(2L, "admin@ex.com", Role.ADMIN, false);

        Page<User> page = new PageImpl<>(List.of(u1, u2), pageable(), 2);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        // when
        Page<UserResponse> result = userService.getAllUsers(pageable());

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(UserResponse::getId).containsExactly(1L, 2L);
        assertThat(result.getContent()).extracting(UserResponse::getEmail).containsExactly("a@b.com", "admin@ex.com");
        assertThat(result.getContent()).extracting(UserResponse::getRole).containsExactly(Role.USER, Role.ADMIN);
        assertThat(result.getContent()).extracting(UserResponse::isEnabled).containsExactly(true, false);

        verify(userRepository).findAll(any(Pageable.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserById_shouldReturnMappedUserResponse_whenUserExists() {
        // given
        User user = userWithId(7L, "x@y.com", Role.USER, true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        // when
        UserResponse resp = userService.getUserById(7L);

        // then
        assertThat(resp.getId()).isEqualTo(7L);
        assertThat(resp.getEmail()).isEqualTo("x@y.com");
        assertThat(resp.getRole()).isEqualTo(Role.USER);
        assertThat(resp.isEnabled()).isTrue();

        verify(userRepository).findById(7L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserById_shouldThrowUserNotFoundException_whenUserNotFound() {
        // given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id 99 not found.");

        verify(userRepository).findById(99L);
        verifyNoMoreInteractions(userRepository);
    }
}
