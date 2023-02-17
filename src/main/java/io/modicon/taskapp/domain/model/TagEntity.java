package io.modicon.taskapp.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class TagEntity {

    @EqualsAndHashCode.Include
    @Id
    private String tagName;
    private Long taskCount;

    public void setNewName(String tagName) {
        this.tagName = tagName;
    }

    public void addTask() {
        taskCount++;
    }
}
