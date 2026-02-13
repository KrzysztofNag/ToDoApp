package krzysztof.nagraba.todoapp.repository;

import krzysztof.nagraba.todoapp.entity.Task;
import krzysztof.nagraba.todoapp.entity.TaskStatus;
import krzysztof.nagraba.todoapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task,Long> {
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByOwner(User user, Pageable pageable);

    Optional<Task> findByIdAndOwnerId(Long taskId, Long userId);

    Page <Task> findByOwnerAndStatus(User user, TaskStatus taskStatus, Pageable pageable);

    void deleteAllByOwnerAndStatus(User owner, TaskStatus taskStatus);

}
