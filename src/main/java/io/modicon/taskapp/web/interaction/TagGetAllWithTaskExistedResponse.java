package io.modicon.taskapp.web.interaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TagGetAllWithTaskExistedResponse {
    List<String> tags;
}
