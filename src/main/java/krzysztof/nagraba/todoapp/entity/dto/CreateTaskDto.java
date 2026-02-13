package krzysztof.nagraba.todoapp.entity.dto;

import krzysztof.nagraba.todoapp.entity.Importance;
import krzysztof.nagraba.todoapp.entity.Urgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class CreateTaskDto {
    @NotBlank
    @Size(min = 3, max = 50)
    private String taskTitle;

    private Urgency urgency;
    private Importance importance;
}
