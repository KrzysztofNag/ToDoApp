package krzysztof.nagraba.todoapp.entity.dto;

import krzysztof.nagraba.todoapp.entity.Importance;
import krzysztof.nagraba.todoapp.entity.TaskStatus;
import krzysztof.nagraba.todoapp.entity.Urgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UpdateTaskDto {

    @NotBlank
    @Size(min = 3, max = 50)
    private String taskTitle;

    @NotNull
    private TaskStatus status;

    @NotNull
    private Urgency urgency;

    @NotNull
    private Importance importance;
}
