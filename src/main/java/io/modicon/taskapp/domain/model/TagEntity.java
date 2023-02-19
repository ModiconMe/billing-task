package io.modicon.taskapp.domain.model;

import jakarta.persistence.*;
import lombok.*;

@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(indexes = @Index(name = "tag_name_index", columnList = "tagName"))
public class TagEntity {

    @Id
    private String id;
    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true)
    private String tagName;
    private Long taskCount;

    public void setNewName(String tagName) {
        this.tagName = tagName;
    }

    public void addTask() {
        taskCount++;
    }
    public void removeTask() {
        taskCount--;
    }
}
