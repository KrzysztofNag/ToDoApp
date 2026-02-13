package krzysztof.nagraba.todoapp.mapper;

import krzysztof.nagraba.todoapp.entity.Task;
import krzysztof.nagraba.todoapp.entity.dto.CreateTaskDto;
import krzysztof.nagraba.todoapp.entity.dto.TaskResponse;
import krzysztof.nagraba.todoapp.entity.dto.UpdateTaskDto;

public class TaskMapper {

    private TaskMapper() {}

    public static Task toNewEntity(CreateTaskDto createTaskDto) {
        Task task = new Task();
        task.setTaskTitle(createTaskDto.getTaskTitle());
        task.setUrgency(createTaskDto.getUrgency());
        task.setImportance(createTaskDto.getImportance());
        return task;
    }

    public static void updateEntity(Task task, UpdateTaskDto updateTaskDto) {
        task.setTaskTitle(updateTaskDto.getTaskTitle());
        task.setStatus(updateTaskDto.getStatus());
        task.setUrgency(updateTaskDto.getUrgency());
        task.setImportance(updateTaskDto.getImportance());
    }

    public static TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTaskTitle(),
                task.getStatus(),
                task.getCreatedDate(),
                task.getUrgency(),
                task.getImportance(),
                task.getOwner().getEmail()
        );
    }
}
