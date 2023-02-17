package io.modicon.taskapp.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
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

    @Enumerated(value = EnumType.ORDINAL)
    @Column(nullable = false)
    private PriorityType priorityType;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate createdAt;
    @Column(nullable = false)
    private LocalDate finishDate;

    @ManyToOne(cascade = CascadeType.MERGE)
    private TagEntity tag;

    @ManyToOne(cascade = CascadeType.MERGE)
    private UserEntity creator;

    @Singular
    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<FileData> files;
}
