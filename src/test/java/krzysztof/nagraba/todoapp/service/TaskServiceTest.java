package krzysztof.nagraba.todoapp.service;

import krzysztof.nagraba.todoapp.entity.*;
import krzysztof.nagraba.todoapp.entity.*;
import krzysztof.nagraba.todoapp.entity.dto.CreateTaskDto;
import krzysztof.nagraba.todoapp.entity.dto.TaskResponse;
import krzysztof.nagraba.todoapp.entity.dto.UpdateTaskDto;
import krzysztof.nagraba.todoapp.exception.TaskNotFoundException;
import krzysztof.nagraba.todoapp.exception.UserNotFoundException;
import krzysztof.nagraba.todoapp.repository.TaskRepository;
import krzysztof.nagraba.todoapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock UserRepository userRepository;

    @InjectMocks TaskService taskService;

    private static Pageable pageable() {
        return PageRequest.of(0, 10, Sort.by("id").descending());
    }

    private static User userWithEmail(String email) {
        return new User(email, "HASH", Role.USER);
    }

    private static Task task(Long id, String title, TaskStatus status, User owner) {
        Task t = new Task();
        org.springframework.test.util.ReflectionTestUtils.setField(t, "id", id);
        t.setTaskTitle(title);
        t.setStatus(status);
        t.setUrgency(Urgency.NOT_URGENT);
        t.setImportance(Importance.NOT_IMPORTANT);
        t.setOwner(owner);
        org.springframework.test.util.ReflectionTestUtils.setField(t, "createdDate", LocalDateTime.of(2025, 1, 1, 12, 0));
        return t;
    }

    private static Page<Task> pageOf(Task... tasks) {
        return new PageImpl<>(java.util.List.of(tasks));
    }

    private static CreateTaskDto createDto(String title) {
        CreateTaskDto dto = new CreateTaskDto();
        dto.setTaskTitle(title);
        dto.setUrgency(Urgency.URGENT);
        dto.setImportance(Importance.IMPORTANT);
        return dto;
    }

    private static UpdateTaskDto updateDto(String title, TaskStatus status) {
        UpdateTaskDto dto = new UpdateTaskDto();
        dto.setTaskTitle(title);
        dto.setStatus(status);
        dto.setUrgency(Urgency.NOT_URGENT);
        dto.setImportance(Importance.NOT_IMPORTANT);
        return dto;
    }


    @Test
    void getAllTasks_shouldCallFindByStatus_whenStatusProvided() {
        // given
        User owner = userWithEmail("a@b.com");
        Task t1 = task(1L, "T1", TaskStatus.TODO, owner);

        when(taskRepository.findByStatus(eq(TaskStatus.TODO), any(Pageable.class)))
                .thenReturn(pageOf(t1));

        // when
        Page<TaskResponse> result = taskService.getAllTasks(TaskStatus.TODO, pageable());

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getOwner()).isEqualTo("a@b.com");

        verify(taskRepository).findByStatus(eq(TaskStatus.TODO), any(Pageable.class));
        verify(taskRepository, never()).findAll(any(Pageable.class));
        verifyNoMoreInteractions(taskRepository, userRepository);
    }

    @Test
    void getAllTasks_shouldCallFindAll_whenStatusNull() {
        // given
        User owner = userWithEmail("a@b.com");
        Task t1 = task(1L, "T1", TaskStatus.DONE, owner);

        when(taskRepository.findAll(any(Pageable.class))).thenReturn(pageOf(t1));

        // when
        Page<TaskResponse> result = taskService.getAllTasks(null, pageable());

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(TaskStatus.DONE);

        verify(taskRepository).findAll(any(Pageable.class));
        verify(taskRepository, never()).findByStatus(any(), any());
        verifyNoMoreInteractions(taskRepository, userRepository);
    }


    @Test
    void getAllUserTasks_shouldThrow_whenUserNotFound() {
        // given
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> taskService.getAllUserTasks(10L, TaskStatus.TODO, pageable()))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(10L);
        verifyNoInteractions(taskRepository);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAllUserTasks_shouldCallFindByOwnerAndStatus_whenStatusProvided() {
        // given
        User user = userWithEmail("user@ex.com");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        Task t1 = task(1L, "T1", TaskStatus.IN_PROGRESS, user);
        when(taskRepository.findByOwnerAndStatus(eq(user), eq(TaskStatus.IN_PROGRESS), any(Pageable.class)))
                .thenReturn(pageOf(t1));

        // when
        Page<TaskResponse> result = taskService.getAllUserTasks(5L, TaskStatus.IN_PROGRESS, pageable());

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getOwner()).isEqualTo("user@ex.com");

        verify(userRepository).findById(5L);
        verify(taskRepository).findByOwnerAndStatus(eq(user), eq(TaskStatus.IN_PROGRESS), any(Pageable.class));
        verify(taskRepository, never()).findByOwner(any(), any());
        verifyNoMoreInteractions(taskRepository, userRepository);
    }

    @Test
    void getAllUserTasks_shouldCallFindByOwner_whenStatusNull() {
        // given
        User user = userWithEmail("user@ex.com");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        Task t1 = task(1L, "T1", TaskStatus.TODO, user);
        when(taskRepository.findByOwner(eq(user), any(Pageable.class))).thenReturn(pageOf(t1));

        // when
        Page<TaskResponse> result = taskService.getAllUserTasks(5L, null, pageable());

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(userRepository).findById(5L);
        verify(taskRepository).findByOwner(eq(user), any(Pageable.class));
        verify(taskRepository, never()).findByOwnerAndStatus(any(), any(), any());
        verifyNoMoreInteractions(taskRepository, userRepository);
    }


    @Test
    void getMyTask_shouldReturnTask_whenExistsAndOwned() {
        // given
        User owner = userWithEmail("me@ex.com");
        Task t = task(7L, "Mine", TaskStatus.TODO, owner);

        when(taskRepository.findByIdAndOwnerId(7L, 1L)).thenReturn(Optional.of(t));

        // when
        TaskResponse resp = taskService.getMyTask(7L, 1L);

        // then
        assertThat(resp.getId()).isEqualTo(7L);
        assertThat(resp.getOwner()).isEqualTo("me@ex.com");

        verify(taskRepository).findByIdAndOwnerId(7L, 1L);
        verifyNoMoreInteractions(taskRepository, userRepository);
    }

    @Test
    void getMyTask_shouldThrow_whenNotFoundOrNotOwned() {
        // given
        when(taskRepository.findByIdAndOwnerId(7L, 1L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> taskService.getMyTask(7L, 1L))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository).findByIdAndOwnerId(7L, 1L);
        verifyNoMoreInteractions(taskRepository, userRepository);
    }


    @Test
    void getMyTasks_shouldThrow_whenUserNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> taskService.getMyTasks(1L, pageable()))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(1L);
        verifyNoInteractions(taskRepository);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getMyTasks_shouldReturnTasks_forUser() {
        // given
        User user = userWithEmail("me@ex.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Task t1 = task(1L, "T1", TaskStatus.TODO, user);
        Task t2 = task(2L, "T2", TaskStatus.DONE, user);

        when(taskRepository.findByOwner(eq(user), any(Pageable.class))).thenReturn(pageOf(t1, t2));

        // when
        Page<TaskResponse> result = taskService.getMyTasks(1L, pageable());

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(TaskResponse::getId).containsExactly(1L, 2L);

        verify(userRepository).findById(1L);
        verify(taskRepository).findByOwner(eq(user), any(Pageable.class));
        verifyNoMoreInteractions(taskRepository, userRepository);
    }


    @Test
    void createMyTask_shouldSetOwner_saveAndReturnResponse() {
        // given
        User user = userWithEmail("me@ex.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        CreateTaskDto dto = createDto("New Task");

        // repo save zwróci "ten sam" obiekt - w praktyce JPA może zwrócić z id.
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task saved = inv.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", 123L);
            org.springframework.test.util.ReflectionTestUtils.setField(saved, "createdDate", LocalDateTime.of(2025, 1, 2, 10, 0));
            saved.setStatus(TaskStatus.TODO);
            return saved;
        });

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        // when
        TaskResponse resp = taskService.createMyTask(dto, 1L);

        // then
        verify(userRepository).findById(1L);
        verify(taskRepository).save(captor.capture());

        Task saved = captor.getValue();
        assertThat(saved.getTaskTitle()).isEqualTo("New Task");
        assertThat(saved.getOwner()).isSameAs(user);
        assertThat(saved.getUrgency()).isEqualTo(Urgency.URGENT);
        assertThat(saved.getImportance()).isEqualTo(Importance.IMPORTANT);

        assertThat(resp.getId()).isEqualTo(123L);
        assertThat(resp.getOwner()).isEqualTo("me@ex.com");

        verifyNoMoreInteractions(taskRepository, userRepository);
    }

    @Test
    void createMyTask_shouldThrow_whenUserNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> taskService.createMyTask(createDto("New Task"), 1L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(1L);
        verifyNoInteractions(taskRepository);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateMyTask_shouldUpdateEntityFields_whenOwned() {
        // given
        User owner = userWithEmail("me@ex.com");
        Task t = task(7L, "Old", TaskStatus.TODO, owner);

        when(taskRepository.findByIdAndOwnerId(7L, 1L)).thenReturn(Optional.of(t));

        UpdateTaskDto dto = updateDto("New Title", TaskStatus.DONE);

        // when
        taskService.updateMyTask(7L, dto, 1L);

        // then
        assertThat(t.getTaskTitle()).isEqualTo("New Title");
        assertThat(t.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(t.getUrgency()).isEqualTo(Urgency.NOT_URGENT);
        assertThat(t.getImportance()).isEqualTo(Importance.NOT_IMPORTANT);

        verify(taskRepository).findByIdAndOwnerId(7L, 1L);
        verify(taskRepository, never()).save(any());
        verifyNoMoreInteractions(taskRepository, userRepository);
    }

    @Test
    void updateMyTask_shouldThrow_whenTaskNotOwnedOrNotFound() {
        // given
        when(taskRepository.findByIdAndOwnerId(7L, 1L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> taskService.updateMyTask(7L, updateDto("X", TaskStatus.TODO), 1L))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository).findByIdAndOwnerId(7L, 1L);
        verifyNoMoreInteractions(taskRepository, userRepository);
    }

    @Test
    void updateMyStatus_shouldSetStatus_whenOwned() {
        // given
        User owner = userWithEmail("me@ex.com");
        Task t = task(7L, "Mine", TaskStatus.TODO, owner);

        when(taskRepository.findByIdAndOwnerId(7L, 1L)).thenReturn(Optional.of(t));

        // when
        taskService.updateMyStatus(7L, TaskStatus.IN_PROGRESS, 1L);

        // then
        assertThat(t.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

        verify(taskRepository).findByIdAndOwnerId(7L, 1L);
        verifyNoMoreInteractions(taskRepository, userRepository);
    }

    @Test
    void updateMyStatus_shouldThrow_whenTaskNotFoundOrNotOwned() {
        // given
        when(taskRepository.findByIdAndOwnerId(7L, 1L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> taskService.updateMyStatus(7L, TaskStatus.DONE, 1L))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository).findByIdAndOwnerId(7L, 1L);
        verifyNoMoreInteractions(taskRepository, userRepository);
    }

    @Test
    void deleteMyTask_shouldDelete_whenOwned() {
        // given
        User owner = userWithEmail("me@ex.com");
        Task t = task(7L, "Mine", TaskStatus.TODO, owner);

        when(taskRepository.findByIdAndOwnerId(7L, 1L)).thenReturn(Optional.of(t));

        // when
        taskService.deleteMyTask(7L, 1L);

        // then
        verify(taskRepository).findByIdAndOwnerId(7L, 1L);
        verify(taskRepository).delete(t);
        verifyNoMoreInteractions(taskRepository, userRepository);
    }

    @Test
    void deleteMyTask_shouldThrow_whenTaskNotFoundOrNotOwned() {
        // given
        when(taskRepository.findByIdAndOwnerId(7L, 1L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> taskService.deleteMyTask(7L, 1L))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository).findByIdAndOwnerId(7L, 1L);
        verify(taskRepository, never()).delete(any());
        verifyNoMoreInteractions(taskRepository, userRepository);
    }

    @Test
    void deleteMyTasksByStatus_shouldCallRepoDeleteAll_whenUserExists() {
        // given
        User user = userWithEmail("me@ex.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        taskService.deleteMyTasksByStatus(1L, TaskStatus.DONE);

        // then
        verify(userRepository).findById(1L);
        verify(taskRepository).deleteAllByOwnerAndStatus(user, TaskStatus.DONE);
        verifyNoMoreInteractions(taskRepository, userRepository);
    }

    @Test
    void deleteMyTasksByStatus_shouldThrow_whenUserNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> taskService.deleteMyTasksByStatus(1L, TaskStatus.DONE))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(1L);
        verifyNoInteractions(taskRepository);
        verifyNoMoreInteractions(userRepository);
    }
}
