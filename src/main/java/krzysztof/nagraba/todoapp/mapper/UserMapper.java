package krzysztof.nagraba.todoapp.mapper;

import krzysztof.nagraba.todoapp.entity.User;
import krzysztof.nagraba.todoapp.entity.dto.UserResponse;

public class UserMapper {

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
        );
    }
}
