package io.modicon.taskapp.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Getter
@Entity
@Table
public class TaskEntity {

    @EqualsAndHashCode.Include
    @Id
    private String id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private PriorityType priorityType;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate createdAt;
    @Column(nullable = false)
    private LocalDate finishDate;

    @Singular
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "task_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags;

    @ManyToOne(cascade = CascadeType.MERGE)
    private UserEntity creator;
}
