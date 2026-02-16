package com.katlego.task_tracking_api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TaskStatus status;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_user_id",
            foreignKey = @ForeignKey(name = "fk_task_user")
    )
    private User assignedUser;

    @PrePersist
    public void prePersist() {
        this.createdDate = Instant.now();
        if (this.status == null) {
            this.status = TaskStatus.NEW;
        }
    }
}