package krzysztof.nagraba.todoapp.entity.dto;

import krzysztof.nagraba.todoapp.entity.Importance;
import krzysztof.nagraba.todoapp.entity.TaskStatus;
import krzysztof.nagraba.todoapp.entity.Urgency;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String taskTitle;
    private TaskStatus status;
    private LocalDateTime createdDate;
    private Urgency urgency;
    private Importance importance;
    private String owner;
}
