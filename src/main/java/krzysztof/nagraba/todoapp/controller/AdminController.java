package krzysztof.nagraba.todoapp.controller;

import krzysztof.nagraba.todoapp.entity.TaskStatus;
import krzysztof.nagraba.todoapp.entity.dto.TaskResponse;
import krzysztof.nagraba.todoapp.entity.dto.UpdateRoleRequest;
import krzysztof.nagraba.todoapp.entity.dto.UserResponse;
import krzysztof.nagraba.todoapp.service.AuthService;
import krzysztof.nagraba.todoapp.service.TaskService;
import krzysztof.nagraba.todoapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TaskService taskService;
    private final AuthService authService;
    private final UserService userService;

    public AdminController(TaskService taskService, AuthService authService, UserService userService) {
        this.taskService = taskService;
        this.authService = authService;
        this.userService = userService;
    }

    //Task endpoints
    @GetMapping("/tasks")
    public ResponseEntity<Page<TaskResponse>> listTasks(
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getAllTasks(status, pageable));
    }

    @GetMapping("/{userId}/tasks")
    public ResponseEntity<Page<TaskResponse>> listTasksPerUser(
            @PathVariable Long userId,
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(taskService.getAllUserTasks(userId, status, pageable));
    }

    //User endpoints
    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable Long id){
        authService.disableUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable Long id){
        authService.enableUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest updateRoleRequest) {
        authService.updateUserRole(id, updateRoleRequest.getRole());
        return ResponseEntity.noContent().build();
    }

}
