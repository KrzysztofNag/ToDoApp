package krzysztof.nagraba.todoapp.controller;

import krzysztof.nagraba.todoapp.entity.TaskStatus;
import krzysztof.nagraba.todoapp.entity.dto.CreateTaskDto;
import krzysztof.nagraba.todoapp.entity.dto.TaskResponse;
import krzysztof.nagraba.todoapp.entity.dto.TaskStatusUpdateDto;
import krzysztof.nagraba.todoapp.entity.dto.UpdateTaskDto;
import krzysztof.nagraba.todoapp.config.CustomUserDetails;
import krzysztof.nagraba.todoapp.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/tasks")
public class UserTaskController {
    private final TaskService taskService;
    public UserTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getMyTasks(@AuthenticationPrincipal CustomUserDetails customUserDetails, Pageable pageable) {
        return ResponseEntity.ok().body(taskService.getMyTasks(customUserDetails.getId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getMyTaskById(@PathVariable Long id,  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(taskService.getMyTask(id, customUserDetails.getId()));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createMyTask(@Valid @RequestBody CreateTaskDto createTaskDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        TaskResponse response = taskService.createMyTask(createTaskDto, customUserDetails.getId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateMyTask(@PathVariable Long id, @Valid @RequestBody UpdateTaskDto updateTaskDto,  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        taskService.updateMyTask(id, updateTaskDto, customUserDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> patchStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusUpdateDto taskStatusPatchDto,  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        taskService.updateMyStatus(id, taskStatusPatchDto.getStatus(), customUserDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        taskService.deleteMyTask(id, customUserDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllByStatus(@RequestParam TaskStatus taskStatus, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        taskService.deleteMyTasksByStatus(customUserDetails.getId(), taskStatus);
        return ResponseEntity.noContent().build();
    }
}
