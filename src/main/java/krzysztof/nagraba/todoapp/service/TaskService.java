package krzysztof.nagraba.todoapp.service;

import krzysztof.nagraba.todoapp.entity.User;
import krzysztof.nagraba.todoapp.exception.TaskNotFoundException;
import krzysztof.nagraba.todoapp.exception.UserNotFoundException;
import krzysztof.nagraba.todoapp.mapper.TaskMapper;
import krzysztof.nagraba.todoapp.entity.Task;
import krzysztof.nagraba.todoapp.entity.TaskStatus;
import krzysztof.nagraba.todoapp.entity.dto.CreateTaskDto;
import krzysztof.nagraba.todoapp.entity.dto.TaskResponse;
import krzysztof.nagraba.todoapp.entity.dto.UpdateTaskDto;
import krzysztof.nagraba.todoapp.repository.TaskRepository;
import krzysztof.nagraba.todoapp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    //For admin

    //All tasks
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(
            TaskStatus status,
            Pageable pageable) {

        Page<Task> page;
        if(status != null) {
            page = taskRepository.findByStatus(status, pageable);
        } else {
            page = taskRepository.findAll(pageable);
        }

        return page.map(TaskMapper::toResponse);
    }

    //All tasks per User
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllUserTasks(Long userId, TaskStatus taskStatus, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Page<Task> page;

        if(taskStatus != null) {
            page = taskRepository.findByOwnerAndStatus(user, taskStatus, pageable);
        } else {
            page = taskRepository.findByOwner(user, pageable);
        }

        return page.map(TaskMapper::toResponse);
    }

    //For user

    @Transactional(readOnly = true)
    public TaskResponse getMyTask(Long taskId, Long userId) {
        Task task = taskRepository.findByIdAndOwnerId(taskId, userId).orElseThrow(() -> new TaskNotFoundException(taskId));
        return TaskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getMyTasks(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        return taskRepository.findByOwner(user, pageable)
                .map(TaskMapper::toResponse);
    }

    @Transactional
    public TaskResponse createMyTask(CreateTaskDto createTaskDto, Long userId) {
        Task task = TaskMapper.toNewEntity(createTaskDto);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        task.setOwner(user);
        Task savedTask = taskRepository.save(task);

        return TaskMapper.toResponse(savedTask);
    }

    @Transactional
    public void updateMyTask(Long taskId, UpdateTaskDto updateTaskDto, Long userId) {
        Task task = taskRepository.findByIdAndOwnerId(taskId, userId).orElseThrow(() -> new TaskNotFoundException(taskId));

        TaskMapper.updateEntity(task, updateTaskDto);
    }

    @Transactional
    public void updateMyStatus(Long taskId, TaskStatus newStatus, Long userId) {
        Task task = taskRepository.findByIdAndOwnerId(taskId, userId).orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setStatus(newStatus);
    }

    @Transactional
    public void deleteMyTask(Long taskId, Long userId) {
        Task task = taskRepository.findByIdAndOwnerId(taskId, userId).orElseThrow(() -> new TaskNotFoundException(taskId));

        taskRepository.delete(task);
    }

    @Transactional
    public void deleteMyTasksByStatus(Long userId, TaskStatus taskStatus) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        taskRepository.deleteAllByOwnerAndStatus(user, taskStatus);
    }
}
