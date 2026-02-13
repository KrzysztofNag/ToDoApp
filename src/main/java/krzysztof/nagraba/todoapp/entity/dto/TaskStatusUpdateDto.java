package krzysztof.nagraba.todoapp.entity.dto;

import krzysztof.nagraba.todoapp.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusUpdateDto {
    @NotNull
    private TaskStatus status;
}
