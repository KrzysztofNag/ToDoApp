package krzysztof.nagraba.todoapp.entity.dto;

import krzysztof.nagraba.todoapp.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateRoleRequest {
    @NotNull
    private Role role;
}
