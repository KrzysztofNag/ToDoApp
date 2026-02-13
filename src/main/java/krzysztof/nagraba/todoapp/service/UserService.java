package krzysztof.nagraba.todoapp.service;

import krzysztof.nagraba.todoapp.entity.User;
import krzysztof.nagraba.todoapp.entity.dto.UserResponse;
import krzysztof.nagraba.todoapp.exception.UserNotFoundException;
import krzysztof.nagraba.todoapp.mapper.UserMapper;
import krzysztof.nagraba.todoapp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        Page<User> users = userRepository.findAll(pageable);

        return users.map(UserMapper::toUserResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return UserMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
    }
}
