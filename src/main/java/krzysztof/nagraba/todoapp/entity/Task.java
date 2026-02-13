package krzysztof.nagraba.todoapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 50, name = "task_title")
    private String taskTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false, name = "created_date")
    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Urgency urgency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Importance importance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @PrePersist
    public void onCreate() {
        if (createdDate == null) createdDate = LocalDateTime.now();
        if (status == null) status = TaskStatus.TODO;
        if (urgency == null) urgency = Urgency.NOT_URGENT;
        if (importance == null) importance = Importance.NOT_IMPORTANT;
    }
}
